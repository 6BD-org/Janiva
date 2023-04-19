package com.oracle.truffle.jx.runtime.view;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

/** An array view is a read-only array facade of certain data structure */
@ExportLibrary(InteropLibrary.class)
public abstract class AbstractArrayView implements TruffleObject {

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

  /**
   * @param index
   * @return
   * @throws UnsupportedMessageException
   * @throws InvalidArrayIndexException
   * @implNote implementation should access underlying data structure without copying any data
   *     (except caching)
   */
  @ExportMessage
  protected abstract Object readArrayElement(long index)
      throws UnsupportedMessageException, InvalidArrayIndexException;

  /**
   * @return
   * @throws UnsupportedMessageException
   * @implSpec implementation should always provide a size bounded by integer
   */
  @ExportMessage
  protected abstract long getArraySize() throws UnsupportedMessageException;

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
  public boolean hasArrayElements() {
    return true;
  }
}
