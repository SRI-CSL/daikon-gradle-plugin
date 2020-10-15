package com.sri.gradle.tasks;

import com.google.common.collect.ImmutableSet;
import com.sri.gradle.Constants;
import java.io.File;
import java.util.Set;
import javax.annotation.Nonnull;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;

public abstract class AbstractNamedTask extends DefaultTask {
  // TODO(has) pls consider pruning this set
  private static final Set<String> ALLOWED_SET = ImmutableSet.of(
      Constants.DAIKON_JAR_FILE,
      Constants.CHICORY_JAR_FILE,
      Constants.DYN_COMP_RT_JAR_FILE,
      Constants.DYN_COMP_PRE_MAIN_JAR_FILE
  );

  @Internal protected abstract String getTaskName();
  @Internal protected abstract String getTaskDescription();

  @SuppressWarnings("SameParameterValue")
  protected File getJarfile(String jarName) {
    if (!ALLOWED_SET.contains(jarName)) {
      throw new IllegalArgumentException("Unknown options");
    }

    return getProject().getLayout()
        .getProjectDirectory()
        .dir(Constants.PROJECT_LIB_DIR)
        .file(jarName)
        .getAsFile();
  }

  @Override @Nonnull public String toString() {
    return getTaskName() + ": " + getTaskDescription();
  }
}
