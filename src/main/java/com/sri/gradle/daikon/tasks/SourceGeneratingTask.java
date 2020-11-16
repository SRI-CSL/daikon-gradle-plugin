package com.sri.gradle.daikon.tasks;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.sri.gradle.daikon.Constants;
import com.sri.gradle.daikon.utils.Filefinder;
import com.sri.gradle.daikon.utils.ImmutableStream;
import com.sri.gradle.daikon.utils.JavaCode;
import com.sri.gradle.daikon.utils.JavaProjectHelper;
import com.sri.gradle.daikon.utils.MoreFiles;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.gradle.api.GradleException;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

public class SourceGeneratingTask extends AbstractNamedTask {
  // Read-only fields
  @Internal private final Property<String> testDriverPackage;

  public SourceGeneratingTask() {
    testDriverPackage = getProject().getObjects().property(String.class);
  }

  @TaskAction
  public void generateTestDriverCode() {
    final JavaProjectHelper projectHelper = new JavaProjectHelper(getProject());
    final File testDriverOutputDir = projectHelper.getDriverDir();

    if (!Files.exists(testDriverOutputDir.toPath())) {
      if (!testDriverOutputDir.mkdir()) {
        throw new GradleException("Unable to create output directory");
      }
    }

    // If the test driver java class exist then don't generate anything
    String mainClass = projectHelper.findDriverClass().orElse(null);

    if (Constants.TEST_DRIVER_CLASSNAME.equals(mainClass)){
      getProject().getLogger().quiet(Constants.DRIVER_EXIST);
      return;
    }

    final File testDriverJavaFile =
        testDriverOutputDir.toPath().resolve(Constants.TEST_DRIVER_CLASSNAME + ".java").toFile();

    if (Files.exists(testDriverJavaFile.toPath())) {
      MoreFiles.deleteFile(testDriverJavaFile.toPath());
    }

    final String testDriverPackage = getTestDriverPackage().get();
    final List<String> testClassNames = getTestClassNames(projectHelper, testDriverPackage);

    final JavaCode testDriverCode = generateTestDriverCode(testDriverPackage, testClassNames);
    final File writtenTestDriverCode = testDriverCode.writeTo(testDriverOutputDir);
    if (writtenTestDriverCode == null) {
      throw new GradleException("Unable to write code; java file is null.");
    }

    getProject().getLogger().debug(Constants.SUCCESSFUL_CODE_GENERATION);
  }

  static JavaCode generateTestDriverCode(
      String testDriverPackage, Collection<String> testClassNames) {
    final String joinedTestClasses =
        Joiner.on(", ")
            .join(ImmutableStream.listCopyOf(testClassNames.stream().map(s -> s + ".class")));

    return JavaCode.classBuilder(Constants.TEST_DRIVER_CLASSNAME)
        .addPackageName(testDriverPackage)
        .addImports("org.junit.runner.Result", "org.junit.runner.JUnitCore")
        .addStaticMainMethod(
            "final Result result = JUnitCore.runClasses(" + joinedTestClasses + ");",
            "System.out.printf(\"Test ran: %s, Failed: %s%n\",",
            "\t\t\t\tresult.getRunCount(), result.getFailureCount());")
        .build();
  }

  static List<String> getTestClassNames(JavaProjectHelper projectHelper, String testDriverPackage) {
    Objects.requireNonNull(projectHelper);
    Objects.requireNonNull(testDriverPackage);

    if (testDriverPackage.isEmpty()) return ImmutableList.of();

    final Directory buildTestDir = projectHelper.getBuildTestDir(); // buildDir.dir(Constants.PROJECT_TEST_CLASS_DIR).get();

    final String testpath = testDriverPackage.replaceAll("\\.", Constants.FILE_SEPARATOR);
    final Path testClassesDir = buildTestDir.dir(testpath).getAsFile().toPath();

    final List<File> allTestClasses =
        Filefinder.findJavaClasses(testClassesDir, "$" /*exclude those that contain this symbol*/);
    return ImmutableList.copyOf(MoreFiles.getTestClassNames(allTestClasses));
  }

  public Property<String> getTestDriverPackage() {
    return this.testDriverPackage;
  }

  @Override
  protected String getTaskName() {
    return Constants.CODE_GEN_TASK;
  }

  @Override
  protected String getTaskDescription() {
    return Constants.CODE_GEN_TASK_DESCRIPTION;
  }
}
