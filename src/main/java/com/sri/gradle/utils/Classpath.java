package com.sri.gradle.utils;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.Set;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;

public class Classpath {
  private Classpath(){}

  public static Set<File> getRuntimeClasspath(Project project){
    // HACK. needed the test's runtime classpath to compile the test driver.
    // This classpath is different than the one the Daikon tool needs.
    // TODO(has) to find a better way to get this classpath.
    return ImmutableSet.copyOf(((SourceSetContainer) project
        .getProperties().get("sourceSets"))
        .getByName("test").getRuntimeClasspath()
        .getFiles());
  }
}
