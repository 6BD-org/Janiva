package com.oracle.truffle.jx.nodes.expression;

import com.oracle.truffle.api.dsl.*;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.jx.JXException;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.JXPartialLambda;
import java.util.ArrayList;
import java.util.List;

public class JXFeedValueNode extends JXExpressionNode {
  private List<JXExpressionNode> args = new ArrayList<>();

  private final JXExpressionNode child;

  public JXFeedValueNode(JXExpressionNode child) {
    this.child = child;
  }

  public Object executeGeneric(VirtualFrame virtualFrame) {
    Object cValue = child.executeGeneric(virtualFrame);
    DynamicObjectLibrary library = DynamicObjectLibrary.getUncached();
    if (isPartialApplicable(cValue)) {
      // Need to clone to avoid mutating internal state of original one
      return ((JXPartialLambda) cValue)
          .clone(library)
          .mergeArgs(args.stream().map(a -> a.executeGeneric(virtualFrame)).toArray(), library);
    }
    throw new JXException("Not supported", this);
  }

  public void feed(List<JXExpressionNode> args) {
    if (args == null) return;
    this.args.addAll(args);
  }

  boolean isPartialApplicable(Object o) {
    return o instanceof JXPartialLambda;
  }
}
