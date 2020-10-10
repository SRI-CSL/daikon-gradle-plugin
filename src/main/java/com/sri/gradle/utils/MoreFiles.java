package com.sri.gradle.utils;

import com.sri.gradle.Constants;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MoreFiles {
  private MoreFiles() {}

  public static List<String> getFullyQualifiedNames(List<File> javaFiles) {
    return ImmutableStream.listCopyOf(
        javaFiles.stream()
            .map(MoreFiles::getFullyQualifiedName)
            .filter(Objects::nonNull)
    );
  }

  public static String getFullyQualifiedName(File fromFile) {
    if (fromFile == null) throw new IllegalArgumentException("File is null");

    try {
      final String canonicalPath = fromFile.getCanonicalPath();
      return getFullyQualifiedName(canonicalPath);
    } catch (IOException ignored) {}

    return null;
  }

  public static String getFullyQualifiedName(String canonicalPath) {
    String deletingPrefix = canonicalPath.substring(
        0, canonicalPath.indexOf(Constants.PROJECT_TEST_CLASS_DIR));
    deletingPrefix = (deletingPrefix  + Constants.PROJECT_TEST_CLASS_DIR) + Constants.FILE_SEPARATOR;

    String trimmedCanonicalPath = canonicalPath.replace(deletingPrefix, Constants.EMPTY_STRING);
    trimmedCanonicalPath = trimmedCanonicalPath.replaceAll(".class", Constants.EMPTY_STRING)
        .replaceAll(Constants.FILE_SEPARATOR, Constants.PERIOD);
    return trimmedCanonicalPath;
  }
}
