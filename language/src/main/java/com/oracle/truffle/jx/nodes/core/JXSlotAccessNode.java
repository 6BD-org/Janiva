package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.dsl.NodeField;import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.nodes.JXExpressionNode;

@NodeField(name = "slot", type = int.class)
@NodeField(name = "name", type = TruffleString.class)
public abstract class JXSlotAccessNode extends JXExpressionNode {

  abstract int getSlot();
  abstract TruffleString getName();

  @Specialization
  public Object executeInt(VirtualFrame frame) {
    return frame.getObject(getSlot());
  }

}
