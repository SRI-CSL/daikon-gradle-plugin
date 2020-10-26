package com.sri.gradle.tasks;

import com.sri.gradle.Constants;
import com.sri.gradle.internal.Daikon;
import java.io.File;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

public class CheckForDaikon extends AbstractNamedTask {

  @TaskAction
  public void daikonCheck() {
    try {
      final File daikonJar = getJarfile(Constants.DAIKON_JAR_FILE);

      new Daikon().help()
          .addToolJarToClasspath(daikonJar)
          .setWorkingDirectory(getProject().getProjectDir().toPath())
          .execute();

    } catch (Exception e) {
      throw new GradleException(Constants.UNEXPECTED_ERROR);
    }
  }

  @Override
  protected String getTaskDescription() {
    return Constants.CHECK_DAIKON_TASK_DESCRIPTION;
  }

  @Override
  protected String getTaskName() {
    return Constants.CHECK_DAIKON_TASK;
  }
}
