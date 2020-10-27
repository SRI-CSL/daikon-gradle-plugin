package com.sri.gradle;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

@SuppressWarnings("UnstableApiUsage")
public class DaikonPluginExtension {

  private final DirectoryProperty outputDir;
  private final DirectoryProperty requires;
  private final Property<String> testDriverPackage;
  private final Property<Boolean> generateTestDriver;

  public DaikonPluginExtension(Project project) {
    this.outputDir = project.getObjects().directoryProperty();
    this.requires = project.getObjects().directoryProperty();
    this.testDriverPackage = project.getObjects().property(String.class);
    this.generateTestDriver = project.getObjects().property(Boolean.class);
  }

  public DirectoryProperty getOutputDir() {
    return outputDir;
  }

  public DirectoryProperty getRequires() {
    return requires;
  }

  public Property<String> getTestDriverPackage() {
    return testDriverPackage;
  }

  public Property<Boolean> getGenerateTestDriver() {
    return generateTestDriver;
  }
}
