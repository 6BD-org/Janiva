package com.oracle.truffle.jx.nodes.functional;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.JXComposedLambda;
import com.oracle.truffle.jx.runtime.LambdaLibrary;

public class JXComposeNode extends JXExpressionNode {

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    var children = frame.getArguments();
    Object[] lambdas = new Object[children.length];
    LambdaLibrary lambdaLibrary = LambdaLibrary.getUncached();
    int i = 0;
    for (Object child : children) {
      assert lambdaLibrary.isLambda(child);
      lambdas[i] = child;
      i++;
    }
    return new JXComposedLambda(lambdas);
  }
}
