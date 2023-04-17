package com.oracle.truffle.jx.runtime.view;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.runtime.view.AbstractArrayView;

/** This is a read-only image of a string that lazily access array elements. */
@ExportLibrary(InteropLibrary.class)
public class JXStringArrayView extends AbstractArrayView {

  private final TruffleString ts;

  // Cache to accelerate repeated access
  private final TruffleString[] cache;

  public JXStringArrayView(TruffleString ts) {
    this.ts = ts;
    this.cache = new TruffleString[ts.byteLength(TruffleString.Encoding.UTF_8)];
  }

  @ExportMessage
  @Override
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
  @Override
  protected long getArraySize() throws UnsupportedMessageException {
    return ts.byteLength(TruffleString.Encoding.UTF_8);
  }

}
