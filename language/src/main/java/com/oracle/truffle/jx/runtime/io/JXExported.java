package com.oracle.truffle.jx.runtime.io;

import com.oracle.truffle.api.TruffleLanguage;import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;import com.oracle.truffle.api.interop.UnsupportedMessageException;import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;import com.oracle.truffle.jx.JanivaLang;
import com.oracle.truffle.jx.runtime.*;
import org.graalvm.polyglot.Value;

@ExportLibrary(InteropLibrary.class)
public class JXExported implements TruffleObject{

  private static final String[] INTERNAL_MEMBERS = new String[] {JXType.MAGIC_MEMBER_EXPORTED, JXType.MAGIC_MEMBER_EXP_VAL};
  private static final String[] EMPTY = new String[]{};
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

  @ExportMessage
  boolean hasLanguage() {
    return true;
  }

  @ExportMessage
  Class<? extends TruffleLanguage<?>> getLanguage() {
    return JanivaLang.class;
  }

  @ExportMessage
  final Object toDisplayString(boolean allowSideEffects) {
    return "JanivaExported";
  }

  @ExportMessage
  public boolean hasMembers() {
    return true;
  }

  @ExportMessage
  public Object readMember(
          String member
  ) throws UnsupportedMessageException, UnknownIdentifierException {
    if (JXType.MAGIC_MEMBER_EXPORTED.equals(member)) {
      return Boolean.TRUE;
    }
  //    if (JXType.MAGIC_MEMBER_EXP_VAL.equals(member)) {
  //      return this.jxObject;
  //    }
    throw UnsupportedMessageException.create();
  }

  @ExportMessage
  final boolean isMemberReadable(String member) {
    return member.equals(JXType.MAGIC_MEMBER_EXPORTED) || member.equals(JXType.MAGIC_MEMBER_EXP_VAL);
  }

  @ExportMessage
  final Object getMembers(boolean includeInternal) {
    if (includeInternal) {
      return new Keys(INTERNAL_MEMBERS);
    } else {
      return new Keys(EMPTY);
    }
  }

  @ExportMessage
  final boolean isExecutable() {
    return true;
  }

  @ExportMessage
  final Object execute(Object[] args) {
    return Value.asValue(this.jxObject);
  }

}
