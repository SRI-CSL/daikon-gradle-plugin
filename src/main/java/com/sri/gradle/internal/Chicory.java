package com.sri.gradle.internal;

import com.sri.gradle.Constants;
import com.sri.gradle.utils.ImmutableStream;
import com.sri.gradle.utils.Urls;
import java.util.List;
import java.util.Objects;

public class Chicory extends JavaProgram {

  public Chicory() {
    super();
  }

  @Override
  public void execute() throws JavaProgramException {
    try {
      final String classPath = Urls.toURLStr(getClasspath());

      List<String> output = getBuilder()
          .arguments("-classpath", classPath)
          .arguments(Constants.CHICORY_MAIN_CLASS)
          .arguments(getArgs())
          .execute();

      List<String> err = ImmutableStream.listCopyOf(output.stream()
          .filter(Objects::nonNull)
          .filter(s -> s.startsWith(Constants.ERROR_MARKER)));

      if (!err.isEmpty()) throw new JavaProgramException(Constants.BAD_DAIKON_ERROR);

    } catch (Exception e) {
      throw new JavaProgramException(Constants.BAD_DAIKON_ERROR, e);
    }
  }
}
