package com.sri.gradle.tasks;

import java.util.LinkedList;
import java.util.List;

class InputProviderImpl implements InputProvider {

  private final Object[] inputContent;

  InputProviderImpl(int size, Object... content) {
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
