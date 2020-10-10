package com.sri.gradle.internal;

import org.gradle.api.GradleException;

public class ToolException extends GradleException {
  public ToolException(String message) {
    super(message);
  }

  public ToolException(String message, Throwable cause) {
    super(message, cause);
  }
}
