package com.sri.gradle.internal;

import com.sri.gradle.Constants;
import com.sri.gradle.utils.Command;
import com.sri.gradle.utils.Command.Builder;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Javac {

  private final Builder builder;
  private boolean debugMode;

  public Javac() {
    this("javac");
  }

  /**
   * Creates a new Javac command.
   *
   * @param cmd the name of the command.
   */
  public Javac(String cmd) {
    this.builder = Command.create();

    builder.arguments(cmd);
    this.debugMode = false;
  }


  public Javac bootClasspath(List<URL> classpath) {
    builder.arguments("-bootclasspath", classpath.toString());
    return this;
  }

  public Javac classpath(File... path) {
    return classpath(Arrays.asList(path));
  }

  public Javac classpath(Collection<File> classpath) {
    builder.arguments("-classpath", Command.joinCollection(Constants.PATH_SEPARATOR, classpath));
    return this;
  }

  public List<String> compile(File... files) {
    return compile(Arrays.asList(files));
  }

  public List<String> compile(Collection<File> files) {
    if (files == null || files.contains(null)) {
      throw new IllegalArgumentException(
          "Error: either null collection or null values in collection"
      );
    }

    final Object[] args = files.stream()
        .map(File::toString)
        .toArray(Object[]::new);

    return builder.arguments(args).execute();
  }

  public Javac debug() {
    builder.arguments("-g");
    debugMode = true;
    return this;
  }

  public Javac destination(File directory) {
    builder.arguments("-d", directory.toString());
    return this;
  }

  public Javac extraArgs(List<String> extra) {
    builder.arguments(extra);
    return this;
  }

  public boolean inDebugMode() {
    return debugMode;
  }

  public Javac sourcePath(File... path) {
    return sourcePath(Arrays.asList(path));
  }


  public Javac sourcePath(Collection<File> path) {
    builder.arguments("-sourcepath", Command.joinCollection(Constants.PATH_SEPARATOR, path));
    return this;
  }

  public List<String> version() {
    return builder.arguments("-version").execute();
  }

  public Javac workingDirectory(File localWorkingDirectory) {
    this.builder.workingDirectory(localWorkingDirectory);
    return this;
  }

  @Override public String toString() {
    return builder.toString();
  }
}