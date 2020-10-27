package com.sri.gradle.utils;

import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

import com.google.common.collect.ImmutableSet;
import com.sri.gradle.Constants;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MoreFiles {
  private MoreFiles() {
    throw new Error("Cannot be instantiated");
  }

  /**
   * Sets a file object as a writable file object.
   *
   * @param file the file object
   * @return true if file was set as writable; false otherwise.
   */
  public static boolean setWritable(File file) {
    try {
      Objects.requireNonNull(file);
      final PosixFileAttributeView fileAttributes =
          Files.getFileAttributeView(file.toPath(), PosixFileAttributeView.class);
      Objects.requireNonNull(fileAttributes);
      fileAttributes.setPermissions(EnumSet.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, OTHERS_READ, OTHERS_WRITE));
    } catch (Exception ignored) {
      return false;
    }

    return true;
  }

  /**
   * Gets a file object's posix file permissions.
   *
   * @param file the file object
   * @return set of posix file permissions.
   */
  public static Set<PosixFilePermission> getPosixFilePermissions(File file) {
    try {
      final PosixFileAttributeView fileAttributes =
          Files.getFileAttributeView(file.toPath(), PosixFileAttributeView.class);
      Objects.requireNonNull(fileAttributes);
      return fileAttributes.readAttributes().permissions();
    } catch (Exception ignored) {
      return ImmutableSet.of();
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

    String trimmedCanonicalPath = canonicalPath.replace(deletingPrefix, "");
    trimmedCanonicalPath =
        trimmedCanonicalPath
            .replaceAll(".class", "")
            .replaceAll(Constants.FILE_SEPARATOR, ".");
    return trimmedCanonicalPath;
  }
}
