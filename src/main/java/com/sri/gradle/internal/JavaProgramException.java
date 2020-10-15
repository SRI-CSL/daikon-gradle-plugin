package com.sri.gradle.internal;

import org.gradle.api.GradleException;

public class JavaProgramException extends GradleException {
  public JavaProgramException(String message) {
    super(message);
  }

  public JavaProgramException(String message, Throwable cause) {
    super(message, cause);
  }
}
