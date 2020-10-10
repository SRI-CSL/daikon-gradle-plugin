package com.foo;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class FooStuffTestDriver {
  public static void main(String... args) {
    final FooStuffTestDriver a = new FooStuffTestDriver();
    a.testEquality();
  }

  @Test public void testEquality() {
    Foo x = new Foo();
    Foo y = x;

    assertEquals(x, y);
  }
}