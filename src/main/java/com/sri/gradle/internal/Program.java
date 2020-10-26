package com.sri.gradle.internal;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface Program {

  void args(Object... args);

  Program addToolJarToClasspath(File toolJar);

  void execute() throws JavaProgramException;

  Program setClasspath(List<File> files);

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

  default Program setDtraceFile(String filename) {
    args(String.format("%s", filename));
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

  default Program setSelectedClasses(List<String> fullyQualifiedClassNames) {
    return setSelectPatterns(fullyQualifiedClassNames);
  }

  Program setWorkingDirectory(Path directory);
}
