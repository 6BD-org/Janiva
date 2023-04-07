package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.JXObject;
import com.oracle.truffle.jx.runtime.io.JXExported;

@NodeChild("child")
public abstract class JXExportNode extends JXExpressionNode {

  @Specialization
  public Object exportObject(Object child) {
    return new JXExported(child);
  }
}
