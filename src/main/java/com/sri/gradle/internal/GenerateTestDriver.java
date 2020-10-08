package com.sri.gradle.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.sri.gradle.Constants;
import com.sri.gradle.utils.ImmutableStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;

public class GenerateTestDriver {

  private final Project project;
  private final Set<File> runtimeClasspath;
  private final String testDriverPackage;
  private final List<String> allTestClasses;

  private final boolean isJUnit5Platform;

  public GenerateTestDriver(Project project, String testDriverPackage, List<String> allTestClasses){
    this.project = Objects.requireNonNull(project);
    this.runtimeClasspath = ImmutableSet.copyOf(getRuntimeClasspath(project));
    this.testDriverPackage = testDriverPackage;
    this.isJUnit5Platform = this.runtimeClasspath.stream().anyMatch(f -> f.toString().contains("org.junit.platform.launcher"));

    this.allTestClasses = Objects.requireNonNull(allTestClasses);
  }

  public List<String> getAllTestClasses(){
    return allTestClasses;
  }

  public Project getProject(){
    return project;
  }

  public Set<File> getRuntimeClasspath(){
    return runtimeClasspath;
  }

  public String getTestDriverPackage(){
    return testDriverPackage;
  }

  private static Set<File> getRuntimeClasspath(Project project){
    // HACK. needed the test's runtime classpath to compile the test driver.
    // This classpath is different than the one the Daikon tool needs.
    // TODO(has) to find a better way to get this classpath.
    return ((SourceSetContainer) project
        .getProperties().get("sourceSets"))
        .getByName("test").getRuntimeClasspath()
        .getFiles();
  }

  Code generateCode(){
    Builder builder = Code.builder(getTestDriverPackage());
    builder = isJUnit5Platform
        ? builder.addImports(
            "org.junit.platform.launcher.Launcher",
            "org.junit.platform.launcher.LauncherDiscoveryRequest",
            "org.junit.platform.launcher.TestPlan",
            "org.junit.platform.launcher.core.LauncherFactory",
            "org.junit.platform.launcher.listeners.SummaryGeneratingListener",
            "org.junit.platform.launcher.listeners.TestExecutionSummary",
            "java.io.PrintWriter")
          .addLineBreak()
          .addStaticImports(
              "org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns",
              "org.junit.platform.engine.discovery.DiscoverySelectors.selectClass",
              "org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage")
          .addLineBreak()
        // JUnit 4.13
        : builder.addImports("org.junit.runner.Result", "org.junit.runner.JUnitCore");

    builder = builder.addComment("Auto-generated class.");
    builder = builder.addClass(Constants.TEST_DRIVER_CLASSNAME).indent();

    if (isJUnit5Platform){
      builder = builder.addLine("SummaryGeneratingListener listener = new SummaryGeneratingListener();").addLineBreak();
      builder = builder.addMethod("public void runAll()",
          "LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder",
          "\t\t\t\t.request()", "\t\t\t\t.selectors(selectPackage(\"" + getTestDriverPackage() + "\"))",
          "\t\t\t\t.filters(includeClassNamePatterns(\".*Test\"))",
          "\t\t\t\t.build();",
          "Launcher launcher = LauncherFactory.create();",
          "TestPlan testPlan = launcher.discover(request);",
          "launcher.registerTestExecutionListeners(listener);",
          "launcher.execute(request);").addLineBreak().indent();
      builder = builder.addStaticMainMethod(
          Constants.TEST_DRIVER_CLASSNAME + " runner = new " + Constants.TEST_DRIVER_CLASSNAME + "();",
          "runner.runAll();",
          "TestExecutionSummary summary = runner.listener.getSummary();",
          "summary.printTo(new PrintWriter(System.out));"
      );
    } else {
      String joinedTestClasses = Joiner.on(", ").join(ImmutableStream.listCopyOf(Objects.requireNonNull(getAllTestClasses()).stream().map(s -> s + ".class")));
      builder = builder.addStaticMainMethod(
          "final Result result = JUnitCore.runClasses(" + joinedTestClasses + ");"
          , "System.out.printf(\"Test ran: %s, Failed: %s%n\","
          , "\t\t\t\tresult.getRunCount(), result.getFailureCount());"
      );
    }

    builder = builder.addLine("}").unindent();
    return builder.build();
  }

  public boolean writeTo(File outputDir){
    final Code code = generateCode();

    File generatedJavaFile = null;
    try {
      generatedJavaFile = code.writeToFile(outputDir);
    } catch (IOException ignored){}

    if (generatedJavaFile == null){
      throw new NullPointerException("Unable to write code; java file is null.");
    }

    // Compiles the newly generated test driver class
    final Javac javac = new Javac()
        .debug()
        .classpath(getRuntimeClasspath())
        .workingDirectory(outputDir)
        .destination(getProject().getLayout()
            .getBuildDirectory()
            .dir(Constants.PROJECT_TEST_CLASS_DIR + Constants.FILE_SEPARATOR)
            .get().getAsFile());

    final List<String> output = javac.compile(generatedJavaFile);
    if (!output.isEmpty()){

      StringBuilder msg = new StringBuilder();
      output.forEach(s -> msg.append(s).append(Constants.NEW_LINE));
      getProject().getLogger().quiet(String.format("Unable to compile %s java file. See errors: %s", generatedJavaFile, msg.toString()));
      return false;
    }

    return true;

  }


