package com.oracle.truffle.jx.runtime;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.strings.TruffleString;

/** This is a read-only image of a string that lazily access array elements. */
@ExportLibrary(InteropLibrary.class)
public class JXStringImage implements TruffleObject {

  private final TruffleString ts;

  // Cache to accelerate repeated access
  private final TruffleString[] cache;

  public JXStringImage(TruffleString ts) {
    this.ts = ts;
    this.cache = new TruffleString[ts.byteLength(TruffleString.Encoding.UTF_8)];
  }

  @ExportMessage
  public boolean hasArrayElements() {
    return true;
  }

  @ExportMessage
  public Object readArrayElement(long index)
      throws UnsupportedMessageException, InvalidArrayIndexException {
    int i = (int) index;
    // Note that character in string is never null!
    if (cache[i] != null) {
      return cache[i];
    }
    if (index >= ts.byteLength(TruffleString.Encoding.UTF_8)) {
      throw InvalidArrayIndexException.create(index);
    }
    TruffleString res = ts.substringUncached((int) index, 1, TruffleString.Encoding.UTF_8, true);
    cache[i] = res;
    return res;
  }

  @ExportMessage
  public boolean isArrayElementReadable(long index) {
    return false;
  }

  @ExportMessage
  public boolean isArrayElementModifiable(long index) {
    return false;
  }

  @ExportMessage
  public boolean isArrayElementInsertable(long index) {
    return false;
  }

  @ExportMessage
  public boolean isArrayElementRemovable(long index) {
    return false;
  }

  @ExportMessage
  final long getArraySize() throws UnsupportedMessageException {
    return ts.byteLength(TruffleString.Encoding.UTF_8);
  }

  @ExportMessage
  final void writeArrayElement(long index, Object value)
      throws UnsupportedMessageException, InvalidArrayIndexException {
    throw UnsupportedMessageException.create();
  }

  @ExportMessage
  final void removeArrayElement(long index)
      throws UnsupportedMessageException, InvalidArrayIndexException {
    throw UnsupportedMessageException.create();
  }
}
