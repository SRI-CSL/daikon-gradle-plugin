package com.sri.gradle.internal;

import com.sri.gradle.Constants;
import com.sri.gradle.utils.ImmutableStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DynComp extends AbstractTool {
  public DynComp(){
    super();
  }

  public DynComp setOutputDirectory(Path directory) {
    args(String.format("--output_dir=%s", directory));
    return this;
  }

  public DynComp setMainClass(String name){
    if(name == null || name.isEmpty())
      return this;

    args(name);
    return this;
  }

  @Override public DynComp setWorkingDirectory(Path directory) {
    return (DynComp) super.setWorkingDirectory(directory);
  }

  @Override public DynComp setToolJar(File toolJar){
    return (DynComp) super.setToolJar(toolJar);
  }

  @Override public DynComp setClasspath(List<URL> classpathUrls) {
    return (DynComp) super.setClasspath(classpathUrls);
  }

  public DynComp selectedClasses(List<String> fullyQualifiedClassNames) {
    return (DynComp) setSelectPatterns(fullyQualifiedClassNames);
  }

  public DynComp omittedClasses(List<String> fullyQualifiedClassNames) {
    return (DynComp) setOmitPatterns(fullyQualifiedClassNames);
  }

  @Override public void execute() throws ToolException {
    try {
      final String classPath = getClasspath().stream()
          .map(URL::toString)
          .collect(Collectors.joining(File.pathSeparator));

      List<String> output = getBuilder()
          .arguments("-classpath", classPath)
          .arguments(Constants.DYN_COMP_MAIN_CLASS)
          .arguments(getArgs())
          .execute();

      List<String> err = ImmutableStream.listCopyOf(output.stream()
          .filter(Objects::nonNull)
          .filter(s -> s.startsWith(Constants.ERROR_MARKER)));

      if (!err.isEmpty()) throw new ToolException(Constants.BAD_DAIKON_ERROR);
    } catch (Exception e){
      throw new ToolException(Constants.BAD_DAIKON_ERROR, e);
    }
  }
}
