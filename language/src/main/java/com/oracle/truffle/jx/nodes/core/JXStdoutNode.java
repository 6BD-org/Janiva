package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.jx.JXException;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.system.IOUtils;
import java.io.IOException;

@NodeChild("child")
public abstract class JXStdoutNode extends JXExpressionNode {

  @Specialization
  public Object executeObject(Object v) {
    try {
      IOUtils.writeJanivaObjectIntoStream(System.out, v);
      return v;
    } catch (IOException e) {
      throw new JXException("Get io exception when writing to stdout", this);
    }
  }
}
