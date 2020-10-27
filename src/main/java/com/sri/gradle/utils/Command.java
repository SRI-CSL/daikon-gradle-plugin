package com.sri.gradle.utils;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Command {

  private final PrintStream stdout;
  private final PrintStream stderr;
  private final List<String> args;

  private final Map<String, String> environment;
  private final File workingDirectory;
  private final boolean permitNonZeroExitStatus;

  private volatile Process process;

  /**
   * Constructs a new Command using elements specified in its builder.
   *
   * @param builder the command builder.
   */
  private Command(Builder builder) {
    Objects.requireNonNull(builder);

    this.stdout = builder.stdout;
    this.stderr = builder.stderr;
    this.args = new ArrayList<>(builder.args);
    this.environment = builder.env;

    this.workingDirectory = builder.workingDirectory;
    this.permitNonZeroExitStatus = builder.permitNonZeroExitStatus;

    // checks if we maxed out the number of budgeted arguments
    if (builder.maxCommandLength != -1) {
      final String string = toString();
      if (string.length() > builder.maxCommandLength) {
        throw new IllegalStateException(
            "Maximum command length " + builder.maxCommandLength + " exceeded by: " + string);
      }
    }
  }

  /**
   * Creates a Command.Builder object
   *
   * @return self to facilitate method chaining
   */
  public static Builder create() {
    return create(null, null);
  }

  /**
   * Creates a Command.Builder object given standard output streams.
   *
   * @param stdout standard output
   * @param stderr standard error output
   * @return self to facilitate method chaining
   */
  public static Builder create(PrintStream stdout, PrintStream stderr) {
    return new Builder().standardStreams(stdout, stderr);
  }

  /**
   * starts the command
   *
   * @throws IOException if unable to start command.
   */
  public void start() throws IOException {
    if (isStarted()) {
      throw new IllegalStateException("Already started!");
    }

    println(stdout, ("executing " + this));

    final ProcessBuilder processBuilder =
        new ProcessBuilder().command(args).redirectErrorStream(true);

    // makes sure we set a non-null working directory
    processBuilder.directory(Objects.requireNonNull(workingDirectory));

    processBuilder.environment().putAll(environment);

    println(stdout, ("INFO: " + String.format("Current process: %s", toString())));

    process = processBuilder.start();
  }

  /** @return true if the process has started; false otherwise. */
  public boolean isStarted() {
    return process != null;
  }

  /** @return the current input stream used by running process. */
  public InputStream getInputStream() {
    if (!isStarted()) {
      throw new IllegalStateException("Not started!");
    }

    return process.getInputStream();
  }

  /**
   * Reads from standard input and writes to standard output. Once the process completes, the
   * command's output is returned.
   *
   * @return the output on terminal.
   * @throws IOException unexpected behavior occurred.
   * @throws InterruptedException unexpected behavior occurred.
   */
  public List<String> gatherOutput() throws IOException, InterruptedException {
    if (!isStarted()) {
      throw new IllegalStateException("Not started!");
    }

    try (BufferedReader bufferedReader =
        new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8))) {
      final List<String> outputLines = new ArrayList<>();

      String outputLine;
      while ((outputLine = bufferedReader.readLine()) != null) {
        println(stdout, outputLine);

        outputLines.add(outputLine);
      }

      int exitValue = process.waitFor();

      if (exitValue != 0 && !permitNonZeroExitStatus) {
        throw new CommandFailedException(args, outputLines);
      }

      return outputLines;
    }
  }

  /** @return the output displayed on the terminal. */
  public List<String> execute() {
    try {
      start();
      return gatherOutput();
    } catch (IOException e) {
      println(stderr, e.getMessage());
      throw new RuntimeException("Failed to execute process: " + args, e);
    } catch (InterruptedException e) {
      println(stderr, e.getMessage());
      throw new RuntimeException("Interrupted while executing process: " + args, e);
    }
  }

  @Override
  public String toString() {

    MoreObjects.ToStringHelper toString = MoreObjects.toStringHelper(this);
    for (String eachKey : environment.keySet()) {
      toString = toString.add(eachKey, environment.get(eachKey));
    }

    for (String eachArg : args) {
      toString = toString.addValue(eachArg);
    }

    return toString.toString();
  }

  /**
   * Implements a generic method for joining a collection of objects. This method is intended to
   * work on Java6+ versions.
   *
   * @param delimiter delimiter between entries in a collection.
   * @param data collection to join using a given delimiter.
   * @param <T> element type
   * @return joined collection represented as a String
   */
  public static <T> String joinCollection(String delimiter, Collection<T> data) {
    final Iterator<T> iterator = data.iterator();
    final StringBuilder stringBuilder = new StringBuilder();

    if (iterator.hasNext()) {
      stringBuilder.append(iterator.next());

      while (iterator.hasNext()) {
        stringBuilder.append(delimiter).append(iterator.next());
      }
    }

    return stringBuilder.toString();
  }

  private static void println(PrintStream pstream, String text) {
    if (pstream != null) {
      pstream.println(text);
    }
  }

  /** Command builder */
  public static class Builder {

    private PrintStream stdout;
    private PrintStream stderr;
    private final List<String> args;
    private final Map<String, String> env;

    private File workingDirectory;
    private boolean permitNonZeroExitStatus;
    private int maxCommandLength;

    /** Creates a command builder. */
    Builder() {
      this.stdout = null;
      this.stderr = null;

      this.workingDirectory = null;
      this.permitNonZeroExitStatus = false;

      this.maxCommandLength = Integer.MAX_VALUE;

      this.args = new ArrayList<>();
      this.env = new LinkedHashMap<>();
    }

    public List<String> getArgs() {
      return ImmutableList.copyOf(args);
    }

    /**
     * Sets the command's arguments.
     *
     * @param args the command's arguments.
     * @return self
     */
    public Builder arguments(Object... args) {
      return arguments(ImmutableStream.listCopyOf(Arrays.stream(args).map(Object::toString)));
    }

    /**
     * Sets the command's list of arguments.
     *
     * @param args the command's list of arguments.
     * @return self
     */
    public Builder arguments(List<String> args) {
      this.args.addAll(args);
      return this;
    }

    /**
     * Sets an environment's variable.
     *
     * @param key key identifying the variable
     * @param value the value of the variable
     * @return self
     */
    public Builder environment(String key, String value) {
      env.put(Objects.requireNonNull(key), Objects.requireNonNull(value));
      return this;
    }

    /**
     * Sets the standard streams (out and error) to which the Command writes its output and its
     * error output.
     *
     * @param stdout the standard output
     * @param stderr the standard error
     * @return self
     */
    public Builder standardStreams(PrintStream stdout, PrintStream stderr) {
      this.stdout = stdout;
      this.stderr = stderr;
      return this;
    }

    /**
     * Sets the command's working directory.
     *
     * @param localWorkingDirectory the command's working directory.
     * @return self
     */
    public Builder workingDirectory(File localWorkingDirectory) {
      this.workingDirectory = Objects.requireNonNull(localWorkingDirectory);
      return this;
    }

    /**
     * Prevents command from throwing if the invoked process returns a nonzero exit code.
     *
     * @return self
     */
    public Builder permitNonZeroExitStatus() {
      this.permitNonZeroExitStatus = true;
      return this;
    }

    /**
     * Sets the permitted length of a command in its String representation.
     *
     * @param maxLength the length of a command (string representation of command)
     * @return self.
     */
    public Builder maxCommandLength(int maxLength) {
      this.maxCommandLength = maxLength;
      return this;
    }

    /** @return the built command. */
    public Command build() {
      return new Command(this);
    }

    /**
     * Shortcut to execute a command
     *
     * @return a list of lines representing the output of the command.
     */
    public List<String> execute() {
      return build().execute();
    }

    @Override
    public String toString() {
      final String left = this.args.toString();
      String right = Optional.ofNullable(workingDirectory).map(File::toString).orElse("");
      return left + " : " + right;
    }
  }

  /** Command failed to execute exception. */
  private static class CommandFailedException extends RuntimeException {

    /**
     * Construct a new CommandFailedException object.
     *
     * @param args list of command's args.
     * @param outputLines list of output lines displayed on terminal.
     */
    CommandFailedException(List<String> args, List<String> outputLines) {
      this(formatMessage(args, outputLines));
    }

    /**
     * Construct a new CommandFailedException object.
     *
     * @param message exception message
     */
    CommandFailedException(String message) {
      super(message);
    }

    /**
     * Turns a list of args and output lines into a formatted message.
     *
     * @param args list of command's args.
     * @param outputLines list of output lines displayed on terminal.
     * @return formatted message.
     */
    static String formatMessage(List<String> args, List<String> outputLines) {
      StringBuilder result = new StringBuilder();
      result.append("Command failed:");

      for (String arg : args) {
        result.append(" ").append(arg);
      }

      for (String outputLine : outputLines) {
        result.append(System.lineSeparator()).append("  ").append(outputLine);
      }

      return result.toString();
    }
  }
}
