package com.sri.gradle.daikon.tasks;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public interface TaskBuilder {
  // TODO(has) add more Daikon's options
  /**
   * Builds the classpath needed to run the Daikon tools. Client code
   * can update this classpath, if needed.
   *
   * <p>Note: this method assumes that the working directory is the same as the output directory set
   * after calling the {@link OutputBuilder#toDir(File)} method. The {@link TaskBuilder} will throw
   * an exception if this output directory is null.
   *
   * @param files additional files
   * @return a reference to the task builder
   */
  default OutputBuilder withClasspath(File... files) {
    return withClasspath(Arrays.asList(files));
  }

  /**
   * Builds classpath from a list of files
   *
   * @param files additional files
   * @return a reference to the task builder
   */
  OutputBuilder withClasspath(List<File> files);
}
