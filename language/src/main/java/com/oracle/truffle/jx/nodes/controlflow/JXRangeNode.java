package com.oracle.truffle.jx.nodes.controlflow;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.JXArray;
import com.oracle.truffle.jx.runtime.JXBigNumber;
import com.oracle.truffle.jx.runtime.JXContext;
import com.oracle.truffle.jx.runtime.view.JXIntegerRangeArrayView;
import com.oracle.truffle.jx.runtime.view.JXStringArrayView;

@NodeChild("o")
public abstract class JXRangeNode extends JXExpressionNode {

  @Specialization(guards = "isArray(o)")
  public Object doArray(JXArray o) {
    // simply return a reference to it
    return o;
  }

  @CompilerDirectives.TruffleBoundary
  @Specialization(guards = "isNumber(o)")
  public Object doLong(JXBigNumber o, @Cached("lookup()") AllocationReporter reporter) {
    return new JXIntegerRangeArrayView(o.intValue());
  }

  @Specialization(guards = "isString(o)")
  @CompilerDirectives.TruffleBoundary
  public Object doString(TruffleString o, @Cached("lookup()") AllocationReporter reporter) {
    return new JXStringArrayView(o);
  }

  protected boolean isArray(Object o) {
    return o instanceof JXArray;
  }

  protected boolean isNumber(Object o) {
    return o instanceof JXBigNumber;
  }

  protected boolean isString(Object o) {
    return o instanceof TruffleString;
  }

  final AllocationReporter lookup() {
    return JXContext.get(this).getAllocationReporter();
  }
}
