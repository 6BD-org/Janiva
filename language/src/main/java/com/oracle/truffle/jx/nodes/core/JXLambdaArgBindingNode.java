package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.JXStatementNode;

public class JXLambdaArgBindingNode extends JXStatementNode {

  private int offset;
  private JXExpressionNode val;

  public JXLambdaArgBindingNode(int offset, JXExpressionNode val) {
    this.offset = offset;
    this.val = val;
  }

  @Override
  public void executeVoid(VirtualFrame frame) {
    frame.getArguments()[offset] = val.executeGeneric(frame);
  }
}
