package com.sri.gradle.daikon.tasks;

import java.io.File;

public interface OutputBuilder {
  /**
   * Sets work's output directory.
   *
   * @param outputDir the path to this work's output directory.
   */
  void toDir(File outputDir);
}
