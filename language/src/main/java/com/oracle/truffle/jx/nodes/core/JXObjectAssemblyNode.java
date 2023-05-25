package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.jx.builtins.JXNewObjectBuiltin;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.JXStatementNode;
import com.oracle.truffle.jx.runtime.JXObject;
import java.util.List;

@NodeChild
public class JXObjectAssemblyNode extends JXExpressionNode {
  private final List<JXStatementNode> bindings;
  private final List<JXSlotAccessNode> accessors;
  private final JXNewObjectBuiltin newObjectBuiltin;

  public JXObjectAssemblyNode(
      List<JXStatementNode> bindings,
      List<JXSlotAccessNode> accessors,
      JXNewObjectBuiltin newObjectBuiltin) {
    this.accessors = accessors;
    this.newObjectBuiltin = newObjectBuiltin;
    this.bindings = bindings;
    bindings.forEach(this::insert);
    accessors.forEach(this::insert);
    this.insert(newObjectBuiltin);
    // Need to adopt children in order to support cached library
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    DynamicObjectLibrary dynamicObjectLibrary = DynamicObjectLibrary.getFactory().getUncached();
    JXObject jxObject = (JXObject) newObjectBuiltin.executeGeneric(frame);
    for (JXStatementNode bindingNode : bindings) {
      if (bindingNode instanceof JXAttributeBindingNode) {
        if (((JXAttributeBindingNode) bindingNode).isLatent()) {
          // don't assemble latent nodes
        }
      }
      bindingNode.executeVoid(frame);
    }
    for (JXSlotAccessNode n : accessors) {
      dynamicObjectLibrary.put(jxObject, n.getName(), n.executeGeneric(frame));
    }
    return jxObject;
  }
}
