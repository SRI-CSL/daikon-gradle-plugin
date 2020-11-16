package com.sri.gradle.daikon.tasks;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

class InputProviderImpl implements InputProvider {

  private final Object[] inputContent;

  InputProviderImpl(Object... content) {
    Objects.requireNonNull(content);
    int size = content.length;
    final List<Object> objs = new LinkedList<>();
    for (Object each : content) {
      if (each == null) continue;
      if (objs.size() == size) break;
      objs.add(each);
    }

    if (size != objs.size()) throw new IllegalArgumentException("ill-formed input provider");

    this.inputContent = objs.toArray();
  }

  @Override
  public Object[] get() {
    return this.inputContent;
  }
}
