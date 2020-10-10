package com.sri.gradle.internal;

import static java.util.Arrays.stream;

import com.sri.gradle.utils.Command;
import com.sri.gradle.utils.Urls;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractTool implements Tool {

  private final Command.Builder builder;

  private final List<URL> classpathUrls = new LinkedList<>();
  private Object[] args = new Object[0];

  public AbstractTool() {
    this.builder = Command.create()
        .arguments("java")
        .arguments("-Xmx4G")
        .permitNonZeroExitStatus();
  }

  public Object[] getArgs() {
    return args;
  }

  @Override
  public void args(Object... args) {
    this.args = Stream.concat(
        stream(this.args), stream(args)).toArray(Object[]::new);
  }

  @Override
  public Tool addToolJarToClasspath(File toolJar) {
    getClasspath().add(Urls.toURL(toolJar.getAbsolutePath()));
    return this;
  }

  @Override
  public Tool setClasspath(List<URL> classpathUrls) {
    this.classpathUrls.clear();
    this.classpathUrls.addAll(classpathUrls);
    return this;
  }

  public List<URL> getClasspath() {
    return classpathUrls;
  }

  public Command.Builder getBuilder() {
    return builder;
  }

  @Override
  public Tool setWorkingDirectory(Path directory) {
    builder.workingDirectory(directory.toFile());
    return this;
  }
}
