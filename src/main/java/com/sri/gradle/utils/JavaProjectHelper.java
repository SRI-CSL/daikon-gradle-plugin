package com.sri.gradle.utils;

import com.google.common.collect.ImmutableSet;
import com.sri.gradle.Constants;
import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

public class JavaProjectHelper {
  private JavaProjectHelper() {
    throw new Error("Cannot be instantiated");
  }

  public static Set<File> getRuntimeClasspath(Project project) {
    // HACK. needed the test's runtime classpath to compile the test driver.
    // This classpath is different than the one the Daikon tool needs.
    // TODO(has) to find a better way to get this classpath.
    return ImmutableSet.copyOf(getSourceSet("test", project).getRuntimeClasspath().getFiles());
  }

  public static <T> T extension(Project project, Class<T> extensionType) {
    return project.getExtensions().getByType(extensionType);
  }

  public static <T> T extension(Project project, String name, Class<T> extensionType) {
    return extensionType.cast(project.getExtensions().getByName(name));
  }

  public static SourceSetContainer sourceSets(Project project) {
    return extension(project, SourceSetContainer.class);
  }

  public static SourceSet sourceSet(Project project, String sourceSetName) {
    return sourceSets(project).getByName(sourceSetName);
  }

  public static SourceSet mainSourceSet(Project project) {
    return sourceSet(project, SourceSet.MAIN_SOURCE_SET_NAME);
  }

  public static SourceSet testSourceSet(Project project) {
    return sourceSet(project, SourceSet.TEST_SOURCE_SET_NAME);
  }

  public static Task task(Project project, String taskName) {
    return project.getTasks().getByName(taskName);
  }

  public static Optional<Task> findTask(Project project, String taskName) {
    return Optional.ofNullable(project.getTasks().findByName(taskName));
  }

  public static <T extends Task> T task(Project project, String taskName, Class<T> taskType) {
    return taskType.cast(task(project, taskName));
  }

  public static <T extends Task> Optional<T> findTask(
      Project project, String taskName, Class<T> taskType) {
    return findTask(project, taskName).map(taskType::cast);
  }

  public static DirectoryProperty getBuildDir(Project project) {
    Objects.requireNonNull(project);
    return project.getLayout().getBuildDirectory();
  }

  public static Directory getBuildMainDir(DirectoryProperty buildDir) {
    return buildDir.dir(Constants.PROJECT_MAIN_CLASS_DIR).get();
  }

  public static Directory getBuildTestDir(DirectoryProperty buildDir) {
    return buildDir.dir(Constants.PROJECT_TEST_CLASS_DIR).get();
  }

  public static Directory getTestClassesDir(
      Property<String> testDriverPackage, Directory buildTestDir) {
    Objects.requireNonNull(buildTestDir);

    return getTestDriverOutputDir(testDriverPackage, buildTestDir);
  }

  public static Directory getTestClassesDir(Property<String> testDriverPackage, Project project) {
    return getTestClassesDir(testDriverPackage, getBuildTestDir(getBuildDir(project)));
  }

  public static SourceSet getSourceSet(String taskName, Project project) {
    Objects.requireNonNull(taskName);
    Objects.requireNonNull(project);
    return ((SourceSetContainer) project.getProperties().get("sourceSets")).getByName(taskName);
  }

  public static Directory getTestDriverOutputDir(
      Property<String> testDriverPackage, Directory outputDir) {
    Objects.requireNonNull(testDriverPackage);
    Objects.requireNonNull(testDriverPackage.get());
    Objects.requireNonNull(outputDir);

    if (testDriverPackage.get().isEmpty()) {
      return null;
    }

    final String testpath = testDriverPackage.get().replaceAll("\\.", Constants.FILE_SEPARATOR);
    return outputDir.dir(testpath);
  }

  public static File getDriverDir(Project project) {
    return new File(project.getBuildDir(), "driver");
  }

  public static File getDriverOutputDir(Project project, String testDriverPackage) {
    final String testpath = testDriverPackage.replaceAll("\\.", Constants.FILE_SEPARATOR);
    return getDriverDir(project).toPath().resolve(testpath).toFile();
  }
}
