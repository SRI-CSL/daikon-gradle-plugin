package com.sri.gradle.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/** Immutable collections and maps from Stream objects */
public class ImmutableStream {
  private ImmutableStream() {}

  /**
   * Converts stream of objects into an immutable list.
   *
   * @param stream stream of objects
   * @param <T> type parameter
   * @return an immutable list.
   */
  public static <T> List<T> listCopyOf(Stream<? extends T> stream) {
    return stream == null ? ImmutableList.of() : stream.collect(ImmutableList.toImmutableList());
  }

  /**
   * Concatenation of two sets using their Stream API.
   *
   * @param x first set
   * @param y second set
   * @param <T> type of element in a set
   * @return added values of two sets into a single stream.
   */
  public static <T> Stream<T> concat(Stream<? extends T> x, Stream<? extends T> y) {
    return Stream.concat(x, y);
  }

  /**
   * Converts stream of object into an immutable set.
   *
   * @param stream stream of objects
   * @param <T> type parameter
   * @return an immutable list.
   */
  public static <T> Set<T> setCopyOf(Stream<? extends T> stream) {
    return stream == null ? ImmutableSet.of() : stream.collect(ImmutableSet.toImmutableSet());
  }

  /**
   * Converts a stream of Map.Entry objects into an immutable map.
   *
   * @param stream stream of Map.Entry object.s
   * @param <K> the output type of the key mapping function
   * @param <V> the output type of the value mapping function
   * @return a new immutable map
   */
  public static <K, V> Map<K, V> mapCopyOf(
      Stream<? extends Map.Entry<? extends K, ? extends V>> stream) {
    return stream == null
        ? ImmutableMap.of()
        : stream.collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
