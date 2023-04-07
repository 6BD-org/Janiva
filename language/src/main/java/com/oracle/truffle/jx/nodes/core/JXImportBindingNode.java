package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.jx.JXException;
import com.oracle.truffle.jx.nodes.JXStatementNode;
import com.oracle.truffle.jx.runtime.io.JXExported;

public class JXImportBindingNode extends JXStatementNode {

  private final int slot;
  private final RootNode rn;

  public JXImportBindingNode(int slot, RootNode rn) {
    this.slot = slot;
    this.rn = rn;
  }

  @Override
  public void executeVoid(VirtualFrame frame) {
    Object imported = rn.execute(frame);
    if (imported instanceof JXExported) {
      frame.setObject(slot, ((JXExported) imported).getValue());
    } else {
      throw new JXException("Value cannot be imported: " + imported.getClass(), this);
    }
  }
}
