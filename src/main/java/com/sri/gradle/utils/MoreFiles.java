package com.sri.gradle.utils;

import com.sri.gradle.Constants;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class MoreFiles {
  private MoreFiles() {
    throw new Error("Cannot be instantiated");
  }

  /**
   * Deletes file in path.
   *
   * @param path file path
   */
  public static void deleteFile(Path path) {
    try {
      Files.delete(path);
    } catch (IOException ignored) {
    }
  }

  public static List<String> getClassNames(List<File> javaFiles) {
    return ImmutableStream.listCopyOf(
        javaFiles.stream().map(MoreFiles::getClassName).filter(Objects::nonNull));
  }

  public static String getClassName(File fromFile) {
    if (fromFile == null) throw new IllegalArgumentException("File is null");

    try {
      final String canonicalPath = fromFile.getCanonicalPath();
      return getClassName(canonicalPath);
    } catch (IOException ignored) {
    }

    return null;
  }

  public static String getClassName(String canonicalPath) {
    String deletingPrefix =
        canonicalPath.substring(0, canonicalPath.indexOf(Constants.PROJECT_TEST_CLASS_DIR));
    deletingPrefix = (deletingPrefix + Constants.PROJECT_TEST_CLASS_DIR) + Constants.FILE_SEPARATOR;

    String trimmedCanonicalPath = canonicalPath.replace(deletingPrefix, Constants.EMPTY_STRING);
    trimmedCanonicalPath =
        trimmedCanonicalPath
            .replaceAll(".class", Constants.EMPTY_STRING)
            .replaceAll(Constants.FILE_SEPARATOR, Constants.PERIOD);
    return trimmedCanonicalPath;
  }
}
