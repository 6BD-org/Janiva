package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.parser.lambda.LambdaTemplate;

public class JXLambdaAttrAccessNode extends JXExpressionNode {

    private final TruffleString name;
    private LambdaTemplate lambdaTemplate;

    public JXLambdaAttrAccessNode(TruffleString name, LambdaTemplate lambdaTemplate) {
        this.name = name;
        this.lambdaTemplate = lambdaTemplate;
    }


    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return frame.getObject(lambdaTemplate.lookupParamSlot(name));
    }
}
