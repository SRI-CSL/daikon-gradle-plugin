package com.sri.gradle.internal;

import static java.util.Arrays.stream;

import com.sri.gradle.Constants;
import com.sri.gradle.utils.Command;
import com.sri.gradle.utils.ImmutableStream;
import java.io.File;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class JavaProgram implements Program {

  private final Command.Builder builder;

  private final List<File> classpath = new LinkedList<>();
  private Object[] args = new Object[0];

  public JavaProgram() {
    this.builder = Command.create().arguments("java").arguments("-Xmx4G").permitNonZeroExitStatus();
  }

  public Object[] getArgs() {
    return args;
  }

  @Override
  public void args(Object... args) {
    this.args = Stream.concat(stream(this.args), stream(args)).toArray(Object[]::new);
  }

  @Override
  public Program addToolJarToClasspath(File toolJar) {
    getClasspath().add(toolJar);
    return this;
  }

  @Override
  public Program setClasspath(List<File> files) {
    this.classpath.clear();
    this.classpath.addAll(files);
    return this;
  }

  public List<File> getClasspath() {
    return classpath;
  }

  public Command.Builder getBuilder() {
    return builder;
  }

  @Override
  public Program setWorkingDirectory(Path directory) {
    builder.workingDirectory(directory.toFile());
    return this;
  }

  @Override public void execute() throws JavaProgramException {

  }

  protected void throwJavaProgramExceptionIfErrorsFound(List<String> output) throws JavaProgramException {
    List<String> capturedErrors =
        ImmutableStream.listCopyOf(
            output
                .stream()
                .filter(Objects::nonNull)
                .filter(s -> s.startsWith(Constants.ERROR_MARKER)));

    if (!capturedErrors.isEmpty()) throw new JavaProgramException(Constants.BAD_DAIKON_ERROR);
  }
}
