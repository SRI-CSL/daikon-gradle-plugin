package com.sri.gradle.daikon.internal;

import java.nio.file.Path;

public class DaikonExecSpec extends MainExecSpec {
  public void setStandardOutput(Path directory, String filename) {
    args("-o", String.format("%s", relativizeFile(directory, filename)));
  }

  public void setDtraceFile(Path directory, String filename) {
    args(String.format("%s", relativizeFile(directory, filename)));
  }
}
