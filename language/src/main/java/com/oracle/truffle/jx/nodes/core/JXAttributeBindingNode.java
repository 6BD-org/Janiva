package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.jx.JXException;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.JXStatementNode;

public class JXAttributeBindingNode extends JXStatementNode {

  private final int slot;
  private final JXExpressionNode expressionNode;
  private final boolean latent;


  public JXAttributeBindingNode(int slot, JXExpressionNode expressionNode) {
    this.slot = slot;
    this.expressionNode = expressionNode;
    this.latent = false;
  }

  /**
   * Create a Attribute binding node with latent parameter
   * @param slot
   * @param expressionNode
   * @param latent if an attribute is latent, it's hidden from final output, but can be referred by other attributes
   */
  public JXAttributeBindingNode(int slot, JXExpressionNode expressionNode, boolean latent) {
    this.slot = slot;
    this.expressionNode = expressionNode;
    this.latent = latent;
  }



  @Override
  public void executeVoid(VirtualFrame frame) {
    if (!latent) {
      if (frame.getObject(slot) != null) {
        throw new JXException("Cannot re-bind a non-latent variable: " + slot, this);
      }
      frame.setObject(slot, expressionNode.executeGeneric(frame));
    } else {
      // Latent variable can be bound multiple times
      frame.setObject(slot, expressionNode.executeGeneric(frame));
    }
  }

  public boolean isLatent() {
    return this.latent;
  }

  public int getSlot() {
    return this.slot;
  }
}
