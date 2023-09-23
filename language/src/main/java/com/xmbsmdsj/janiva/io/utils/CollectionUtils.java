package com.xmbsmdsj.janiva.io.utils;

import java.util.Collection;

public abstract class CollectionUtils {
  public static boolean isEmpty(Collection<?> c) {
    return c == null || c.isEmpty();
  }
}
