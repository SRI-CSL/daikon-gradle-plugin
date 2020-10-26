package com.sri.gradle.tasks;

import com.google.common.base.Preconditions;
import com.sri.gradle.Constants;
import com.sri.gradle.internal.Chicory;
import com.sri.gradle.internal.Daikon;
import com.sri.gradle.internal.DynComp;
import com.sri.gradle.internal.Program;
import com.sri.gradle.utils.Filefinder;
import com.sri.gradle.utils.MoreFiles;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class TaskExecutorImpl implements TaskExecutor {

  private final List<Throwable> encounteredErrors;
  private final List<TaskBuilderImpl> workBuilders;

  public TaskExecutorImpl() {
    this.encounteredErrors = new LinkedList<>();
    this.workBuilders = new LinkedList<>();
  }

  @Override
  public void addError(Throwable cause) {
    if (cause != null) {
      this.encounteredErrors.add(cause);
    }
  }

  @Override
  public TaskBuilder runDaikonOn(InputProvider provider) {
    Preconditions.checkArgument(provider != null);
    Preconditions.checkArgument(provider.size() == 2 || provider.size() == 3);

    final TaskBuilderImpl builder = new TaskBuilderImpl(provider, this);
    workBuilders.add(builder);
    return builder;
  }

  @Override
  public void execute() throws TaskConfigurationError {

    // Blow up if we encountered errors.
    if (!encounteredErrors.isEmpty()) {
      throw new TaskConfigurationError(encounteredErrors);
    }

    for (TaskBuilderImpl each : workBuilders) {
      // a work builder configures a work executor
      // by applying a task configuration to it.
      applyBuiltConfiguration(each);
    }
  }

  private static void applyBuiltConfiguration(TaskBuilderImpl each) {
    final Path classesDir = each.getTestClassesDir().toPath();
    final Path outputDir = each.getOutputDir();

    each.getGradleProject()
        .getLogger()
        .debug(
            "Output directory "
                + outputDir
                + " is "
                + (Files.isWritable(outputDir) ? "writable." : "not writable."));

    final List<File> classpath = each.getClasspath();

    final List<File> allTestClasses =
        Filefinder.findJavaClasses(classesDir, "$" /*exclude those that contain this symbol*/);
    final List<String> allClassnames = MoreFiles.getClassNames(allTestClasses);

    // TODO(has) Some projects may have more than one test driver. Is there
    //  a better way to handle the case of multiple test drivers?
    String mainClass =
        allClassnames
            .stream()
            .filter(Constants.EXPECTED_JUNIT4_NAME_REGEX.asPredicate())
            .filter(f -> f.endsWith(Constants.TEST_DRIVER))
            .findFirst()
            .orElse(null);

    if (mainClass == null) {
      each.getGradleProject().getLogger().debug("Not main class for DynComp operation");
      return;
    }

    mainClass = mainClass.endsWith(".class")
        ? mainClass.replace(".class", "")
        : mainClass;

    final String prefix = mainClass.substring(mainClass.lastIndexOf('.') + 1);

    executeDynComp(mainClass, allClassnames, classpath, classesDir, outputDir);
    executeChicory(mainClass, prefix, allClassnames, classpath, outputDir);
    executeDaikon(prefix, classpath, outputDir);
  }

  private static void executeDaikon(
      String namePrefix,
      List<File> classpath,
      Path outputDir) {

    final Program daikon =
        new Daikon()
            .setDtraceFile(namePrefix + ".dtrace.gz")
            .setStandardOutput(namePrefix + ".inv.gz")
            .setClasspath(classpath)
            .setWorkingDirectory(outputDir);

    daikon.execute();
  }

  private static void executeChicory(
      String mainClass,
      String namePrefix,
      List<String> allQualifiedClasses,
      List<File> classpath,
      Path outputDir) {

    final Program chicory =
        new Chicory()
            .setComparabilityFile(outputDir, namePrefix + ".decls-DynComp")
            .setClasspath(classpath)
            .setWorkingDirectory(outputDir)
            .setMainClass(mainClass)
            .setSelectedClasses(allQualifiedClasses);

    chicory.execute();
  }

  private static void executeDynComp(
      String mainClass,
      List<String> allQualifiedClasses,
      List<File> classpath,
      Path testClassDir,
      Path outputDir) {

    final Program dynComp =
        new DynComp()
            .setOutputDirectory(outputDir)
            .setClasspath(classpath)
            .setWorkingDirectory(testClassDir)
            .setMainClass(mainClass)
            .setSelectedClasses(allQualifiedClasses);

    dynComp.execute();
  }
}
