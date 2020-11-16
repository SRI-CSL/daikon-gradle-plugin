package com.sri.gradle.daikon.internal;

import java.nio.file.Path;

public class ChicoryExecSpec extends MainExecSpec {
  public void setComparabilityFile(Path directory, String filename) {
    args(String.format("--comparability-file=%s", relativizeFile(directory, filename)));
  }
}
