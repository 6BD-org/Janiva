package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.JXStatementNode;

public class JXAttributeBindingNode extends JXStatementNode {

  private final int slot;
  private final JXExpressionNode expressionNode;

  public JXAttributeBindingNode(int slot, JXExpressionNode expressionNode) {
    this.slot = slot;
    this.expressionNode = expressionNode;
  }

  @Override
  public void executeVoid(VirtualFrame frame) {

    frame.setObject(slot, expressionNode.executeGeneric(frame));
  }
}
