package com.sri.gradle.daikon.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Preconditions;
import com.sri.gradle.daikon.Constants;
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

public class JavaCode {
  final String className;
  final String content;

  private JavaCode(Builder builder) {
    this.className = builder.className;
    this.content = builder.code + Constants.NEW_LINE;
  }

  public static Builder classBuilder(String className) {
    return new Builder().addClass(className);
  }

  /**
   * Writes this to {@code directory}.
   *
   * @param outputDir the output {@code directory}
   * @return the {@link File} instance to which source is actually written; otherwise a null object.
   */
  public File writeTo(File outputDir) {
    File generatedJavaFile = null;
    try {
      if (!Files.exists(outputDir.toPath())) {
        outputDir = Files.createDirectories(outputDir.toPath()).toFile();
      }

      generatedJavaFile = writeToFile(outputDir);
    } catch (IOException ignored) {
    }

    return generatedJavaFile;
  }

  /**
   * Writes this to {@code directory} as UTF-8 using the standard directory structure.
   *
   * @return the {@link File} instance to which source is actually written.
   */
  private File writeToFile(File directory) throws IOException {
    final Path outputPath = writeToPath(directory.toPath());
    return outputPath.toFile();
  }

  /**
   * Writes this to {@code directory} as UTF-8 using the standard directory structure.
   *
   * @return the {@link Path} instance to which source is actually written.
   */
  private Path writeToPath(Path directory) throws IOException {
    return writeToPath(directory, UTF_8);
  }

  /**
   * Writes this to {@code directory} with the provided {@code charset} using the standard directory
   * structure.
   *
   * @return the {@link Path} instance to which source is actually written.
   */
  private Path writeToPath(Path directory, Charset charset) throws IOException {
    Preconditions.checkArgument(
        Files.notExists(directory) || Files.isDirectory(directory),
        "path %s exists but is not a directory.",
        directory);

    Path outputPath = directory.resolve(Constants.TEST_DRIVER_CLASSNAME + ".java");
    if (Files.exists(outputPath)) {
      Files.delete(outputPath);
    }

    try (Writer writer = new OutputStreamWriter(Files.newOutputStream(outputPath), charset)) {
      writer.write(content);
    }

    return outputPath;
  }

  @Override
  public String toString() {
    return content;
  }

  public static class Builder {

    String className;
    int indentLevel;
    String code;

    Builder() {}

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

    public Builder addClass(String typeName) {
      Preconditions.checkArgument(typeName != null && !typeName.isEmpty());
      this.className = typeName;
      return this;
    }

    public Builder addClassDeclaration() {
      return addLine("public final class " + this.className + " {");
    }

    public Builder addPackageName(String typeName) {
      return addLine("package " + typeName + ";");
    }

    public Builder addImports(String... typeNames) {
      return addImports(Arrays.asList(typeNames));
    }

    public Builder addImports(List<String> importList) {
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

    public void addImport(String typeName) {
      addLine("import " + typeName + ";");
    }

    public void addStaticImport(String typeName) {
      addLine("import static " + typeName + ";");
    }

    // TODO(has) make this API more general by supporting different types
    //  of modifiers: e.g., protected, private, final
    public Builder addStaticMainMethod(String... body) {
      return addComment("Auto-generated class.")
          .addClassDeclaration()
          .indent()
          .addMethod("public static void main(String... args) throws Exception", body)
          .addLine("}")
          .unindent();
    }

    public Builder addMethod(String declaration, String... body) {
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
      String newLine =
          addAndIndent(("// " + Objects.requireNonNull(comment)) + Constants.NEW_LINE, indentLevel);
      this.code = this.code == null ? newLine : this.code + newLine;
      return this;
    }

    public Builder addLineBreak() {
      return addLine("");
    }

    public Builder addLine(String codeLine) {
      String newLine =
          addAndIndent(Objects.requireNonNull(codeLine) + Constants.NEW_LINE, indentLevel);
      this.code = this.code == null ? newLine : this.code + newLine;
      return this;
    }

    public JavaCode build() {
      return new JavaCode(this);
    }

    public Builder indent() {
      indentLevel = Math.min(10, ++indentLevel);
      return this;
    }

    public Builder unindent() {
      indentLevel = Math.max(0, --indentLevel);
      return this;
    }
  }
}
