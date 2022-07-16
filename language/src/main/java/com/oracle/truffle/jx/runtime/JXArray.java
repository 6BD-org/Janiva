package com.oracle.truffle.jx.runtime;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;

@ExportLibrary(InteropLibrary.class)
public final class JXArray extends DynamicObject implements TruffleObject {

    private final Object[] content;

    public JXArray(Shape shape, Object[] content) {
        super(shape);
        this.content = content;
    }

    @ExportMessage
    public boolean hasArrayElements() {
        return true;
    }

    @ExportMessage
    public int getArraySize() {
        return content.length;
    }

    @ExportMessage
    public Object readArrayElement(long i) {
        return this.content[(int)i];
    }

    @ExportMessage
    public void writeArrayElement(long index, Object o) {
        this.content[(int) index] = o;
    }

    @ExportMessage
    public boolean isArrayElementModifiable(long index) {
        return false;
    }

    @ExportMessage
    public boolean isArrayElementInsertable(long index) {
        return true;
    }

    @ExportMessage
    public boolean isArrayElementReadable(long index) {
        return true;
    }
}
