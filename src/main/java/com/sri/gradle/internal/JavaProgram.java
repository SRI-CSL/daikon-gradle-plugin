package com.sri.gradle.internal;

import static java.util.Arrays.stream;

import com.sri.gradle.utils.Command;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public abstract class JavaProgram implements Program {

  private final Command.Builder builder;

  private final List<File> classpath;
  private Object[] args = new Object[0];

  public JavaProgram() {
    this.builder = Command.create()
        .arguments("java")
        .arguments("-Xmx4G")
        .permitNonZeroExitStatus();

    this.classpath = new LinkedList<>();
  }

  public Object[] getArgs() {
    return args;
  }

  @Override public void args(Object... args) {
    this.args = Stream.concat(
        stream(this.args), stream(args)).toArray(Object[]::new);
  }

  @Override public Program setToolJar(File toolJar) {
    getClasspath().add(toolJar);
    return this;
  }

  @Override public Program setClasspath(Collection<File> aClasspath) {
    this.classpath.clear();
    this.classpath.addAll(aClasspath);
    return this;
  }

  public List<File> getClasspath() {
    return classpath;
  }

  public Command.Builder getBuilder() {
    return builder;
  }

  @Override public Program setWorkingDirectory(Path directory) {
    builder.workingDirectory(directory.toFile());
    return this;
  }
}
