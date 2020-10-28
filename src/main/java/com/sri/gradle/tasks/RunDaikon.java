package com.sri.gradle.tasks;

import com.sri.gradle.Constants;
import com.sri.gradle.utils.Filefinder;
import com.sri.gradle.utils.JavaProjectHelper;
import com.sri.gradle.utils.MoreFiles;
import java.io.File;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

@SuppressWarnings("UnstableApiUsage")
public class RunDaikon extends AbstractNamedTask {

  private final DirectoryProperty outputDir;
  private final DirectoryProperty requires;
  private final Property<String> testDriverPackage;
  private final Property<Boolean> generateTestDriver;

  public RunDaikon() {
    this.outputDir = getProject().getObjects().directoryProperty(); // unchecked warning
    this.requires = getProject().getObjects().directoryProperty(); // unchecked warning
    this.testDriverPackage = getProject().getObjects().property(String.class); // unchecked warning
    this.generateTestDriver =
        getProject().getObjects().property(Boolean.class); // unchecked warning
  }

  @TaskAction
  public void daikonRun() {
    final TaskExecutorImpl executor = new TaskExecutorImpl();
    final JavaProjectHelper projectHelper = new JavaProjectHelper(getProject());

    final Directory buildMainDir = projectHelper.getBuildMainDir();
    final Directory buildTestDir = projectHelper.getBuildTestDir();
    final Directory testClassesDir =
        JavaProjectHelper.getTestClassesDir(getTestDriverPackage(), buildTestDir);

    if (testClassesDir == null) {
      throw new GradleException("Unable to find the test driver directory");
    }

    File inputDir = testClassesDir.getAsFile();

    // jar files under the directory specified in the build.gradle's `requires` statement
    final File dependenciesDir = getRequires().getAsFile().get();

    final List<File> classpath =
        new LinkedList<>(Filefinder.findJavaJars(dependenciesDir.toPath()));

    classpath.add(buildMainDir.getAsFile());
    classpath.add(buildTestDir.getAsFile());

    final File outputDir = getOutputDir().getAsFile().get();

    if (!Files.exists(outputDir.toPath())) {
      if (!outputDir.mkdir()) {
        throw new GradleException("Unable to create output directory");
      }

      getProject().getLogger().debug("Current output directory: " + outputDir);
    }

    if (Files.exists(outputDir.toPath())) {
      getProject().getLogger().debug("Current permissions for outputDir: " + MoreFiles.getPosixFilePermissions(outputDir).toString());
    }


    // If a test driver is needed, fetch the testDriverPackage value; otherwise fetch a null value
    final String testDriverPackage =
        getGenerateTestDriver().get() ? getTestDriverPackage().get() : null;

    final RunDaikonConfiguration config =
        new RunDaikonConfiguration(inputDir, testDriverPackage, getProject(), classpath, outputDir);

    getLogger().debug("Created RunDaikon task configuration");
    executor.install(config);
    getLogger().debug("Configured RunDaikon task");

    getLogger().debug("About to execute task");
    executor.execute();
    getLogger().quiet(Constants.SUCCESSFUL_DAIKON_EXECUTION);
  }

  @OutputDirectory
  public DirectoryProperty getOutputDir() {
    return this.outputDir;
  }

  @InputDirectory
  public DirectoryProperty getRequires() {
    return this.requires;
  }

  @Input
  public Property<String> getTestDriverPackage() {
    return this.testDriverPackage;
  }

  @Optional
  @Input
  public Property<Boolean> getGenerateTestDriver() {
    return this.generateTestDriver;
  }

  @Override
  protected String getTaskName() {
    return Constants.DAIKON_TASK;
  }

  @Override
  protected String getTaskDescription() {
    return Constants.DAIKON_TASK_DESCRIPTION;
  }

  static class RunDaikonConfiguration extends AbstractConfiguration {

    private final File inputDir;
    private final String testDriverPackage;
    private final Project project;
    private final List<File> classpath;
    private final File outputDir;

    RunDaikonConfiguration(
        File inputDir,
        String testDriverPackage,
        Project project,
        List<File> classpath,
        File outputDir) {
      this.inputDir = inputDir;
      this.testDriverPackage = testDriverPackage;
      this.project = project;
      this.classpath = classpath;
      this.outputDir = outputDir;
    }

    @Override
    protected void configure() {
      TaskBuilder builder =
          testDriverPackage == null
              ? runDaikonOn(new InputProviderImpl(inputDir, project))
              : runDaikonOn(new InputProviderImpl(inputDir, testDriverPackage, project));

      builder.withClasspath(classpath).toDir(outputDir);
    }
  }

  abstract static class AbstractConfiguration implements TaskConfiguration {

    TaskExecutor executor;

    @Override
    public final synchronized void configure(TaskExecutor executor) {
      try {
        if (this.executor != null) {
          throw new IllegalStateException("executor already available");
        }

        this.executor = Objects.requireNonNull(executor);
        configure();
      } finally {
        this.executor = null;
      }
    }

    protected abstract void configure();

    /** @see TaskExecutor#runDaikonOn(InputProvider) */
    protected TaskBuilder runDaikonOn(InputProvider inputProvider) {
      return executor.runDaikonOn(inputProvider);
    }

    /** @see TaskExecutor#addError(Throwable) */
    protected void addError(Throwable t) {
      executor.addError(t);
    }
  }
}
