package com.oracle.truffle.jx.nodes.expression;

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.jx.JXException;
import com.oracle.truffle.jx.constants.CacheLimits;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.LambdaLibrary;
import java.util.ArrayList;
import java.util.List;

@NodeChild("child")
public abstract class JXFeedValueNode extends JXExpressionNode {

  private final Object[] EMPTY_ARGS = new Object[] {};
  private final List<JXExpressionNode> args = new ArrayList<>();

  /**
   * @return a partially applied lambda or a value if the lambda is executable after this operation
   * @throws AssertionError when the root node is exchanged (This could happen when applying an
   *     exported lambda)
   */
  @Specialization(limit = "10")
  public Object executeSpecialized(
      VirtualFrame virtualFrame,
      Object child,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    return doExecute(virtualFrame, child, lambdaLibrary);
  }

  protected Object doExecute(VirtualFrame virtualFrame, Object child, LambdaLibrary lambdaLibrary) {
    if (isPartialApplicable(lambdaLibrary, child)) {
      // Need to clone to avoid mutating internal state of original one
      Object cloned = lambdaLibrary.cloneLambda(child);
      return lambdaLibrary.mergeArgs(
          cloned, args.stream().map(a -> a.executeGeneric(virtualFrame)).toArray());
    }
    throw new JXException("Not supported: " + child.getClass(), this);
  }

  public JXFeedValueNode feed(List<JXExpressionNode> args) {
    if (args == null) return this;
    this.args.addAll(args);
    for (JXExpressionNode arg : args) {
      this.insert(arg);
    }
    return this;
  }

  boolean isPartialApplicable(LambdaLibrary lambdaLibrary, Object o) {
    return lambdaLibrary.isLambda(o);
  }
}
