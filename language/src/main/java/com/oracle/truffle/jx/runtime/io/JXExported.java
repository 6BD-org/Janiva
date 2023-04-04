package com.oracle.truffle.jx.runtime.io;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.jx.runtime.JXArray;
import com.oracle.truffle.jx.runtime.JXObject;
import com.oracle.truffle.jx.runtime.JXPartialLambda;

@ExportLibrary(InteropLibrary.class)
public class JXExported implements TruffleObject {
    enum Mode {
        Value, Lambda, Array
    }

    private JXObject jxObject;
    private final Mode mode;

    public JXExported(JXObject jxObject) {
        this.jxObject = jxObject;
        this.mode = Mode.Value;
    }

    public JXObject getValue() {
        if (mode != Mode.Value) {
            throw new IllegalStateException("Cannot get value from non-value export");
        }
        return this.jxObject;
    }


}
