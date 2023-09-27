package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.io.JXExported;

public abstract class JXExportNode extends JXExpressionNode {

  @Specialization
  public Object exportObject(VirtualFrame frame) {
    return new JXExported(frame.getArguments()[0]);
  }
}
