package com.sri.gradle.daikon.extensions;

import com.sri.gradle.daikon.Constants;
import org.gradle.api.Project;
import org.gradle.api.tasks.compile.JavaCompile;


public class CompileTestDriverJavaExtension {
  private final Project project;
  private boolean compileTestDriverSeparately = true;

  public CompileTestDriverJavaExtension(Project project) {
    this.project = project;
  }

  public boolean getCompileTestDriverSeparately() {
    return compileTestDriverSeparately;
  }

  public void setCompileTestDriverSeparately(boolean compileTestDriverSeparately) {
    if (compileTestDriverSeparately) {
      // We need to create "compileTestDriverJava" task eagerly so that
      // the user can configure it immediately
      project.getTasks().maybeCreate(Constants.COMPILE_TEST_DRIVER, JavaCompile.class);
    }

    this.compileTestDriverSeparately = compileTestDriverSeparately;
  }
}
