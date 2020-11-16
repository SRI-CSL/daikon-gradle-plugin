package com.sri.gradle.daikon.internal;

import com.sri.gradle.daikon.Constants;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;

public class MainExecutor {
  private final Project project;

  public MainExecutor(Project project){
    this.project = project;
  }

  public void execChicory(List<File> classPath, List<String> allClassnames, String targetClass, String classNamePrefix, Path outputDir){
    execChicory(new ChicoryExecSpecAction(
        project,
        classPath,
        allClassnames,
        targetClass,
        classNamePrefix,
        outputDir
    ));
  }

  public void execChicory(Action<ChicoryExecSpec> action){
    Objects.requireNonNull(action);
    final ChicoryExecSpec spec = new ChicoryExecSpec();
    action.execute(spec);
    execChicory(spec);
  }

  public void execChicory(ChicoryExecSpec chicorySpec){
    Objects.requireNonNull(chicorySpec);
    final FileCollection fullClasspath = chicorySpec.getClasspath();
    project.javaexec(spec -> {
      spec.setStandardOutput(Constants.QUIET_OUTPUT);
      spec.setWorkingDir(chicorySpec.getWorkingDir());
      spec.setClasspath(fullClasspath);
      spec.setMain(Constants.CHICORY_MAIN_CLASS);
      spec.setArgs(Arrays.asList(chicorySpec.getArgs()));
      chicorySpec.getConfigureFork().forEach(forkAction -> forkAction.execute(spec));
    });
  }

  public void execDaikon(String classNamePrefix, List<File> classPath, Path outputDir){
    execDaikon(new DaikonExecSpecAction(
        project,
        classNamePrefix,
        classPath,
        outputDir
    ));
  }

  public void execDaikon(Action<DaikonExecSpec> action){
    Objects.requireNonNull(action);
    final DaikonExecSpec spec = new DaikonExecSpec();
    action.execute(spec);
    execDaikon(spec);
  }

  public void execDaikon(DaikonExecSpec daikonSpec){
    Objects.requireNonNull(daikonSpec);
    final FileCollection fullClasspath = daikonSpec.getClasspath();
    project.javaexec(spec -> {
      spec.setStandardOutput(Constants.QUIET_OUTPUT);
      spec.setWorkingDir(daikonSpec.getWorkingDir());
      spec.setClasspath(fullClasspath);
      spec.setMain(Constants.DAIKON_MAIN_CLASS);
      spec.setArgs(Arrays.asList(daikonSpec.getArgs()));
      daikonSpec.getConfigureFork().forEach(forkAction -> forkAction.execute(spec));
    });
  }

  public void execPrintDaikonXml(List<File> classPath, String classNamePrefix, Path outputDir){
    execPrintDaikonXml(new PrintInvariantsExecSpecAction(
        project,
        classPath,
        classNamePrefix,
        outputDir
    ));
  }

  public void execPrintDaikonXml(Action<PrintInvariantsExecSpec> action){
    Objects.requireNonNull(action);
    final PrintInvariantsExecSpec spec = new PrintInvariantsExecSpec();
    action.execute(spec);
    execPrintDaikonXml(spec);
  }

  public void execPrintDaikonXml(PrintInvariantsExecSpec printSpec){
    Objects.requireNonNull(printSpec);
    final FileCollection fullClasspath = printSpec.getClasspath();
    project.javaexec(spec -> {
      spec.setStandardOutput(Constants.QUIET_OUTPUT);
      spec.setWorkingDir(printSpec.getWorkingDir());
      spec.setClasspath(fullClasspath);
      spec.setMain(Constants.PRINT_INVARIANTS_MAIN_CLASS);
      spec.setArgs(Arrays.asList(printSpec.getArgs()));
      printSpec.getConfigureFork().forEach(forkAction -> forkAction.execute(spec));
    });
  }

  public void execDynComp(List<File> classPath, List<String> allClassnames, String targetClass, Path testClassDir, Path outputDir){
    execDynComp(new DynCompExecSpecAction(
        project,
        classPath,
        allClassnames,
        targetClass,
        testClassDir,
        outputDir
    ));
  }

  public void execDynComp(Action<DynCompExecSpec> action){
    Objects.requireNonNull(action);
    final DynCompExecSpec spec = new DynCompExecSpec();
    action.execute(spec);
    execDynComp(spec);
  }

