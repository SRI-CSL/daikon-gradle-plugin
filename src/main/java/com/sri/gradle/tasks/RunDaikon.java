package com.sri.gradle.tasks;

import com.sri.gradle.Constants;
import com.sri.gradle.utils.Filefinder;
import java.io.File;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.gradle.api.GradleException;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public class RunDaikon extends AbstractNamedTask {

  private final DirectoryProperty outputDir;
  private final DirectoryProperty requires;
  private final Property<String> testDriverPackage;

  @SuppressWarnings("UnstableApiUsage")
  public RunDaikon() {
    this.outputDir = getProject().getObjects().directoryProperty(); // unchecked warning
    this.requires = getProject().getObjects().directoryProperty();  // unchecked warning
    this.testDriverPackage = getProject().getObjects().property(String.class); // unchecked warning
  }

  @TaskAction public void daikonRun() {
    final TaskExecutorImpl executor = new TaskExecutorImpl();

    final DirectoryProperty buildDir = getProject()
        .getLayout()
        .getBuildDirectory();

    final Directory buildMainDir = buildDir.dir(Constants.PROJECT_MAIN_CLASS_DIR).get();
    final Directory buildTestDir = buildDir.dir(Constants.PROJECT_TEST_CLASS_DIR).get();

    final String testpath = getTestDriverPackage().get().replaceAll("\\.", Constants.FILE_SEPARATOR);
    final File inputDir = buildTestDir.dir(testpath).getAsFile();

    final File dependenciesDir = getRequires()
        .getAsFile()
        .get();

    final List<File> classpath = new LinkedList<>(
        Filefinder.findJavaJars(dependenciesDir.toPath()));
    classpath.add(buildMainDir.getAsFile());
    classpath.add(buildTestDir.getAsFile());

    final File outputDir = getOutputDir()
        .getAsFile()
        .get();

    if (!Files.exists(outputDir.toPath())) {
      if (!outputDir.mkdir()) {
        throw new GradleException("Unable to create output directory");
      }
    }

    final RunDaikonConfiguration config = new RunDaikonConfiguration(inputDir, classpath,
        outputDir);
    getLogger().debug("Created RunDaikon task configuration");

    executor.install(config);
    getLogger().debug("Configured RunDaikon task");

    getLogger().debug("About to execute task");
    executor.execute();

    getLogger().debug("Successfully executed task");

    getLogger().quiet(Constants.SUCCESSFUL_EXECUTION);
  }

  @OutputDirectory public DirectoryProperty getOutputDir() {
    return this.outputDir;
  }

  @Input public Property<String> getTestDriverPackage() {
    return this.testDriverPackage;
  }

  @InputDirectory public DirectoryProperty getRequires() {
    return this.requires;
  }

  @Override protected String getTaskName() {
    return Constants.DAIKON_TASK;
  }

  @Override protected String getTaskDescription() {
    return Constants.DAIKON_TASK_DESCRIPTION;
  }

  static class RunDaikonConfiguration extends AbstractConfiguration {

    private final File inputDir;
    private final List<File> classpath;
    private final File outputDir;

    RunDaikonConfiguration(File inputDir, List<File> classpath, File outputDir) {
      this.inputDir = inputDir;
      this.classpath = classpath;
      this.outputDir = outputDir;
    }

    @Override protected void configure() {
      runDaikonOn(inputDir)
          .withClasspath(classpath)
          .toDir(outputDir);
    }
  }

  static abstract class AbstractConfiguration implements TaskConfiguration {

    TaskExecutor executor;

    @Override public final synchronized void configure(TaskExecutor executor) {
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

    /**
     * @see TaskExecutor#runDaikonOn(File)
     */
    protected TaskBuilder runDaikonOn(File testClassesDir) {
      return executor.runDaikonOn(testClassesDir);
    }

    /**
     * @see TaskExecutor#addError(Throwable)
     */
    protected void addError(Throwable t) {
      executor.addError(t);
    }
  }
}
