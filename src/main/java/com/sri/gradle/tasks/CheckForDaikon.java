package com.sri.gradle.tasks;

import com.sri.gradle.Constants;
import com.sri.gradle.internal.MainExecutor;
import java.io.File;
import java.nio.charset.StandardCharsets;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.compile.ForkOptions;

public class CheckForDaikon extends AbstractNamedTask {

  @TaskAction
  public void daikonCheck() {
    try {
      final File daikonJar = getJarfile(Constants.DAIKON_JAR_FILE);
      final ForkOptions options = new ForkOptions();


      final MainExecutor mainExecutor = new MainExecutor(getProject());
      mainExecutor.execDaikon(spec -> {
        spec.setClasspath(getProject().files(daikonJar));
        spec.setWorkingDir(getProject().getProjectDir());

        spec.args("--help");

        spec.forkOptions(fork -> {
          fork.setWorkingDir(spec.getWorkingDir());
          fork.setJvmArgs(options.getJvmArgs());
          fork.setMinHeapSize(options.getMemoryInitialSize());
          fork.setMaxHeapSize(options.getMemoryMaximumSize());
          fork.setDefaultCharacterEncoding(StandardCharsets.UTF_8.name());
        });
      });

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
