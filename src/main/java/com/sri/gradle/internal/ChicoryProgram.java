package com.sri.gradle.internal;

import java.nio.file.Path;

public interface ChicoryProgram extends Program {
  default ChicoryProgram setComparabilityFile(Path directory, String filename) {
    final Path resolved = directory.resolve(filename);
    args(String.format("--comparability-file=%s", resolved));
    return this;
  }
}
