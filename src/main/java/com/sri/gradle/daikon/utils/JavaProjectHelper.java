package com.sri.gradle.daikon.utils;

import com.google.common.collect.ImmutableSet;
import com.sri.gradle.daikon.Constants;
import java.io.File;
import java.util.List;
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
  private final Project project;

  public JavaProjectHelper(Project project){
    this.project = project;
  }

  public Project getProject(){
    return project;
  }

  public Set<File> getRuntimeClasspath(){
    return ImmutableSet.copyOf(getSourceSet("test", getProject()).getRuntimeClasspath().getFiles());
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

  public SourceSet getMainSourceSet() {
    return sourceSet(getProject(), SourceSet.MAIN_SOURCE_SET_NAME);
  }

  public SourceSet getTestSourceSet() {
    return sourceSet(getProject(), SourceSet.TEST_SOURCE_SET_NAME);
  }

  public static Task task(Project project, String taskName) {
    return project.getTasks().getByName(taskName);
  }

  public static Optional<Task> findTask(Project project, String taskName) {
    return Optional.ofNullable(project.getTasks().findByName(taskName));
  }

  public <T extends Task> T task(String taskName, Class<T> taskType) {
    return taskType.cast(task(getProject(), taskName));
  }

  public <T extends Task> Optional<T> findTask(
      String taskName, Class<T> taskType) {
    return findTask(getProject(), taskName).map(taskType::cast);
  }

  public Directory getProjectDir(){
    Objects.requireNonNull(getProject());
    return getProject().getLayout().getProjectDirectory();
  }

  public Directory getSrcMainDir(){
    Objects.requireNonNull(getProject());
    return getProjectDir().dir(Constants.PROJECT_MAIN_SRC_DIR);
  }

  public Directory getTestMainDir(){
    Objects.requireNonNull(getProject());
    return getProjectDir().dir(Constants.PROJECT_TEST_SRC_DIR);
  }

  public DirectoryProperty getBuildDir(){
    Objects.requireNonNull(getProject());
    return getProject().getLayout().getBuildDirectory();
  }

  public static DirectoryProperty getBuildDir(Project project) {
    Objects.requireNonNull(project);
    return project.getLayout().getBuildDirectory();
  }


  public Directory getBuildMainDir() {
    return getBuildDir().dir(Constants.PROJECT_MAIN_CLASS_DIR).get();
  }

  public static Directory getBuildMainDir(DirectoryProperty buildDir) {
    return buildDir.dir(Constants.PROJECT_MAIN_CLASS_DIR).get();
  }

  public Directory getBuildTestDir() {
    return getBuildDir().dir(Constants.PROJECT_TEST_CLASS_DIR).get();
  }


  public static Directory getBuildTestDir(DirectoryProperty buildDir) {
    return buildDir.dir(Constants.PROJECT_TEST_CLASS_DIR).get();
  }

  public static Directory getTestDriverPackageClassesDir(
      Property<String> testDriverPackage, Directory buildTestDir) {
    Objects.requireNonNull(buildTestDir);

    return getTestDriverPackageDir(testDriverPackage, buildTestDir);
  }

  public static SourceSet getSourceSet(String taskName, Project project) {
    Objects.requireNonNull(taskName);
    Objects.requireNonNull(project);
    return ((SourceSetContainer) project.getProperties().get("sourceSets")).getByName(taskName);
  }

  public static Directory getTestDriverPackageDir(
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

  public Optional<String> findDriverClass(){
    final List<File> javaDrivers = Filefinder.findJavaFiles(getDriverDir().toPath());
    return javaDrivers.stream()
        .map(f -> MoreFiles.getClassName(f.getAbsolutePath(), "driver", ".java"))
        .filter(Objects::nonNull)
        .filter(Constants.EXPECTED_JUNIT4_NAME_REGEX.asPredicate())
        .filter(f -> f.endsWith(Constants.TEST_DRIVER))
        .findFirst();
  }

  public File getDriverDir() {
    return new File(getProject().getBuildDir(), "driver");
  }

  public static File getDriverDir(Project project) {
    return new File(project.getBuildDir(), "driver");
  }

  public static File getDriverOutputDir(Project project, String testDriverPackage) {
    final String testpath = testDriverPackage.replaceAll("\\.", Constants.FILE_SEPARATOR);
    return getDriverDir(project).toPath().resolve(testpath).toFile();
  }

  public boolean hasProperty(String property){
    return property != null && !property.isEmpty() && getProject().hasProperty(property);
  }
}
