package com.sri.gradle.internal;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public interface Tool {

  void args(Object... args);

  /**
   * Executes the tool given its previous configuration.
   * @throws ToolException if Daikon is not found either in the project's classpath
   *  or in the path provided by the user in the project's build.gradle (i.e., requires(x)
   *  statement)
   */
  void execute() throws ToolException;

  default Tool help() {
    args("--help");
    return this;
  }

  Tool setClasspath(List<URL> classpathUrls);

  default Tool setComparabilityFile(Path directory, String filename) {
    final Path resolved = directory.resolve(filename);
    args(String.format("--comparability-file=%s", resolved));
    return this;
  }

  default Tool setMainClass(String name) {
    if (name == null || name.isEmpty()) {
      return this;
    }

    args(name);
    return this;
  }

  default Tool setOmitPatterns(List<String> fullyQualifiedClassNamePatterns) {
    //noinspection Convert2streamapi
    for (String qualifiedName : fullyQualifiedClassNamePatterns) { // unchecked warning
      setOmitPattern(qualifiedName);
    }

    return this;
  }

  default Tool setOmitPattern(String classnamePattern) {
    args("--ppt-omit-pattern=" + classnamePattern);
    return this;
  }

  default Tool setOutputDirectory(Path directory) {
    args(String.format("--output_dir=%s", directory));
    return this;
  }

  default Tool setDtraceFile(Path directory, String filename) {
    final Path resolved = directory.resolve(filename);
    args(String.format("%s", resolved));
    return this;
  }

  default Tool setSelectPatterns(List<String> fullyQualifiedClassNamePatterns) {
    //noinspection Convert2streamapi
    for (String qualifiedName : fullyQualifiedClassNamePatterns) { // unchecked warning
      setSelectPattern(qualifiedName);
    }

    return this;
  }

  default Tool setSelectPattern(String classnamePattern) {
    args("--ppt-select-pattern=" + classnamePattern);
    return this;
  }

  default Tool setStandardOutput(String filename) {
    args(String.format("-o %s", filename));

    return this;
  }


  default Tool setSelectedClasses(List<String> fullyQualifiedClassNames) {
    return setSelectPatterns(fullyQualifiedClassNames);
  }

  Tool setToolJar(File toolJar);

  Tool setWorkingDirectory(Path directory);
}
