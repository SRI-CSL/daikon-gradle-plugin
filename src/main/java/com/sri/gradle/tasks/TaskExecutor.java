package com.sri.gradle.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;

/**
 * Wraps DynComp, Chicory, and Daikon into a single task:
 *
 * runDaikonOn(path/to/test/classes)
 *  .withClasspath(file1, file2, ...)
 *  .toDir(..)
 */
public interface TaskExecutor {
  /**
   * Caches an error.
   * @param cause thrown exception.
   */
  void addError(Throwable cause);

  /**
   * Installs a new Task configuration
   *
   * @param configuration install configuration into task executor.
   */
  default void install(TaskConfiguration configuration) {
    configuration.configure(this);
  }

  /**
   * Likely-Invariants extraction is applied to project's test classes. These test
   * classes were automatically generated by Randoop and their .class files should
   * be already available.
   *
   * @param testClassesDir where to look for .class files.
   * @return a new TaskBuilder object.
   */
  TaskBuilder runDaikonOn(File testClassesDir);

  /**
   * Executes installed configuration
   */
  void execute() throws TaskConfigurationError;

  class TaskConfigurationError extends RuntimeException {
    TaskConfigurationError(Collection<Throwable> throwables) {
      super(buildErrorMessage(throwables));
    }

    private static String buildErrorMessage(Collection<Throwable> errorMessages) {
      final List<Throwable> encounteredErrors = new ArrayList<>(errorMessages);
      if (!encounteredErrors.isEmpty()) {
        encounteredErrors.sort(new ThrowableComparator());
      }

      final Formatter messageFormatter = new Formatter();
      messageFormatter.format("Task configuration errors:%n%n");
      int index = 1;

      for (Throwable errorMessage : encounteredErrors) {
        final String    message = errorMessage.getLocalizedMessage();
        final String    line    = "line " + message.charAt(message.lastIndexOf("line") + 5);
        messageFormatter.format("%s) Error at %s:%n", index++, line).format(" %s%n%n", message);
      }

      return messageFormatter.format("%s error[s]", encounteredErrors.size()).toString();
    }
  }


  class ThrowableComparator implements Comparator<Throwable> {
    @Override
    public int compare(Throwable a, Throwable b) {
      return a.getMessage().compareTo(b.getMessage());
    }
  }
}
