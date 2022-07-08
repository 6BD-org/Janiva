package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.JXStatementNode;
import com.oracle.truffle.jx.runtime.JXObject;

import java.util.Map;


public  class JXAttributeBindingNode extends JXStatementNode {

    private final int slot;
    private final JXExpressionNode expressionNode;

    public JXAttributeBindingNode(int slot, JXExpressionNode expressionNode) {
        this.slot = slot;
        this.expressionNode = expressionNode;
    }

    @Override
    public void executeVoid(VirtualFrame frame) {

        frame.setObject(slot, expressionNode.executeGeneric(frame));
    }
}
