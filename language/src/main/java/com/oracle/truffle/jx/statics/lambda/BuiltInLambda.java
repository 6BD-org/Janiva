package com.oracle.truffle.jx.statics.lambda;

import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.core.JXIfNode;
import com.oracle.truffle.jx.parser.exceptions.JXSyntaxError;

import java.util.List;

public enum BuiltInLambda implements BuiltInLambdaFactory {

    IF {
        @Override
        public JXExpressionNode create(List<JXExpressionNode> arguments) {
            return new JXIfNode(
                    arguments.get(0),
                    arguments.get(1),
                    arguments.get(2)
            );
        }
    };


    public static BuiltInLambda valueOf(TruffleString ts) {
        if (ts.equals(LambdaRegistry.IF)) {
            return IF;
        }
        throw new JXSyntaxError("Illegal built-in");
    }
}
