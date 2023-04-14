package com.oracle.truffle.jx.runtime.view;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(InteropLibrary.class)
public class JXIntegerRangeArrayView extends AbstractArrayView {

  private final int i;

  public JXIntegerRangeArrayView(int i) {
    this.i = i;
  }

  @ExportMessage
  @Override
  protected Object readArrayElement(long index)
      throws UnsupportedMessageException, InvalidArrayIndexException {
    if (index >= i) {
      throw InvalidArrayIndexException.create(index);
    }
    return index;
  }

  @ExportMessage
  @Override
  protected long getArraySize() throws UnsupportedMessageException {
    return i;
  }
}
