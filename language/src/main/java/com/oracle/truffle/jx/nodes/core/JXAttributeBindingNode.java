package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.jx.JXException;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.JXStatementNode;

@NodeChild(value = "val")
@NodeField(name = "slot", type = int.class)
@NodeField(name = "latent", type = boolean.class)
public abstract class JXAttributeBindingNode extends JXExpressionNode {

  abstract int getSlot();
  abstract boolean isLatent();

  @Specialization
  public Object executeVal(VirtualFrame frame, Object val) {
    if (!isLatent()) {
      if (frame.getObject(getSlot()) != null) {
        // should be able to re-use frame
        // throw new JXException("Cannot re-bind a non-latent variable: " + getSlot(), this);
      }
      frame.setObject(getSlot(), val);
    } else {
      // Latent variable can be bound multiple times
      frame.setObject(getSlot(), val);
    }
    return val;
  }
}