  static class Code {

    final String content;

    private Code(Builder builder) {
      this.content = builder.code + Constants.NEW_LINE;
    }

    public static Builder builder(String packageName) {
      return new Builder().addPackageName(packageName);
    }


    /**
     * Writes this to {@code directory} as UTF-8 using the standard directory structure.
     *
     * @return the {@link File} instance to which source is actually written.
     */
    public File writeToFile(File directory) throws IOException {
      final Path outputPath = writeToPath(directory.toPath());
      return outputPath.toFile();
    }

    /**
     * Writes this to {@code directory} as UTF-8 using the standard directory structure.
     * @return the {@link Path} instance to which source is actually written.
     */
    public Path writeToPath(Path directory) throws IOException {
      return writeToPath(directory, UTF_8);
    }

    /**
     * Writes this to {@code directory} with the provided {@code charset} using the standard directory
     * structure.
     * @return the {@link Path} instance to which source is actually written.
     */
    public Path writeToPath(Path directory, Charset charset) throws IOException {
      Preconditions.checkArgument(Files.notExists(directory) || Files.isDirectory(directory),
          "path %s exists but is not a directory.", directory);

      Path outputPath = directory.resolve(Constants.TEST_DRIVER_CLASSNAME + ".java");
      if (Files.exists(outputPath)){
        Files.delete(outputPath);
      }

      try (Writer writer = new OutputStreamWriter(Files.newOutputStream(outputPath), charset)) {
        writer.write(content);
      }

      return outputPath;
    }

    @Override public String toString() {
      return content;
    }
  }

  private static class Builder {
    int indentLevel;
    String code;

    private Builder() {}

    Builder addClass(String typeName){
      return addLine("public final class " + typeName + " {");
    }

    Builder addPackageName(String typeName){
      return addLine("package " + typeName + ";");
    }

    Builder addImports(String... typeNames){
      return addImports(Arrays.asList(typeNames));
    }

    Builder addImports(List<String> importList){
      for (String each : importList){
        addImport(each);
      }

      return this;
    }

    Builder addStaticImports(String... typeNames){
      return addStaticImports(Arrays.asList(typeNames));
    }

    Builder addStaticImports(List<String> importList){
      for (String each : importList){
        addStaticImport(each);
      }

      return this;
    }

    void addImport(String typeName){
      addLine("import " + typeName + ";");
    }

    void addStaticImport(String typeName){
      addLine("import static " + typeName + ";");
    }

    // TODO(has) make this API more general by supporting different types
    //  of modifiers: e.g., protected, private, final
    Builder addStaticMainMethod(String... body){
      return addMethod("public static void main(String... args)", body);
    }


    Builder addMethod(String declaration, String... body){
      addLine(declaration + "{" + Constants.NEW_LINE).indent();

      for (String eachLine : body){
        if (eachLine != null){
          addLine(eachLine);
        }
      }

      unindent().addLine("}").unindent();
      return this;
    }

    public Builder addComment(String comment) {
      String newLine = addAndIndent(("// " + Objects.requireNonNull(comment)) + Constants.NEW_LINE, indentLevel);
      this.code = this.code == null ? newLine : this.code + newLine;
      return this;
    }

    Builder addLineBreak(){
      return addLine("");
    }

    Builder addLine(String codeLine) {
      String newLine = addAndIndent(Objects.requireNonNull(codeLine) + Constants.NEW_LINE, indentLevel);
      this.code = this.code == null ? newLine : this.code + newLine;
      return this;
    }

    static String addAndIndent(String code, int indentLevel) {

      final String newLine = Constants.NEW_LINE;

      final StringBuilder out = new StringBuilder();
      boolean first = true;
      final String[] lines = code.split(newLine, -1);
      for (String line : lines) {

        if (!first) {
          out.append(newLine);
        }

        first = false;

        if (line.isEmpty())
          continue; // Don't indent empty lines.

        addIndentation(out, indentLevel);

        out.append(line);
      }

      return out.toString();
    }

    static void addIndentation(StringBuilder out, int indentLevel) {
      for (int j = 0; j < indentLevel; j++) {
        out.append(Constants.DOUBLE_SPACE);
      }
    }

    Code build() {
      return new Code(this);
    }

    Builder indent() {
      indentLevel = Math.min(10, ++indentLevel);
      return this;
    }

    Builder unindent() {
      indentLevel = Math.max(0, --indentLevel);
      return this;
    }

  }

}
