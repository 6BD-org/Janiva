package com.oracle.truffle.jx.nodes.functional;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.JXComposedLambda;
import com.oracle.truffle.jx.runtime.LambdaLibrary;
import java.util.List;
import java.util.stream.Collectors;

public class JXComposeNode extends JXExpressionNode {

  private List<JXExpressionNode> children;

  public JXComposeNode(List<JXExpressionNode> children) {
    this.children = children;
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    var children =
        this.children.stream().map(c -> c.executeGeneric(frame)).collect(Collectors.toList());
    Object[] lambdas = new Object[children.size()];
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
