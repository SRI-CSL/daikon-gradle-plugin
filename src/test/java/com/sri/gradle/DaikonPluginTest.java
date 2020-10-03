package com.sri.gradle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.sri.gradle.utils.Command;
import com.sri.gradle.utils.Filefinder;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;

public class DaikonPluginTest {
  @Test
  public void testJavafinder() {
    Path dir = new File("src/main/java/com/sri/gradle/utils").toPath();
    System.out.println(dir);
    List<File> filesAvailable = Filefinder.findJavaFiles(dir);
    assertThat("filesAvailable=" + filesAvailable, filesAvailable.size(), is(4));
  }

  @Test
  public void testCommandBuilder() {
    List<String> filesAvailable =
        Command.create().arguments("ls").permitNonZeroExitStatus().execute();

    assertThat("filesAvailable=" + filesAvailable, filesAvailable.size(), is(9));
  }
}
