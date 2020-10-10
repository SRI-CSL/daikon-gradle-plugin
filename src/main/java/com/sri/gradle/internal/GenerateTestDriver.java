package com.sri.gradle.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.sri.gradle.Constants;
import com.sri.gradle.utils.Classpath;
import com.sri.gradle.utils.Filefinder;
import com.sri.gradle.utils.ImmutableStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;

public class GenerateTestDriver {

  private final Project project;
  private final Set<File> runtimeClasspath;
  private final String testDriverPackage;
  private final List<String> allTestClassNames;
  private final Map<String, File> testClassNameToFile;

  private final boolean isJUnit5Platform;

  public GenerateTestDriver(Project project, String testDriverPackage,
      List<String> allFullyQualifiedTestClassNames) {
    this.project = Objects.requireNonNull(project);
    this.runtimeClasspath = ImmutableSet.copyOf(Classpath.getRuntimeClasspath(project));
    this.testDriverPackage = testDriverPackage;
    this.isJUnit5Platform = this.runtimeClasspath.stream()
        .anyMatch(f -> f.toString().contains("org.junit.platform.launcher"));

    this.allTestClassNames = Objects.requireNonNull(allFullyQualifiedTestClassNames);
    final Directory testSrcDir = getProject()
        .getLayout().getProjectDirectory()
        .dir(Constants.PROJECT_TEST_SRC_DIR)
        .dir(this.testDriverPackage.replaceAll("\\" + Constants.PERIOD, Constants.FILE_SEPARATOR));

    final List<File> allTestFiles = Filefinder.findJavaFiles(testSrcDir.getAsFile().toPath());
    this.testClassNameToFile = buildTestClassNameToFileMap(this.testDriverPackage, allTestFiles,
        this.allTestClassNames);
  }

  private static Map<String, File> buildTestClassNameToFileMap(String testDriverPackage,
      List<File> allTestFiles, List<String> allFullyQualifiedTestClassNames) {
    Map<String, File> testClassNameToFile = new HashMap<>();
    for (File each : allTestFiles) {
      for (String eachName : allFullyQualifiedTestClassNames) {
        final String actualName = eachName.replace(testDriverPackage + Constants.PERIOD, "");
        if (each.getName().replace(".java", Constants.EMPTY_STRING).equals(actualName)) {
          testClassNameToFile.put(eachName, each);
        }
      }
    }

    return ImmutableMap.copyOf(testClassNameToFile);
  }

  public List<String> getAllTestClassNames() {
    return allTestClassNames;
  }

  public Project getProject() {
    return project;
  }

  public Set<File> getRuntimeClasspath() {
    return runtimeClasspath;
  }

  public String getTestDriverPackage() {
    return testDriverPackage;
  }

  public Map<String, File> getTestClassNameToFile() {
    return testClassNameToFile;
  }

  Code generateCodeForRunningTestCases() {
    Builder builder = Code.builder(getTestDriverPackage());

    Map<String, List<String>> allTestMethodCallsPerClass = ClassInsider
        .buildsClassToMethodCallsMap(getTestClassNameToFile());
    StringBuilder bodyBuilder = new StringBuilder();
    int i = 1;
    for (String eachTestClass : allTestMethodCallsPerClass.keySet()) {
      String instanceName = "testClass" + i;
      bodyBuilder.append(eachTestClass)
          .append(Constants.SPACE)
          .append(instanceName).append(" = ")
          .append("new ").append(eachTestClass).append("();")
          .append(Constants.NEW_LINE);

      for (String eachCall : allTestMethodCallsPerClass.get(eachTestClass)) {
        bodyBuilder.append(instanceName)
            .append(Constants.PERIOD)
            .append(eachCall)
            .append(Constants.NEW_LINE);
      }

      i++;
      if (i < allTestMethodCallsPerClass.size()) {
        bodyBuilder.append(Constants.NEW_LINE);
      }
    }

    builder = builder.addComment("Auto-generated class.");
    builder = builder.addClass(Constants.TEST_DRIVER_CLASSNAME).indent();
    builder = builder.addMethod("public void runAll() throws Exception ", bodyBuilder.toString())
        .addLineBreak().indent();
    builder = builder.addStaticMainMethod(
        Constants.TEST_DRIVER_CLASSNAME + " runner = new " + Constants.TEST_DRIVER_CLASSNAME
            + "();",
        "runner.runAll();"
    );

    builder = builder.addLine("}").unindent();
    return builder.build();
  }

  Code generateCodeThatUsesFacadeObjectsForRunningTests() {
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

    if (isJUnit5Platform) {
      builder = builder
          .addLine("SummaryGeneratingListener listener = new SummaryGeneratingListener();")
          .addLineBreak();
      builder = builder.addMethod("public void runAll()",
          "LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder",
          "\t\t\t\t.request()",
          "\t\t\t\t.selectors(selectPackage(\"" + getTestDriverPackage() + "\"))",
          "\t\t\t\t.filters(includeClassNamePatterns(\".*Test\"))",
          "\t\t\t\t.build();",
          "Launcher launcher = LauncherFactory.create();",
          "TestPlan testPlan = launcher.discover(request);",
          "launcher.registerTestExecutionListeners(listener);",
          "launcher.execute(request);").addLineBreak().indent();
      builder = builder.addStaticMainMethod(
          Constants.TEST_DRIVER_CLASSNAME + " runner = new " + Constants.TEST_DRIVER_CLASSNAME
              + "();",
          "runner.runAll();",
          "TestExecutionSummary summary = runner.listener.getSummary();",
          "summary.printTo(new PrintWriter(System.out));"
      );
    } else {
      String joinedTestClasses = Joiner.on(", ")
          .join(ImmutableStream.listCopyOf(Objects.requireNonNull(
              getAllTestClassNames()).stream().map(s -> s + ".class")));
      builder = builder.addStaticMainMethod(
          "final Result result = JUnitCore.runClasses(" + joinedTestClasses + ");"
          , "System.out.printf(\"Test ran: %s, Failed: %s%n\","
          , "\t\t\t\tresult.getRunCount(), result.getFailureCount());"
      );
    }

    builder = builder.addLine("}").unindent();
    return builder.build();
  }

