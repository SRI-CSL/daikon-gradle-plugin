package com.sri.gradle;

import com.sri.gradle.tasks.AbstractNamedTask;
import com.sri.gradle.tasks.CheckForDaikon;
import com.sri.gradle.tasks.CompileTestJavaTaskMutator;
import com.sri.gradle.tasks.RunDaikon;
import com.sri.gradle.tasks.SourceGeneratingTask;
import com.sri.gradle.utils.JavaProjectHelper;
import java.util.Optional;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;

@SuppressWarnings({"NullableProblems", "Convert2Lambda"})
public class DaikonPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    // We need this in order to have access to the JavaPlugin tasks.
    // If we don't do this, we won't be able to hook the test driver
    // generation into the Gradle build process.
    project.getPlugins().apply(JavaPlugin.class);

    DaikonPluginExtension extension =
        project
            .getExtensions()
            .create(Constants.PLUGIN_EXTENSION, DaikonPluginExtension.class, project);

    final CheckForDaikon checkDaikonInstallation = createCheckForDaikonTask(project);
    final JavaCompile configuredJavaCompileTask = configureSourceGeneratingTask(project, extension);
    checkDaikonInstallation.dependsOn(configuredJavaCompileTask);

    final RunDaikon mainTask = createRunDaikonTask(project, extension);
    mainTask.dependsOn("build", checkDaikonInstallation);

    project.getLogger().quiet("Executing " + mainTask.getName());
  }

  private RunDaikon createRunDaikonTask(Project project, DaikonPluginExtension extension) {
    final RunDaikon mainTask = project.getTasks().create(Constants.DAIKON_TASK, RunDaikon.class);
    mainTask.setGroup(Constants.GROUP);
    mainTask.setDescription(Constants.PLUGIN_DESCRIPTION);

    mainTask.getRequires().set(extension.getRequires());
    mainTask.getOutputDir().set(extension.getOutputDir());
    mainTask.getTestDriverPackage().set(extension.getTestDriverPackage());
    mainTask.getGenerateTestDriver().set(extension.getGenerateTestDriver());

    return mainTask;
  }

  private CheckForDaikon createCheckForDaikonTask(Project project) {
    // Chicory and DynComp can be accessed via daikon.jar;
    // meaning if daikon.jar is in your classpath then we can assume they are there too
    CheckForDaikon checkTask =
        createCustomPluginTask(project, Constants.CHECK_DAIKON_TASK, CheckForDaikon.class);
    checkTask.setDescription(Constants.CHECK_DAIKON_TASK_DESCRIPTION);
    return checkTask;
  }

  private JavaCompile configureSourceGeneratingTask(
      Project project, DaikonPluginExtension extension) {
    SourceGeneratingTask genCodeTask =
        createCustomPluginTask(project, Constants.CODE_GEN_TASK, SourceGeneratingTask.class);
    genCodeTask.setDescription(Constants.CODE_GEN_TASK_DESCRIPTION);

    genCodeTask.getTestDriverPackage().set(extension.getTestDriverPackage());

    Optional<JavaCompile> javaCompile =
        JavaProjectHelper.findTask(
            project, JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME, JavaCompile.class);
    if (!javaCompile.isPresent()) {
      throw new GradleException("JavaPlugin is available in this project");
    }

    final JavaCompile aJavaCompile = javaCompile.get();
    final CompileTestDriverJavaExtension driverExtension =
        aJavaCompile
            .getExtensions()
            .create("driverExtension", CompileTestDriverJavaExtension.class, project);

    // TODO(has) make this option a configurable option
    driverExtension.setCompileTestDriverSeparately(true);

    project
        .getLogger()
        .debug(
            "Compile test driver separately is "
                + (driverExtension.getCompileTestDriverSeparately() ? "enabled." : "disabled."));

    final JavaCompile testDriverJavaCompile =
        JavaProjectHelper.task(project, Constants.COMPILE_TEST_DRIVER, JavaCompile.class);
    testDriverJavaCompile.dependsOn(genCodeTask);
    final CompileTestJavaTaskMutator compileMutator =
        new CompileTestJavaTaskMutator(project, aJavaCompile.getClasspath());

    // Don't convert to lambda. Java lambdas cannot be used in the plugin
    // only as task inputs (e.g. Task.doFirst, Task.doLast). Whenever a receiver
    // is not a task input (e.g. Project.afterEvaluate, Distribution.contents), it's fine
    // to use Java lambdas. More info:
    // https://github.com/gradle/gradle/issues/5510#issuecomment-416860213
    genCodeTask.doLast(
        new Action<Task>() {
          @Override
          public void execute(Task ignored) {
            compileMutator.mutateJavaCompileTask(testDriverJavaCompile);
          }
        });

    return testDriverJavaCompile;
  }

  @SuppressWarnings("SameParameterValue")
  private static <T extends AbstractNamedTask> T createCustomPluginTask(
      Project project, String taskName, Class<T> taskClass) {
    final T checkTask = project.getTasks().create(taskName, taskClass);
    checkTask.setGroup(Constants.GROUP);
    return checkTask;
  }
}
