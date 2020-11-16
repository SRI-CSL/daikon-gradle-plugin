package com.sri.gradle.daikon;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.sri.gradle.daikon.utils.Filefinder;
import com.sri.gradle.daikon.utils.ImmutableStream;
import com.sri.gradle.daikon.utils.MoreFiles;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;

public class DaikonPluginTest {
  @Test public void testJavafinder() throws Exception {
    Path dir = new File("src/main/java/com/sri/gradle/daikon/utils").toPath();
    System.out.println(dir);
    List<File> filesAvailable = Filefinder.findJavaFiles(dir);
    List<Path> filesExpected = ImmutableStream.listCopyOf(Files.list(dir));

    assertThat(filesAvailable.size(), is(filesExpected.size()));
  }

  @Test public void testFQNExtractor() {
    final String canonicalPath = "daikon-gradle-plugin/consumer/build/classes/java/test/com/foo/FooStuffTestDriver.class";
    final String fqn = MoreFiles.getClassName(canonicalPath, Constants.PROJECT_TEST_CLASS_DIR, ".class");

    assertNotNull(fqn);
    assertEquals("com.foo.FooStuffTestDriver", fqn);

  }
}
