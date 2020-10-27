package com.sri.gradle.internal;

import com.google.common.base.Preconditions;
import com.sri.gradle.Constants;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.compile.ForkOptions;
import org.gradle.process.JavaForkOptions;

public class BaseExecSpec {
  private final List<Action<JavaForkOptions>> configureFork = new ArrayList<>();
  private FileCollection classpath;
  private String main;
  private Object[] args = new Object[0];
  private File workingDirectory = Constants.USER_WORKING_DIR;

  public void args(Object... args) {
    this.args = Stream.concat(Arrays.stream(this.args), Arrays.stream(args))
        .toArray(Object[]::new);
  }

  public void forkOptions(Action<JavaForkOptions> configureFork) {
    this.configureFork.add(configureFork);
  }

  public Object[] getArgs() {
    return args;
  }

  public FileCollection getClasspath() {
    return classpath;
  }

  public List<Action<JavaForkOptions>> getConfigureFork() {
    return configureFork;
  }

  public String getMain() {
    return main;
  }

  public File getWorkingDir(){
    return workingDirectory;
  }

  public void setArgs(Object... args) {
    if (args == null) {
      this.args = new Object[0];
    } else {
      this.args = args;
    }
  }

  public void setClasspath(FileCollection classpath) {
    this.classpath = classpath;
  }

  public void setForkOptions(){
    final ForkOptions options = new ForkOptions();
    forkOptions(fork -> {
      fork.setWorkingDir(getWorkingDir());
      fork.setJvmArgs(options.getJvmArgs());
      fork.setMinHeapSize(options.getMemoryInitialSize());
      fork.setMaxHeapSize(options.getMemoryMaximumSize());
      fork.setDefaultCharacterEncoding(StandardCharsets.UTF_8.name());
    });
  }

  public void setMain(String main) {
    this.main = main;
  }

  private void setSelectPattern(String classnamePattern) {
    args("--ppt-select-pattern=" + classnamePattern);
  }

  private void setSelectPatterns(List<String> fullyQualifiedClassNamePatterns) {
    //noinspection Convert2streamapi
    for (String qualifiedName : fullyQualifiedClassNamePatterns) { // unchecked warning
      setSelectPattern(qualifiedName);
    }
  }

  public void setSelectedClasses(String... fullyQualifiedClassNames) {
    setSelectPatterns(Arrays.asList(fullyQualifiedClassNames));
  }

  public void setSelectedClasses(List<String> fullyQualifiedClassNames) {
    setSelectPatterns(fullyQualifiedClassNames);
  }

  public void setTargetClass(String targetClass) {
    Preconditions.checkArgument(targetClass != null && !targetClass.isEmpty());
    args(targetClass);
  }


  public void setWorkingDir(Path workingDir){
    Objects.requireNonNull(workingDir);
    setWorkingDir(workingDir.toFile());
  }

  public void setWorkingDir(File workingDir){
    this.workingDirectory = workingDir;
  }

}
