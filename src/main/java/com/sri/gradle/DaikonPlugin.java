package com.sri.gradle;

import com.sri.gradle.tasks.AbstractNamedTask;
import com.sri.gradle.tasks.CheckForDaikon;
import com.sri.gradle.tasks.RunDaikon;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DaikonPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    DaikonPluginExtension extension = project.getExtensions().create(
        Constants.PLUGIN_EXTENSION, DaikonPluginExtension.class, project);

    final CheckForDaikon checkDaikonInstallation = createCheckForDaikon(project);

    final RunDaikon mainTask = createRunDaikonTask(project, extension);
    mainTask.dependsOn(checkDaikonInstallation, "build");

    project.getLogger().quiet("Executing " + mainTask.getName());
  }

  private RunDaikon createRunDaikonTask(Project project, DaikonPluginExtension extension) {
    final RunDaikon mainTask = project.getTasks().create(Constants.DAIKON_TASK, RunDaikon.class);
    mainTask.setGroup(Constants.GROUP);
    mainTask.setDescription(Constants.PLUGIN_DESCRIPTION);
    // TODO(has) consider removing this task dependency. Not sure if it's needed.
    mainTask.dependsOn(Constants.ASSEMBLE_TASK);

    mainTask.getRequires().set(extension.getRequires());
    mainTask.getOutputDir().set(extension.getOutputDir());
    mainTask.getTestDriverPackage().set(extension.getTestDriverPackage());

    return mainTask;
  }

  private CheckForDaikon createCheckForDaikon(Project project) {
    // Chicory and DynComp can be accessed via daikon.jar;
    // meaning if daikon.jar is in your classpath then we can assume they are there too
    CheckForDaikon checkTask = createCheckTask(project, Constants.CHECK_DAIKON_TASK, CheckForDaikon.class);
    checkTask.setDescription(Constants.CHECK_DAIKON_TASK_DESCRIPTION);
    return checkTask;
  }

  @SuppressWarnings("SameParameterValue")
  private static <T extends AbstractNamedTask> T createCheckTask(Project project, String taskName, Class<T> taskClass) {
    final T checkTask = project.getTasks().create(taskName, taskClass);
    checkTask.setGroup(Constants.GROUP);
    return checkTask;
  }

}
