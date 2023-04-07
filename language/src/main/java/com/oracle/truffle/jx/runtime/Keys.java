package com.oracle.truffle.jx.runtime;

import com.oracle.truffle.api.interop.InteropLibrary;import com.oracle.truffle.api.interop.InvalidArrayIndexException;import com.oracle.truffle.api.interop.TruffleObject;import com.oracle.truffle.api.library.ExportLibrary;import com.oracle.truffle.api.library.ExportMessage;@ExportLibrary(InteropLibrary.class)
 public final class Keys implements TruffleObject {

    private final Object[] keys;

    public Keys(Object[] keys) {
        this.keys = keys;
    }

    @ExportMessage
    Object readArrayElement(long index) throws InvalidArrayIndexException {
        if (!isArrayElementReadable(index)) {
            throw InvalidArrayIndexException.create(index);
        }
        return keys[(int) index];
    }

    @ExportMessage
    boolean hasArrayElements() {
        return true;
    }

    @ExportMessage
    long getArraySize() {
        return keys.length;
    }

    @ExportMessage
    boolean isArrayElementReadable(long index) {
        return index >= 0 && index < keys.length;
    }
}