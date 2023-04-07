package com.oracle.truffle.jx.runtime.io;

import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.jx.runtime.*;

// Non-interop
public class JXExported implements TruffleObject {

  private static final String[] INTERNAL_MEMBERS =
      new String[] {JXType.MAGIC_MEMBER_EXPORTED, JXType.MAGIC_MEMBER_EXP_VAL};
  private static final String[] EMPTY = new String[] {};

  enum Mode {
    Value,
    Lambda,
    Array
  }

  private Object jxObject;
  private final Mode mode;

  public JXExported(Object jxObject) {
    this.jxObject = jxObject;
    this.mode = Mode.Value;
  }

  public Object getValue() {
    if (mode != Mode.Value) {
      throw new IllegalStateException("Cannot get value from non-value export");
    }
    return this.jxObject;
  }
}
