package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.jx.JSONXLang;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.JXContext;


@ImportStatic(JXContext.class)
@GenerateNodeFactory
@NodeField(name = "size", type = Integer.class)
public abstract class JXArrayAllocationNode extends JXExpressionNode {


    @Specialization
    public Object newArray(Integer size, @Cached("lookup()")AllocationReporter reporter) {
        return JSONXLang.get(this).createJXArray(reporter, size);
    }

    final AllocationReporter lookup() {
        return JXContext.get(this).getAllocationReporter();
    }
}
