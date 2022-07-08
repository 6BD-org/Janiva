package com.oracle.truffle.jx.nodes.expression.value;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.JXObject;

public class JXObjectNode extends JXExpressionNode {

    private final JXObject object;
    public JXObjectNode(JXObject object) {
        this.object = object;
    }
    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return object;
    }
}
