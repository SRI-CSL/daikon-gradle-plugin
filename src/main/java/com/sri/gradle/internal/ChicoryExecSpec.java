package com.sri.gradle.internal;

import java.nio.file.Path;

public class ChicoryExecSpec extends MainExecSpec {
  public void setComparabilityFile(Path directory, String filename) {
    final Path resolved = directory.resolve(filename);
    args(String.format("--comparability-file=%s", resolved));
  }
}
