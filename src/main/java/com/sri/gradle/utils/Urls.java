package com.sri.gradle.utils;

import com.sri.gradle.Constants;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.stream.Collectors;

public class Urls {

  public static String toURLStr(Collection<URL> urls){
    return urls.stream()
        .map(URL::toString)
        .collect(Collectors.joining(Constants.PATH_SEPARATOR));
  }

  public static URL toURL(String filepath) throws RuntimeException {
    final URL source;
    try {
      source = createUrlFrom(filepath);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Could not create source path!", e);
    }
    return source;
  }

  private static URL createUrlFrom(final String path) throws MalformedURLException {
    return new File(path).toURI().toURL();
  }
}
