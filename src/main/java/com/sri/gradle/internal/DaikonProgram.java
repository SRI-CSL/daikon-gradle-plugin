package com.sri.gradle.internal;

public interface DaikonProgram extends Program {
  default DaikonProgram setStandardOutput(String filename) {
    args(String.format("%s %s", "-o", filename));

    return this;
  }

  default DaikonProgram setDtraceFile(String filename) {
    args(String.format("%s", filename));
    return this;
  }

  default DaikonProgram help() {
    args("--help");
    return this;
  }
}
