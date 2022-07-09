package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.jx.nodes.JXExpressionNode;

import java.util.List;

public class JXArrayAssemblyNode extends JXExpressionNode {

    private final List<JXExpressionNode> children;
    private final JXArrayAllocationNode arrayAllocationNode;

    public JXArrayAssemblyNode(List<JXExpressionNode> children, JXArrayAllocationNode arrayAllocationNode) {
        this.children = children;
        this.arrayAllocationNode = arrayAllocationNode;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return null;
    }
}
