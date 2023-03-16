package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.impl.FrameWithoutBoxing;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.jx.nodes.JXExpressionNode;

import java.util.List;

public class JXLambdaNode extends JXExpressionNode {


    private JXLambdaExecutor executor;
    private FrameDescriptor frameDescriptor;

    public JXLambdaNode(
            TruffleLanguage<?> language,
            FrameDescriptor frameDescriptor,
            List<JXAttributeBindingNode> parameterBindingNodes,
            JXExpressionNode evalNode
    ) {
        this.frameDescriptor = frameDescriptor;
        this.executor = new JXLambdaExecutor(language, frameDescriptor, parameterBindingNodes, evalNode);
    }


    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return this.executor.execute(new FrameWithoutBoxing(frameDescriptor, new Object[0]));
    }
}
