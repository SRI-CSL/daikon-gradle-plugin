package com.sri.gradle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.sri.gradle.utils.Command;
import com.sri.gradle.utils.Filefinder;
import com.sri.gradle.utils.MoreFiles;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;

public class DaikonPluginTest {
  @Test public void testJavafinder() {
    Path dir = new File("src/main/java/com/sri/gradle/utils").toPath();
    System.out.println(dir);
    List<File> filesAvailable = Filefinder.findJavaFiles(dir);

    final List<String> output = Command.create()
        .workingDirectory(dir.toFile())
        .arguments("ls")
        .execute();

    assertThat(filesAvailable.size(), is(output.size()));
  }

  @Test public void testCommandBuilder() {
    List<String> echoResult = Command.create()
        .workingDirectory(Constants.USER_WORKING_DIR)
        .arguments("echo", "hello")
        .permitNonZeroExitStatus().execute();

    assertThat(echoResult.size(), is(1));
    assertEquals(echoResult.get(0), "hello");
  }

  @Test public void testFQNExtractor() {
    final String canonicalPath = "daikon-gradle-plugin/consumer/build/classes/java/test/com/foo/FooStuffTestDriver.class";
    final String fqn = MoreFiles.getClassName(canonicalPath);

    assertNotNull(fqn);
    assertEquals("com.foo.FooStuffTestDriver", fqn);

  }
}