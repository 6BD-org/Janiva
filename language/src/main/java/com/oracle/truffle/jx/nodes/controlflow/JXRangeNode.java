package com.oracle.truffle.jx.nodes.controlflow;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.interop.InteropLibrary;import com.oracle.truffle.api.interop.UnsupportedMessageException;import com.oracle.truffle.api.library.CachedLibrary;import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.JXArray;
import com.oracle.truffle.jx.runtime.JXBigNumber;
import com.oracle.truffle.jx.runtime.JXContext;
import com.oracle.truffle.jx.runtime.view.JXIntegerRangeArrayView;
import com.oracle.truffle.jx.runtime.view.JXStringArrayView;

public abstract class JXRangeNode extends JXExpressionNode {

  @Specialization(guards = "isArray(frame)")
  public Object doArray(VirtualFrame frame) {
    // simply return a reference to it
    return frame.getArguments()[0];
  }

  @Specialization(guards = "isNumber(frame)", rewriteOn = UnsupportedMessageException.class)
  public Object doLong(
          VirtualFrame frame,
          @CachedLibrary(limit = "1") InteropLibrary library
  )throws UnsupportedMessageException {
    return new JXIntegerRangeArrayView(library.asInt(frame.getArguments()[0]));
  }

  @Specialization(guards = "isString(frame)", rewriteOn = UnsupportedMessageException.class)
  public Object doString(
          VirtualFrame frame,
          @CachedLibrary(limit = "1") InteropLibrary library
  )throws UnsupportedMessageException {
    return new JXStringArrayView(library.asTruffleString(frame.getArguments()[0]));
  }

  protected boolean isArray(VirtualFrame virtualFrame) {
    return virtualFrame.getArguments()[0] instanceof JXArray;
  }

  protected boolean isNumber(VirtualFrame virtualFrame) {
    return virtualFrame.getArguments()[0]  instanceof JXBigNumber;
  }

  protected boolean isString(VirtualFrame virtualFrame) {
    return virtualFrame.getArguments()[0]  instanceof TruffleString;
  }

  final AllocationReporter lookup() {
    return JXContext.get(this).getAllocationReporter();
  }
}
