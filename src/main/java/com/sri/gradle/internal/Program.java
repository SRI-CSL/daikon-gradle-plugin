package com.sri.gradle.internal;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface Program {

  void args(Object... args);

  /**
   * Executes the tool given its previous configuration.
   *
   * @throws JavaProgramException if Daikon is not found either in the project's classpath or in the
   *     path provided by the user in the project's build.gradle (i.e., requires(x) statement)
   */
  void execute() throws JavaProgramException;

  default Program help() {
    args("--help");
    return this;
  }

  Program setClasspath(List<File> files);

  default Program setComparabilityFile(Path directory, String filename) {
    final Path resolved = directory.resolve(filename);
    args(String.format("--comparability-file=%s", resolved));
    return this;
  }

  default Program setMainClass(String name) {
    if (name == null || name.isEmpty()) {
      return this;
    }

    args(name);
    return this;
  }

  default Program setOmitPatterns(List<String> fullyQualifiedClassNamePatterns) {
    //noinspection Convert2streamapi
    for (String qualifiedName : fullyQualifiedClassNamePatterns) { // unchecked warning
      setOmitPattern(qualifiedName);
    }

    return this;
  }

  default Program setOmitPattern(String classnamePattern) {
    args("--ppt-omit-pattern=" + classnamePattern);
    return this;
  }

  default Program setOutputDirectory(Path directory) {
    args(String.format("--output_dir=%s", directory));
    return this;
  }

  default Program setDtraceFile(Path directory, String filename) {
    final Path resolved = directory.resolve(filename);
    args(String.format("%s", resolved));
    return this;
  }

  default Program setSelectPatterns(List<String> fullyQualifiedClassNamePatterns) {
    //noinspection Convert2streamapi
    for (String qualifiedName : fullyQualifiedClassNamePatterns) { // unchecked warning
      setSelectPattern(qualifiedName);
    }

    return this;
  }

  default Program setSelectPattern(String classnamePattern) {
    args("--ppt-select-pattern=" + classnamePattern);
    return this;
  }

  default Program setStandardOutput(String filename) {
    args(String.format("%s %s", "-o", filename));

    return this;
  }

  default Program setSelectedClasses(List<String> fullyQualifiedClassNames) {
    return setSelectPatterns(fullyQualifiedClassNames);
  }

  Program addToolJarToClasspath(File toolJar);

  Program setWorkingDirectory(Path directory);
}
