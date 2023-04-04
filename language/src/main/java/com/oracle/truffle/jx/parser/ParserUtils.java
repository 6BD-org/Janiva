package com.oracle.truffle.jx.parser;

import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.expression.value.JXStringLiteralNode;
import com.oracle.truffle.jx.parser.exceptions.JXSyntaxError;

abstract class ParserUtils {
  protected static TruffleString evalToString(JXExpressionNode jxExpressionNode) {
    if (jxExpressionNode instanceof JXStringLiteralNode) {
      return ((JXStringLiteralNode) jxExpressionNode).executeGeneric(null);
    }
    throw new JXSyntaxError();
  }
}
