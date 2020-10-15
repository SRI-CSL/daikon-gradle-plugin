package com.sri.gradle.tasks;

import javax.inject.Provider;

interface InputProvider extends Provider<Object[]> {
  @Override
  Object[] get();

  default int size() {
    return get().length;
  }
}