  public boolean writeTo(File outputDir) {
    final Code code = generateCodeForRunningTestCases();

    File generatedJavaFile = null;
    try {
      generatedJavaFile = code.writeToFile(outputDir);
    } catch (IOException ignored) {
    }

    if (generatedJavaFile == null) {
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
    if (!output.isEmpty()) {

      StringBuilder msg = new StringBuilder();
      output.forEach(s -> msg.append(s).append(Constants.NEW_LINE));
      getProject().getLogger().quiet(String
          .format("Unable to compile %s java file. See errors: %s", generatedJavaFile,
              msg.toString()));
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


    public File writeToFile(File directory) throws IOException {
      final Path outputPath = writeToPath(directory.toPath());
      return outputPath.toFile();
    }


    public Path writeToPath(Path directory) throws IOException {
      return writeToPath(directory, UTF_8);
    }


    public Path writeToPath(Path directory, Charset charset) throws IOException {
      Preconditions.checkArgument(Files.notExists(directory) || Files.isDirectory(directory),
          "path %s exists but is not a directory.", directory);

      Path outputPath = directory.resolve(Constants.TEST_DRIVER_CLASSNAME + ".java");
      if (Files.exists(outputPath)) {
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

    private Builder() {
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

        if (line.isEmpty()) {
          continue; // Don't indent empty lines.
        }

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

    Builder addClass(String typeName) {
      return addLine("public final class " + typeName + " {");
    }

    Builder addPackageName(String typeName) {
      return addLine("package " + typeName + ";");
    }

    Builder addImports(String... typeNames) {
      return addImports(Arrays.asList(typeNames));
    }

    Builder addImports(List<String> importList) {
      for (String each : importList) {
        addImport(each);
      }

      return this;
    }

    Builder addStaticImports(String... typeNames) {
      return addStaticImports(Arrays.asList(typeNames));
    }

    Builder addStaticImports(List<String> importList) {
      for (String each : importList) {
        addStaticImport(each);
      }

      return this;
    }

    void addImport(String typeName) {
      addLine("import " + typeName + ";");
    }

    void addStaticImport(String typeName) {
      addLine("import static " + typeName + ";");
    }

    // TODO(has) make this API more general by supporting different types
    //  of modifiers: e.g., protected, private, final
    Builder addStaticMainMethod(String... body) {
      return addMethod("public static void main(String... args) throws Exception", body);
    }

    Builder addMethod(String declaration, String... body) {
      addLine(declaration + "{" + Constants.NEW_LINE).indent();

      for (String eachLine : body) {
        if (eachLine != null) {
          addLine(eachLine);
        }
      }

      unindent().addLine("}").unindent();
      return this;
    }

    public Builder addComment(String comment) {
      String newLine = addAndIndent(("// " + Objects.requireNonNull(comment)) + Constants.NEW_LINE,
          indentLevel);
      this.code = this.code == null ? newLine : this.code + newLine;
      return this;
    }

    Builder addLineBreak() {
      return addLine("");
    }

    Builder addLine(String codeLine) {
      String newLine = addAndIndent(Objects.requireNonNull(codeLine) + Constants.NEW_LINE,
          indentLevel);
      this.code = this.code == null ? newLine : this.code + newLine;
      return this;
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


  static class ClassInsider {

    private ClassInsider() {
    }

    static Map<String, List<String>> buildsClassToMethodCallsMap(
        Map<String, File> testClassNameToFile) {
      final Map<String, List<String>> methodCallsPerClass = new HashMap<>();

      try {
        for (String eachTestClassName : testClassNameToFile.keySet()) {
          final CompilationUnit compilationUnit = StaticJavaParser
              .parse(testClassNameToFile.get(eachTestClassName));
          final ClassOrInterfaceDeclaration declaration = compilationUnit
              .findAll(ClassOrInterfaceDeclaration.class)
              .stream().filter(cls -> cls.isPublic() && !cls.isInnerClass())
              .findFirst().orElse(null);

          if (declaration == null) {
            continue;
          }

          final List<String> methodNames = ImmutableStream.listCopyOf(
              declaration.getMethods().stream()
                  .filter(m -> m.isAnnotationPresent("Test"))
                  .map(NodeWithSimpleName::getNameAsString));

          if (!methodCallsPerClass.containsKey(eachTestClassName)) {
            methodCallsPerClass.put(eachTestClassName, new LinkedList<>());
          }

          for (String eachMethodName : methodNames) {
            methodCallsPerClass.get(eachTestClassName).add(eachMethodName + "();");
          }

        }
      } catch (FileNotFoundException ignored) {
      }

      return methodCallsPerClass;
    }

  }

}
