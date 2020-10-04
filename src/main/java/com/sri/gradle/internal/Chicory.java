package com.sri.gradle.internal;

import com.sri.gradle.Constants;
import com.sri.gradle.utils.ImmutableStream;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Chicory extends AbstractTool {

  public Chicory() {
    super();
  }

  @Override public void execute() throws ToolException {
    try {
      final String classPath = getClasspath().stream()
          .map(URL::toString)
          .collect(Collectors.joining(File.pathSeparator));

      List<String> output = getBuilder()
          .arguments("-classpath", classPath)
          .arguments(Constants.CHICORY_MAIN_CLASS)
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
