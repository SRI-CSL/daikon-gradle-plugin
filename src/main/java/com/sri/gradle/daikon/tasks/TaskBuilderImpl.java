package com.sri.gradle.daikon.tasks;

import com.google.common.base.Predicates;
import com.sri.gradle.daikon.utils.JavaProjectHelper;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.gradle.api.Project;

public class TaskBuilderImpl implements TaskBuilder, OutputBuilder {

  private final TaskExecutorImpl executor;

  private final File testClassesDir;
  private Path outputDir;
  private final String testDriverPackage;

  private final JavaProjectHelper projectHelper;

  private final List<File> classpath;

  public TaskBuilderImpl(InputProvider provider, TaskExecutorImpl executor) {
    final File testClassesDir =
        Arrays.stream(provider.get())
            .filter(Predicates.instanceOf(File.class))
            .map(o -> (File) o)
            .findFirst()
            .orElse(null);

    // TODO(has) consider removing this testDriverPackage from the TaskBuilder
    final String testDriverPackage =
        Arrays.stream(provider.get())
            .filter(Predicates.instanceOf(String.class))
            .map(Object::toString)
            .findFirst()
            .orElse(null);

    final Project gradleProject =
        Arrays.stream(provider.get())
            .filter(Predicates.instanceOf(Project.class))
            .map(o -> (Project) o)
            .findFirst()
            .orElse(null);

    this.executor = executor;
    this.testClassesDir = testClassesDir;
    this.outputDir = null;
    this.classpath = new LinkedList<>();
    // TODO(has) consider removing this
    this.testDriverPackage = testDriverPackage;
    this.projectHelper = new JavaProjectHelper(gradleProject);
  }

  public List<File> getClasspath() {
    return classpath;
  }

  public Project getGradleProject() {
    return getProjectHelper().getProject();
  }

  public JavaProjectHelper getProjectHelper(){
    return projectHelper;
  }

  public Path getOutputDir() {
    return outputDir;
  }

  public File getTestClassesDir() {
    return testClassesDir;
  }

  public String getTestDriverPackage() {
    return testDriverPackage;
  }

  @Override
  public void toDir(File outputDir) {

    if (getTestClassesDir() == null) {
      executor.addError(new NullPointerException("input directory is null"));
      return;
    }

    if (!Files.exists(getTestClassesDir().toPath())) {
      executor.addError(new Error("input directory " + getTestClassesDir() + " does not exist"));
      return;
    }

    if (outputDir == null) {
      executor.addError(new NullPointerException("output directory is null"));
      return;
    }

    if (!Files.exists(outputDir.toPath())) {
      executor.addError(new Error("output directory " + outputDir + " does not exist"));
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
      classpath.add(each);
    }

    classpath.addAll(getProjectHelper().getRuntimeClasspath());

    return this;
  }
}
