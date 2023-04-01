package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.JSONXLang;
import com.oracle.truffle.jx.JXException;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.JXArray;
import com.oracle.truffle.jx.runtime.JXBigNumber;
import com.oracle.truffle.jx.runtime.JXContext;
import org.graalvm.nativebridge.In;


@NodeChild("o")
public abstract class JXRangeNode extends JXExpressionNode {


    @Specialization(guards = "isNumber(o)")
    public Object doLong(JXBigNumber o, @Cached("lookup()") AllocationReporter reporter) {
        if (o.getValue().longValue() > Integer.MAX_VALUE) {
            throw new JXException("ranged number must be less or equal to " + Integer.MAX_VALUE, this);
        }
        JXArray res = JSONXLang.get(this).createJXArray(reporter, o.intValue());
        for (int i=0; i<o.intValue(); i++) {
            res.writeArrayElement(i, i);
        }
        return res;
    }

    @Specialization(guards = "isString(o)")
    public Object doString(TruffleString o, @Cached("lookup()") AllocationReporter reporter) {
        JXArray res = JSONXLang.get(this).createJXArray(reporter, o.byteLength(TruffleString.Encoding.UTF_8));
        for (int i=0; i<o.byteLength(TruffleString.Encoding.UTF_8); i++) {
            res.writeArrayElement(i, o.substringUncached(i, 1, TruffleString.Encoding.UTF_8, true));
        }
        return res;
    }

    @Specialization(guards = "isArray(o)")
    public Object doArray(JXArray o) {
        // simply return a reference to it
        return o;
    }

    protected boolean isArray(Object o) {
        return o instanceof JXArray;
    }

    protected boolean isNumber(Object o) {
        return o instanceof JXBigNumber;
    }

    protected boolean isString(Object o) {
        return o instanceof TruffleString;
    }

    final AllocationReporter lookup() {
        return JXContext.get(this).getAllocationReporter();
    }
}
