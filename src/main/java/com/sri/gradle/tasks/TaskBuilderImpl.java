package com.sri.gradle.tasks;

import com.sri.gradle.utils.Urls;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class TaskBuilderImpl implements TaskBuilder, OutputBuilder {

  private final TaskExecutorImpl executor;

  private final Path testClassesDir;
  private Path outputDir;

  private final List<URL> classpathUrls;

  public TaskBuilderImpl(Path testClassesDir, TaskExecutorImpl executor) {
    this.executor = executor;
    this.testClassesDir = testClassesDir;
    this.outputDir = null;
    this.classpathUrls = new LinkedList<>();
  }

  public Path getTestClassesDir() {
    return testClassesDir;
  }

  public Path getOutputDir() {
    return outputDir;
  }

  public List<URL> getClasspath() {
    return classpathUrls;
  }

  @Override
  public void toDir(File outputDir) {

    if (getTestClassesDir() == null || !Files.exists(getTestClassesDir())) {
      executor.addError(new NullPointerException("input directory is null or does not exist"));
      return;
    }

    if (outputDir == null || !Files.exists(outputDir.toPath())) {
      executor.addError(new NullPointerException("output directory is null or does exist"));
      return;
    }

    if (this.outputDir == null) {
      this.outputDir = outputDir.toPath();
    }

    if (getClasspath().isEmpty()) {
      executor.addError(new IllegalArgumentException("classpath is empty"));
    }
  }

  @Override
  public OutputBuilder withClasspath(List<File> files) {
    for (File each : files) {
      if (each == null) continue;
      classpathUrls.add(Urls.toURL(each.getAbsolutePath()));
    }

    return this;
  }
}
