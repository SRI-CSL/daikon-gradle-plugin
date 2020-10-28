package com.sri.gradle.tasks;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.sri.gradle.Constants;
import com.sri.gradle.utils.Filefinder;
import com.sri.gradle.utils.ImmutableStream;
import com.sri.gradle.utils.JavaCode;
import com.sri.gradle.utils.JavaProjectHelper;
import com.sri.gradle.utils.MoreFiles;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
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

    final File testDriverJavaFile =
        testDriverOutputDir.toPath().resolve(Constants.TEST_DRIVER_CLASSNAME + ".java").toFile();

    if (Files.exists(testDriverJavaFile.toPath())) {
      MoreFiles.deleteFile(testDriverJavaFile.toPath());
    }

    final String testDriverPackage = getTestDriverPackage().get();
    final List<String> testClassNames = getTestClassNames(getProject(), testDriverPackage);

    final JavaCode testDriverCode = generateTestDriverCode(testDriverPackage, testClassNames);
    final File writtenTestDriverCode = testDriverCode.writeTo(testDriverOutputDir);
    if (writtenTestDriverCode == null) {
      throw new GradleException("Unable to write code; java file is null.");
    }

    getProject().getLogger().quiet(Constants.SUCCESSFUL_CODE_GENERATION);
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

  static List<String> getTestClassNames(Project project, String testDriverPackage) {
    Objects.requireNonNull(project);
    Objects.requireNonNull(testDriverPackage);

    if (testDriverPackage.isEmpty()) return ImmutableList.of();

    final DirectoryProperty buildDir = project.getLayout().getBuildDirectory();

    final Directory buildTestDir = buildDir.dir(Constants.PROJECT_TEST_CLASS_DIR).get();

    final String testpath = testDriverPackage.replaceAll("\\.", Constants.FILE_SEPARATOR);
    final Path testClassesDir = buildTestDir.dir(testpath).getAsFile().toPath();

    final List<File> allTestClasses =
        Filefinder.findJavaClasses(testClassesDir, "$" /*exclude those that contain this symbol*/);
    return ImmutableList.copyOf(MoreFiles.getClassNames(allTestClasses));
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
