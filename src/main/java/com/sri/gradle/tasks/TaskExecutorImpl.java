package com.sri.gradle.tasks;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.sri.gradle.Constants;
import com.sri.gradle.internal.Chicory;
import com.sri.gradle.internal.Daikon;
import com.sri.gradle.internal.DynComp;
import com.sri.gradle.internal.GenerateTestDriver;
import com.sri.gradle.internal.Program;
import com.sri.gradle.utils.Filefinder;
import com.sri.gradle.utils.MoreFiles;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.gradle.api.Project;

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

  @Override public TaskBuilder runDaikonOn(InputProvider provider) {
    Preconditions.checkArgument(provider != null);
    Preconditions.checkArgument(provider.size() == 2 || provider.size() == 3);

    final TaskBuilderImpl builder = new TaskBuilderImpl(provider, this);
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
    final Path classesDir = each.getTestClassesDir().toPath();
    final Path outputDir = each.getOutputDir();
    // collects all files and directories that need to be in our classpath
    // e.g., files in `requires` directory and the plugin's runtime classpath
    final Set<File> classpath = ImmutableSet.copyOf(each.getClasspath());

    final List<File>    allTestClasses  = Filefinder.findJavaClasses(classesDir, "$"/*exclude those that contain this symbol*/);
    final List<String>  allQualifiedClasses = MoreFiles.getFullyQualifiedNames(allTestClasses);

    // TODO(has) Consider changing this. Some projects may have
    //  more than one test driver.
    String mainClass  = allQualifiedClasses.stream()
        .filter(Constants.EXPECTED_JUNIT4_NAME_REGEX.asPredicate())
        .filter(f -> f.endsWith(Constants.TEST_DRIVER))
        .findFirst().orElse(null);

    if (each.getTestDriverPackage() != null) {
      final GenerateTestDriver generateTestDriver = new GenerateTestDriver(
          each.getGradleProject(),
          each.getTestDriverPackage(),
          allQualifiedClasses
      );

      if (!generateTestDriver.writeTo(outputDir.toFile())){
        println(each.getGradleProject(), "Unable to generate or compile the new TestDriver class");
        return;
      }

      mainClass = each.getTestDriverPackage() + Constants.PERIOD + Constants.TEST_DRIVER_CLASSNAME;

    }

    if(mainClass == null){
      println(each.getGradleProject(), "Not main class for DynComp operation");
      return;
    }

    mainClass = mainClass.replace(".class", Constants.EMPTY_STRING);

    final String prefix = mainClass.substring(mainClass.lastIndexOf('.') + 1);

    executeDynComp(mainClass, allQualifiedClasses, classpath, classesDir, outputDir);
    executeChicory(mainClass, prefix, allQualifiedClasses, classpath, classesDir, outputDir);
    executeDaikon(mainClass, prefix, classpath, classesDir, outputDir);
  }

  private static void executeDaikon(String mainClass, String namePrefix, Collection<File> classpath,
      Path testClassDir, Path outputDir) {
    final Program daikon = new Daikon()
        .setClasspath(classpath)
        .setWorkingDirectory(testClassDir)
        .setMainClass(mainClass)
        .setDtraceFile(outputDir, namePrefix + ".dtrace.gz")
        .setStandardOutput(outputDir.resolve(namePrefix + ".inv.gz").toFile().getAbsolutePath());

    daikon.execute();
  }

  private static void executeChicory(String mainClass, String namePrefix, List<String> allQualifiedClasses,
      Collection<File> classpath, Path testClassDir, Path outputDir) {
    final Program chicory = new Chicory()
        .setClasspath(classpath)
        .setWorkingDirectory(outputDir)
        .setMainClass(mainClass)
        .setSelectedClasses(allQualifiedClasses)
        // TODO(has) seems that workingDirectory is affecting the output setting behavior.
//        .setOutputDirectory(outputDir)
        .setComparabilityFile(outputDir, namePrefix + ".decls-DynComp");

    chicory.execute();
  }

  private static void executeDynComp(String mainClass, List<String> allQualifiedClasses,
      Collection<File> classpath, Path testClassDir, Path outputDir) {
    final Program dynComp = new DynComp()
        .setClasspath(classpath)
        .setWorkingDirectory(testClassDir)
        .setMainClass(mainClass)
        .setSelectedClasses(allQualifiedClasses)
        .setOutputDirectory(outputDir);

    dynComp.execute();
  }


  private static void println(Project project, String message){
    if (project != null){
      project.getLogger().debug(message);
    } else {
      System.out.println(message);
    }
  }

}
