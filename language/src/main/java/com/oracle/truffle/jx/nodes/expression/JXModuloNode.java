package com.oracle.truffle.jx.nodes.expression;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.jx.nodes.JXBinaryNode;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.JXBigNumber;
import com.oracle.truffle.jx.runtime.JXObject;

public abstract class JXModuloNode extends JXBinaryNode {
    @Specialization
    public Object executeInteger(JXBigNumber left, JXBigNumber right) {
        return new JXBigNumber(left.intValue() % right.intValue());
    }
}
