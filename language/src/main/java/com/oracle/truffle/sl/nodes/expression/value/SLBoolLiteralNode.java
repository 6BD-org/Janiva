package com.oracle.truffle.sl.nodes.expression.value;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.antlr.v4.runtime.Token;

@NodeInfo(shortName = "j_boolean")
public class SLBoolLiteralNode extends SLExpressionNode {
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private final Boolean val;
    public SLBoolLiteralNode(Token token) {
        switch (token.getText()) {
            case TRUE:
                val = true;
                break;
            case FALSE:
                val = false;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public Boolean executeGeneric(VirtualFrame frame) {
        return val;
    }
}
