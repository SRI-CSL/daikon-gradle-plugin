package com.sri.gradle.internal;

import java.nio.file.Path;

public class DynCompExecSpec extends MainExecSpec {
  public void setOutputDirectory(Path directory) {
    args(String.format("--output_dir=%s", directory));
  }
}
