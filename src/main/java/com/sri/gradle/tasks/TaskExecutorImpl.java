package com.sri.gradle.tasks;

import com.sri.gradle.Constants;
import com.sri.gradle.internal.Chicory;
import com.sri.gradle.internal.Daikon;
import com.sri.gradle.internal.DynComp;
import com.sri.gradle.internal.Tool;
import com.sri.gradle.utils.Filefinder;
import com.sri.gradle.utils.MoreFiles;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class TaskExecutorImpl implements TaskExecutor {

  private final List<Throwable> encounteredErrors;
  private final List<TaskBuilderImpl> workBuilders;

  public TaskExecutorImpl(){
    this.encounteredErrors = new LinkedList<>();
    this.workBuilders = new LinkedList<>();
  }

  @Override public void addError(Throwable cause) {
    if (cause != null){
      this.encounteredErrors.add(cause);
    }
  }

  @Override public TaskBuilder runDaikonOn(File testClassesDir) {
    final TaskBuilderImpl builder = new TaskBuilderImpl(testClassesDir.toPath(), this);
    workBuilders.add(builder);
    return builder;
  }

  @Override public void execute() throws TaskConfigurationError {

    // Blow up if we encountered errors.
    if (!encounteredErrors.isEmpty()) {
      throw new TaskConfigurationError(encounteredErrors);
    }

    for (TaskBuilderImpl each : workBuilders){
      // a work builder configures a work executor
      // by applying a task configuration to it.
      applyBuiltConfiguration(each);
    }
  }

  private static void applyBuiltConfiguration(TaskBuilderImpl each) {
    final Path classesDir = each.getTestClassesDir();
    final Path outputDir = each.getOutputDir();
    final List<URL> classpath = each.getClasspath();

    final List<File>    allTestClasses  = Filefinder.findJavaClasses(classesDir);
    final List<String>  allQualifiedClasses = MoreFiles.getFullyQualifiedNames(allTestClasses);

    String mainClass  = allQualifiedClasses.stream()
        .filter(f -> f.endsWith(Constants.TEST_DRIVER))
        .findFirst().orElse(null);

    if(mainClass == null){
      System.out.println("Not main class for DynComp operation");
      return;
    }

    mainClass = mainClass.replace(".class", Constants.EMPTY_STRING);

    final String prefix = mainClass.substring(mainClass.lastIndexOf('.') + 1);

    executeDynComp(mainClass, allQualifiedClasses, classpath, outputDir);
    executeChicory(mainClass, prefix, allQualifiedClasses, classpath, outputDir);
    executeDaikon(mainClass, prefix, classpath, outputDir);
  }

  private static void executeDaikon(String mainClass, String namePrefix, List<URL> classpath, Path outputDir) {
    final Tool daikon = new Daikon()
        .setClasspath(classpath)
        .setWorkingDirectory(outputDir)
        .setMainClass(mainClass)
        .setDtraceFile(outputDir, namePrefix + ".dtrace.gz")
        .setStandardOutput(namePrefix + ".inv.gz");

    daikon.execute();
  }

  private static void executeChicory(String mainClass, String namePrefix, List<String> allQualifiedClasses,
      List<URL> classpath, Path outputDir) {
    final Tool chicory = new Chicory()
        .setClasspath(classpath)
        .setWorkingDirectory(outputDir)
        .setMainClass(mainClass)
        .setSelectedClasses(allQualifiedClasses)
        .setOutputDirectory(outputDir)
        .setComparabilityFile(outputDir, namePrefix + ".decls-DynComp");

    chicory.execute();
  }

  private static void executeDynComp(String mainClass, List<String> allQualifiedClasses,
      List<URL> classpath, Path outputDir) {
    final Tool dynComp = new DynComp()
        .setClasspath(classpath)
        .setWorkingDirectory(outputDir)
        .setMainClass(mainClass)
        .selectedClasses(allQualifiedClasses)
        .setOutputDirectory(outputDir);

    dynComp.execute();
  }

}
