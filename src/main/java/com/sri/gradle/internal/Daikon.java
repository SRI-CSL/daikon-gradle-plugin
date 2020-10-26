package com.sri.gradle.internal;

import com.sri.gradle.Constants;
import com.sri.gradle.utils.Command;
import com.sri.gradle.utils.ImmutableStream;
import java.util.List;
import java.util.Objects;

public class Daikon extends JavaProgram implements DaikonProgram {
  public Daikon() {
    super();
  }

  @Override
  public void execute() throws JavaProgramException {
    try {
      final String classPath = Command.joinCollection(Constants.PATH_SEPARATOR, getClasspath());

      List<String> output =
          getBuilder()
              .arguments("-classpath", classPath)
              .arguments(Constants.DAIKON_MAIN_CLASS)
              .arguments(getArgs())
              .execute();

      throwJavaProgramExceptionIfErrorsFound(output);

    } catch (RuntimeException e) {
      throw new JavaProgramException(Constants.BAD_DAIKON_ERROR, e);
    }
  }

  @Override protected void throwJavaProgramExceptionIfErrorsFound(List<String> output)
      throws JavaProgramException {
    List<String> err =
        ImmutableStream.listCopyOf(
            output
                .stream()
                .filter(Objects::nonNull)
                .filter(
                    s ->
                        s.startsWith(Constants.ERROR_MARKER)
                            || s.startsWith(Constants.DAIKON_SERIALIZATION_ERROR_MARKER)));

    if (!err.isEmpty()) {
      throw new JavaProgramException(
          Constants.BAD_DAIKON_ERROR + Constants.NEW_LINE + String.join(Constants.NEW_LINE, err));
    }
  }
}
