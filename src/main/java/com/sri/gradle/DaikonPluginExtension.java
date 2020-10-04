package com.sri.gradle;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

public class DaikonPluginExtension {

  private final DirectoryProperty outputDir;
  private final DirectoryProperty requires;
  private final Property<String> testDriverPackage;


  @SuppressWarnings("UnstableApiUsage")
  public DaikonPluginExtension(Project project) {
    this.outputDir = project.getObjects().directoryProperty();
    this.requires = project.getObjects().directoryProperty();
    this.testDriverPackage = project.getObjects().property(String.class);
  }

  public Property<String> getTestDriverPackage() {
    return testDriverPackage;
  }

  public DirectoryProperty getOutputDir() {
    return outputDir;
  }

  public DirectoryProperty getRequires() {
    return requires;
  }

}