package com.oracle.truffle.jx.nodes.expression.value;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import org.antlr.v4.runtime.Token;

@NodeInfo(shortName = "j_boolean")
public class JXBoolLiteralNode extends JXExpressionNode {
  private static final String TRUE = "true";
  private static final String FALSE = "false";
  private final Boolean val;

  public JXBoolLiteralNode(Token token) {
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
