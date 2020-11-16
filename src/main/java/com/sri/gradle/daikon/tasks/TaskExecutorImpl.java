package com.sri.gradle.daikon.tasks;

import com.google.common.base.Preconditions;
import com.sri.gradle.daikon.Constants;
import com.sri.gradle.daikon.internal.MainExecutor;
import com.sri.gradle.daikon.utils.Filefinder;
import com.sri.gradle.daikon.utils.MoreFiles;
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
    Preconditions.checkArgument(
        provider != null
            && (provider.size() == 2 || provider.size() == 3));

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
        Filefinder.findJavaClasses(
            classesDir,
            "$" /*exclude those that contain this symbol*/);

    final List<String> allClassNames =
        MoreFiles.getTestClassNames(allTestClasses);

    // Let's assume there should be only one test driver.
    String mainClass =
        allClassNames
            .stream()
            .filter(Constants.EXPECTED_JUNIT4_NAME_REGEX.asPredicate())
            .filter(f -> f.endsWith(Constants.TEST_DRIVER))
            .findFirst()
            .orElse(null);

    if (mainClass == null) {
      throw new TaskConfigurationError(
          "DynComp/Chicory operations require a non-null main class.");
    }

    mainClass = mainClass.endsWith(".class")
        ? mainClass.replace(".class", "")
        : mainClass;

    final String prefix = mainClass.substring(mainClass.lastIndexOf('.') + 1);

    // Use Gradle's Executor and ExecSpec pattern. This the replacement
    // for the Command object and its built Java programs; e.g., Daikon, Chicory, DynComp
    final MainExecutor mainExecutor = new MainExecutor(each.getGradleProject());

    // DynComp
    mainExecutor.execDynComp(
        classpath,
        allClassNames,
        mainClass,
        classesDir,
        outputDir
    );

    // Chicory
    mainExecutor.execChicory(
        classpath,
        allClassNames,
        mainClass,
        prefix,
        outputDir
    );

    // Daikon
    mainExecutor.execDaikon(
        prefix,
        classpath,
        outputDir
    );

    // Print invariants
    mainExecutor.execPrintDaikonXml(
        classpath,
        prefix,
        outputDir
    );

    System.out.println(Constants.SUCCESSFUL_DAIKON_EXECUTION);
  }

}
