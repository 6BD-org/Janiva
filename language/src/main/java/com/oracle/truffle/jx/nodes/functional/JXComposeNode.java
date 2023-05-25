package com.oracle.truffle.jx.nodes.functional;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.JXComposedLambda;
import com.oracle.truffle.jx.runtime.LambdaLibrary;

public class JXComposeNode extends JXExpressionNode {

  private final JXExpressionNode[] children;

  public JXComposeNode(JXExpressionNode[] children) {
    this.children = children;
    for (JXExpressionNode child : children) {
      this.insert(child);
    }
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    Object[] lambdas = new Object[children.length];
    LambdaLibrary lambdaLibrary = LambdaLibrary.getUncached();
    int i = 0;
    for (JXExpressionNode child : children) {
      Object res = child.executeGeneric(frame);
      assert lambdaLibrary.isLambda(res);
      lambdas[i] = res;
      i++;
    }
    return new JXComposedLambda(lambdas);
  }
}
