package com.oracle.truffle.jx.nodes.expression;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;import com.oracle.truffle.api.interop.UnsupportedTypeException;import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.jx.JXException;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.JXPartialLambda;
import com.oracle.truffle.jx.runtime.LambdaLibrary;import org.graalvm.polyglot.PolyglotException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@NodeChild("child")
public abstract class JXFeedValueNode extends JXExpressionNode {

  private final Object[] EMPTY_ARGS = new Object[] {};
  private final List<JXExpressionNode> args = new ArrayList<>();

  /**
   *
   * @return a partially applied lambda or a value if the lambda is executable after this operation
   * @throws AssertionError when the root node is exchanged (This could happen when applying an exported lambda)
   */
  @Specialization(limit = "3", rewriteOn = AssertionError.class)
  public Object executeSpecialized(
          VirtualFrame virtualFrame,
          DynamicObject child
          , @CachedLibrary ("child") InteropLibrary library,
          @CachedLibrary("child") LambdaLibrary lambdaLibrary
  ) throws AssertionError {
    return doExecute(virtualFrame, child, library, lambdaLibrary);
  }

  @Specialization
  public Object executeUncached(
          VirtualFrame virtualFrame,
          DynamicObject child
  ) {
    InteropLibrary library = InteropLibrary.getUncached();
    return doExecute(virtualFrame, child, library, LambdaLibrary.getUncached());
  }

  protected Object doExecute(
      VirtualFrame virtualFrame,
      DynamicObject child,
      InteropLibrary library,
      LambdaLibrary lambdaLibrary) {
    if (isPartialApplicable(child)) {
      // Need to clone to avoid mutating internal state of original one
      Object cloned = lambdaLibrary.cloneLambda(child);
      Object res = lambdaLibrary.mergeArgs(cloned, args.stream().map(a -> a.executeGeneric(virtualFrame)).toArray());
      if (library.isExecutable(res)) {
        // automatically evaluate
        try{
          return library.execute(res);
        } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
          throw new RuntimeException(e);
        }
      } else {
        return res;
      }
    }
    throw new JXException("Not supported: " + child.getClass(), this);
  }

  public JXFeedValueNode feed(List<JXExpressionNode> args) {
    if (args == null) return this;
    this.args.addAll(args);
    return this;
  }

  boolean isPartialApplicable(Object o) {
    return o instanceof JXPartialLambda;
  }
}
