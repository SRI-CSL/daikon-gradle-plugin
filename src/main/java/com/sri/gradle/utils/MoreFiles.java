package com.sri.gradle.utils;

import com.google.common.collect.Iterables;
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
   * Gets count of files in a directory
   * @param dir current directory
   * @return count of files in directory
   */
  public static int fileCount(Path dir){
    try {
      return Iterables.size(Files.newDirectoryStream(dir));
    } catch (IOException ignored) {
      return 0;
    }
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

  public static List<String> getTestClassNames(List<File> javaFiles) {
    return ImmutableStream.listCopyOf(
        javaFiles.stream().map(MoreFiles::getTestClassName).filter(Objects::nonNull));
  }

  public static String getTestClassName(File fromFile) {
    if (fromFile == null) throw new IllegalArgumentException("File is null");

    try {
      final String canonicalPath = fromFile.getCanonicalPath();
      return getClassName(canonicalPath, Constants.PROJECT_TEST_CLASS_DIR, ".class");
    } catch (IOException ignored) {
    }

    return null;
  }

  public static String getClassName(String baseDirPath, String subdir, String dotExt) {
    String deletingPrefix =
        baseDirPath.substring(0, baseDirPath.indexOf(subdir));
    deletingPrefix = (deletingPrefix + subdir) + Constants.FILE_SEPARATOR;

    String trimmedCanonicalPath = baseDirPath.replace(deletingPrefix, "");
    trimmedCanonicalPath = trimmedCanonicalPath.endsWith(dotExt)
        ? trimmedCanonicalPath
        .replaceAll(dotExt, "")
        .replaceAll(Constants.FILE_SEPARATOR, ".")
        : trimmedCanonicalPath;
    return trimmedCanonicalPath;
  }
}
