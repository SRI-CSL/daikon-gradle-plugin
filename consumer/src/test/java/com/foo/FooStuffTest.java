package com.foo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FooStuffTest {
//  public static void main(String... args) {
//    final FooStuffTest a = new FooStuffTest();
//    a.testEquality();
//  }

  @Test public void testEquality() {
    Foo x = new Foo();
    Foo y = x;

    assertEquals(x, y);
  }
}