package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.JXArray;

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
        JXArray array = (JXArray) arrayAllocationNode.executeGeneric(frame);
        for (int i=0; i<children.size(); i++) {
            array.writeArrayElement(i, children.get(i).executeGeneric(frame));
        }
        return array;
    }
}
