package com.sri.gradle;

public class Constants {
  public static final String ASSEMBLE_TASK = "assemble";
  public static final String CHECK_DAIKON_TASK = "daikonCheck";
  public static final String CHECK_DAIKON_TASK_DESCRIPTION = "Checks if Daikon is in your project's classpath.";
  public static final String CHICORY_JAR_FILE = "ChicoryPremain.jar";
  public static final String CHICORY_MAIN_CLASS = "daikon.Chicory";
  public static final String DAIKON_JAR_FILE = "daikon.jar";
  public static final String DAIKON_MAIN_CLASS = "daikon.Daikon";
  public static final String DAIKON_TASK = "daikonRun";
  public static final String DAIKON_TASK_DESCRIPTION = "Runs Daikon invariant detector";
  public static final String DYN_COMP_MAIN_CLASS = "daikon.DynComp";
  public static final String DYN_COMP_PRE_MAIN_JAR_FILE = "dcomp_premain.jar";
  public static final String DYN_COMP_RT_JAR_FILE = "dcomp_rt.jar";
  public static final String GROUP = "Daikon";
  public static final String PLUGIN_DESCRIPTION = "Discovery of likely program invariants using Daikon.";
  public static final String PLUGIN_EXTENSION = "runDaikon";
  public static final String PROJECT_LIB_DIR = "libs";
  public static final String PROJECT_MAIN_CLASS_DIR = "classes/java/main";
  public static final String PROJECT_TEST_CLASS_DIR = "classes/java/test";

  public static final String NEW_LINE = System.getProperty("line.separator");
  public static final String FILE_SEPARATOR = System.getProperty("file.separator");
  public static final String PATH_SEPARATOR = System.getProperty("path.separator");
  public static final String EMPTY_STRING = "";
  public static final String SPACE = " ";
  public static final String PERIOD = ".";

  public static final String TEST_DRIVER = "TestDriver";
  public static final String BAD_DAIKON_ERROR = "Unable to run Daikon. Are you sure daikon.jar is in your path?";
  public static final String ERROR_MARKER = "Error: Could not find or load main";
  public static final String UNEXPECTED_ERROR = "Daikon is not installed on this machine." + NEW_LINE +
      "For latest release, see: https://github.com/codespecs/daikon/releases";

  private Constants(){}
}
