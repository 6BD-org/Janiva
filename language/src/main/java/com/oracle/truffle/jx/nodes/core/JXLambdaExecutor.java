package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.JXPartialLambda;
import java.util.List;

public class JXLambdaExecutor extends RootNode {
  private JXExpressionNode evalNode;

  protected JXLambdaExecutor(
      TruffleLanguage<?> language,
      FrameDescriptor frameDescriptor,
      JXExpressionNode evalNode) {
    super(language, frameDescriptor);
    this.evalNode = evalNode;
  }

  @Override
  public Object execute(VirtualFrame frame) {
    return evalNode.executeGeneric(frame);
  }
}