  public void execDynComp(DynCompExecSpec dynCompSpec){
    Objects.requireNonNull(dynCompSpec);
    final FileCollection fullClasspath = dynCompSpec.getClasspath();
    project.javaexec(spec -> {
      spec.setStandardOutput(Constants.QUIET_OUTPUT);
      spec.setWorkingDir(dynCompSpec.getWorkingDir());
      spec.setClasspath(fullClasspath);
      spec.setMain(Constants.DYN_COMP_MAIN_CLASS);
      spec.setArgs(Arrays.asList(dynCompSpec.getArgs()));
      dynCompSpec.getConfigureFork().forEach(forkAction -> forkAction.execute(spec));
    });
  }


  static class DynCompExecSpecAction implements Action<DynCompExecSpec> {

    private final Project project;
    private final List<File> classPath;
    private final List<String> allClassnames;
    private final String targetClass;
    private final Path testClassDir;
    private final Path outputDir;

    public DynCompExecSpecAction(Project project, List<File> classPath, List<String> allClassnames, String targetClass, Path testClassDir, Path outputDir){
      this.project = project;
      this.classPath = classPath;
      this.allClassnames = allClassnames;
      this.targetClass = targetClass;
      this.testClassDir = testClassDir;
      this.outputDir = outputDir;
    }

    @Override public void execute(DynCompExecSpec spec) {
      spec.setWorkingDir(testClassDir);
      spec.setOutputDirectory(testClassDir.relativize(outputDir));
      spec.setClasspath(project.files(classPath));
      spec.setMain(Constants.DYN_COMP_MAIN_CLASS);
      spec.setSelectedClasses(allClassnames);
      spec.setTargetClass(targetClass);
      spec.setForkOptions();
    }
  }

  static class ChicoryExecSpecAction implements Action<ChicoryExecSpec> {

    private final Project project;
    private final List<File> classPath;
    private final List<String> allClassnames;
    private final String targetClass;
    private final String classNamePrefix;
    private final Path outputDir;

    public ChicoryExecSpecAction(Project project, List<File> classPath, List<String> allClassnames, String targetClass, String classNamePrefix, Path outputDir){
      this.project = project;
      this.classPath = classPath;
      this.allClassnames = allClassnames;
      this.targetClass = targetClass;
      this.classNamePrefix = classNamePrefix;
      this.outputDir = outputDir;
    }

    @Override public void execute(ChicoryExecSpec spec) {
      spec.setWorkingDir(outputDir);
      spec.setClasspath(project.files(classPath));
      spec.setMain(Constants.CHICORY_MAIN_CLASS);
      spec.setComparabilityFile(outputDir, classNamePrefix + ".decls-DynComp");
      spec.setTargetClass(targetClass);
      spec.setSelectedClasses(allClassnames);
      spec.setForkOptions();
    }
  }

  static class DaikonExecSpecAction implements Action<DaikonExecSpec> {

    private final Project project;
    private final String classNamePrefix;
    private final List<File> classPath;
    private final Path outputDir;

    DaikonExecSpecAction(Project project, String classNamePrefix, List<File> classPath, Path outputDir){
      this.project = project;
      this.classNamePrefix = classNamePrefix;
      this.classPath = classPath;
      this.outputDir = outputDir;
    }

    @Override public void execute(DaikonExecSpec spec) {
      spec.setWorkingDir(outputDir);
      spec.setClasspath(project.files(classPath));
      spec.setMain(Constants.DAIKON_MAIN_CLASS);
      spec.setDtraceFile(outputDir, classNamePrefix + ".dtrace.gz");
      spec.setStandardOutput(outputDir, classNamePrefix + ".inv.gz");
      spec.setForkOptions();
    }
  }

  static class PrintInvariantsExecSpecAction implements Action<PrintInvariantsExecSpec> {

    private final Project project;
    private final List<File> classPath;
    private final String classNamePrefix;
    private final Path outputDir;

    PrintInvariantsExecSpecAction(Project project, List<File> classPath, String classNamePrefix, Path outputDir){
      this.project = project;
      this.classPath = classPath;
      this.classNamePrefix = classNamePrefix;
      this.outputDir = outputDir;
    }

    @Override public void execute(PrintInvariantsExecSpec spec) {
      spec.setWorkingDir(outputDir);
      spec.setClasspath(project.files(classPath));
      spec.setMain(Constants.PRINT_INVARIANTS_MAIN_CLASS);
      spec.setXmlOutput(outputDir, classNamePrefix + ".inv.xml");
      spec.setWrapXml();
      spec.setInvariantsFile(outputDir, classNamePrefix + ".inv.gz");
    }
  }
}
