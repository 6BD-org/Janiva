package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.nodes.JXExpressionNode;

public class JXValueAccessNode extends JXExpressionNode {

  private final int slot;
  private final TruffleString name;

  public JXValueAccessNode(int slot, TruffleString name) {
    this.slot = slot;
    this.name = name;
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    return frame.getObject(slot);
  }

  public int getSlot() {
    return slot;
  }

  public TruffleString getName() {
    return name;
  }
}
