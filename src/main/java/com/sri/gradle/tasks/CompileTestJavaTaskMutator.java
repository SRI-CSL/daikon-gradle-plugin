package com.sri.gradle.tasks;

import com.sri.gradle.Constants;
import com.sri.gradle.utils.JavaProjectHelper;
import com.sri.gradle.utils.RuntimeClasspath;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;

public class CompileTestJavaTaskMutator {

  private final Project project;
  private final FileCollection compileJavaClasspath;

  public CompileTestJavaTaskMutator(Project project, FileCollection compileJavaClasspath) {
    this.project = project;
    this.compileJavaClasspath = compileJavaClasspath;
  }

  public void mutateJavaCompileTask(JavaCompile javaCompile) {
    final Directory testClassesDir = JavaProjectHelper.getBuildTestDir(JavaProjectHelper.getBuildDir(project));
    List<String> compilerArgs = buildCompilerArgs(javaCompile);
    javaCompile.getOptions().setCompilerArgs(compilerArgs);
    Set<File> runtimeClasspath = RuntimeClasspath.getFiles(project);
    javaCompile.setClasspath(project.files(this.compileJavaClasspath, runtimeClasspath));
    javaCompile.setDestinationDir(testClassesDir.getAsFile());
    configureSourcepath(javaCompile);
  }

  // Setting the sourcepath is necessary when using forked compilation for module-info.java
  private void configureSourcepath(JavaCompile javaCompile) {
    SourceDirectorySet sds = javaCompile.getProject().getObjects().sourceDirectorySet("driver", "driver").srcDir(JavaProjectHelper.getDriverDir(javaCompile.getProject()));
    final SourceSet sourceSet = JavaProjectHelper.testSourceSet(project);
    final Set<File> newSourceSet = new HashSet<>(sourceSet.getJava().getSrcDirs());
    newSourceSet.add(JavaProjectHelper.getDriverDir(javaCompile.getProject()));
    newSourceSet.stream()
        .map(srcDir -> srcDir.toPath().resolve(Constants.TEST_DRIVER_CLASSNAME + ".java"))
        .filter(Files::exists)
        .findFirst()
        .ifPresent(path -> {
          javaCompile.setSource(sds);
          javaCompile.getOptions().setSourcepath(project.files(path.getParent()));
        });
  }

  private List<String> buildCompilerArgs(JavaCompile javaCompile) {
    return new ArrayList<>(javaCompile.getOptions().getCompilerArgs());
  }
}