package com.sri.gradle.internal;

import com.sri.gradle.Constants;
import com.sri.gradle.utils.ImmutableStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Daikon extends AbstractTool {
  public Daikon() {
    super();
  }

  @Override public void execute() throws ToolException {
    try {
      final String classPath = getClasspath().stream()
          .map(URL::toString)
          .collect(Collectors.joining(File.pathSeparator));

      List<String> output = getBuilder()
          .arguments("-classpath", classPath)
          .arguments(Constants.DAIKON_MAIN_CLASS)
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
