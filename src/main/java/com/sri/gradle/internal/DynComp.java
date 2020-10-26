package com.sri.gradle.internal;

import com.sri.gradle.Constants;
import com.sri.gradle.utils.Command;
import java.util.List;

public class DynComp extends JavaProgram implements DynCompProgram {
  public DynComp() {
    super();
  }

  @Override
  public void execute() throws JavaProgramException {
    try {
      final String classPath = Command.joinCollection(Constants.PATH_SEPARATOR, getClasspath());

      List<String> output =
          getBuilder()
              .arguments("-classpath", classPath)
              .arguments(Constants.DYN_COMP_MAIN_CLASS)
              .arguments(getArgs())
              .execute();

      throwJavaProgramExceptionIfErrorsFound(output);
    } catch (RuntimeException e) {
      throw new JavaProgramException(Constants.BAD_DAIKON_ERROR, e);
    }
  }
}
