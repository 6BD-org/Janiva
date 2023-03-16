package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.jx.nodes.JXExpressionNode;

import java.util.List;

public class JXLambdaNode extends RootNode {

    private List<JXAttributeBindingNode> parameterBindingNodes;
    private JXExpressionNode evalNode;

    protected JXLambdaNode(
            TruffleLanguage<?> language,
            FrameDescriptor frameDescriptor,
            List<JXAttributeBindingNode> parameterBindingNodes,
            JXExpressionNode evalNode
    ) {
        super(language, frameDescriptor);
        this.parameterBindingNodes = parameterBindingNodes;
        this.evalNode = evalNode;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        for (JXAttributeBindingNode parameterBindingNode : parameterBindingNodes) {
            parameterBindingNode.executeVoid(frame);
        }
        return evalNode.executeGeneric(frame);
    }
}
