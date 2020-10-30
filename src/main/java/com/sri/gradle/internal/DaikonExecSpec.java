package com.sri.gradle.internal;

public class DaikonExecSpec extends MainExecSpec {
  public void setStandardOutput(String filename) {
    args(String.format("%s %s", "-o", filename));
  }

  public void setDtraceFile(String filename) {
    args(String.format("%s", filename));
  }
}
