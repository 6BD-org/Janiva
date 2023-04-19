package com.oracle.truffle.jx.nodes.expression;

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.jx.JXException;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.JXPartialLambda;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class JXFeedValueNode extends JXExpressionNode {

  private Object[] EMPTY_ARGS = new Object[] {};
  private List<JXExpressionNode> args = new ArrayList<>();

  private final Supplier<JXExpressionNode> child;
  private final DynamicObjectLibrary library;

  public JXFeedValueNode(Supplier<JXExpressionNode> child) {
    this.child = child;
    this.library = DynamicObjectLibrary.getUncached();
  }

  public Object executeGeneric(VirtualFrame virtualFrame) {
    Object cValue = child.get().executeGeneric(virtualFrame);
    DynamicObjectLibrary library = DynamicObjectLibrary.getUncached();
    if (isPartialApplicable(cValue)) {
      // Need to clone to avoid mutating internal state of original one
      JXPartialLambda res =
          ((JXPartialLambda) cValue)
              .clone(library)
              .mergeArgs(args.stream().map(a -> a.executeGeneric(virtualFrame)).toArray(), library);
      if (res.isExecutable()) {
        // automatically evaluate
        return res.execute(EMPTY_ARGS, library);
      } else {
        return res;
      }
    }
    throw new JXException("Not supported: " + cValue.getClass(), this);
  }

  public void feed(List<JXExpressionNode> args) {
    if (args == null) return;
    this.args.addAll(args);
  }

  boolean isPartialApplicable(Object o) {
    return o instanceof JXPartialLambda;
  }
}
