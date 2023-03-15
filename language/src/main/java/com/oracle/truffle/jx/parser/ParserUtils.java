package com.oracle.truffle.jx.parser;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.expression.value.JXStringLiteralNode;
import com.oracle.truffle.jx.parser.exceptions.JXSyntaxError;

import java.util.Objects;

abstract class ParserUtils {
    protected static TruffleString evalToString(JXExpressionNode jxExpressionNode) {
        if (jxExpressionNode instanceof JXStringLiteralNode) {
            return ((JXStringLiteralNode) jxExpressionNode).executeGeneric(null);
        }
        throw new JXSyntaxError();
    }
}
