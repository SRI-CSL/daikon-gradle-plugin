package com.sri.gradle.utils;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.Set;
import org.gradle.api.Project;

public class RuntimeClasspath {
  private RuntimeClasspath(){
    throw new Error("Cannot be instantiated");
  }
  public static Set<File> getFiles(Project project){
    // HACK. needed the test's runtime classpath to compile the test driver.
    // This classpath is different than the one the Daikon tool needs.
    // TODO(has) to find a better way to get this classpath.
    return ImmutableSet.copyOf(
        JavaProjectHelper.getSourceSet("test", project).getRuntimeClasspath().getFiles());
  }
}
