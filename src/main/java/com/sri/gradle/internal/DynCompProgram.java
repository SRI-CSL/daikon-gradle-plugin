package com.sri.gradle.internal;

import java.nio.file.Path;

public interface DynCompProgram extends Program {
  default DynCompProgram setOutputDirectory(Path directory) {
    args(String.format("--output_dir=%s", directory));
    return this;
  }
}
