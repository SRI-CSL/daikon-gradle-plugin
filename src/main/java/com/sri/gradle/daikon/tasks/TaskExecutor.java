package com.sri.gradle.daikon.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;
import org.gradle.api.Project;

/**
 * Wraps DynComp, Chicory, and Daikon into a single task:
 *
 * <p>runDaikonOn(new InputProvider(inputDirectory, testDriverPackage (optional), GradleProject))
 * .withClasspath(file1, file2, ...) or .withClasspath(Collection(File)) .toDir(..)
 */
public interface TaskExecutor {
  /**
   * Caches an error.
   *
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
   * Likely-Invariants extraction is applied to a type of input (specified by {@code Provider}).
   *
   * @param provider the input provider, which contains an {@link File input directory}, {@link
   *     String testDriverPackage} (Optional), and the {@link Project gradle project}.
   * @return a new TaskBuilder object.
   */
  TaskBuilder runDaikonOn(InputProvider provider);

  /** Executes installed configuration */
  void execute() throws TaskConfigurationError;

  class TaskConfigurationError extends RuntimeException {

    TaskConfigurationError(String message){
      super(message);
    }

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
        final String message = errorMessage.getLocalizedMessage();
        final String line = "line " + message.charAt(message.lastIndexOf("line") + 5);
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
