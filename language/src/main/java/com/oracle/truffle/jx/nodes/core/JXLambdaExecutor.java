package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.jx.nodes.JXExpressionNode;

public class JXLambdaExecutor extends RootNode {
  private JXExpressionNode evalNode;

  protected JXLambdaExecutor(
      TruffleLanguage<?> language, FrameDescriptor frameDescriptor, JXExpressionNode evalNode) {
    super(language, frameDescriptor);
    this.evalNode = evalNode;
    this.insert(evalNode);
  }

  @Override
  public Object execute(VirtualFrame frame) {
    return evalNode.executeGeneric(frame);
  }
}
