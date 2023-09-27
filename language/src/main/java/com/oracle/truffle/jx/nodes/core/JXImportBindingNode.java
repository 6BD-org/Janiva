package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.jx.JXException;
import com.oracle.truffle.jx.nodes.JXStatementNode;
import com.oracle.truffle.jx.runtime.io.JXExported;
import java.util.function.Supplier;

public class JXImportBindingNode extends JXStatementNode {

  private final int slot;
  private final RootNode rn;
  private final Supplier<RootNode> currentRoot;

  public JXImportBindingNode(int slot, RootNode rn, Supplier<RootNode> currentRoot) {
    this.slot = slot;
    this.rn = rn;
    this.currentRoot = currentRoot;
  }

  @Override
  public void executeVoid(VirtualFrame frame) {
    var lib = InteropLibrary.getFactory().getUncached();
    Object imported = rn.execute(frame);
    if (lib.isExecutable(imported)) {
      try {
        imported = lib.execute(imported);
      } catch (ArityException | UnsupportedMessageException | UnsupportedTypeException e) {
        throw new JXException(e.getMessage(), this);
      }
    }
    if (imported instanceof JXExported) {
      frame.setObject(slot, ((JXExported) imported).getValue());
    } else {
      throw new JXException("Value cannot be imported: " + imported.getClass(), this);
    }
    // We would like to re-adopt child nodes when importing AST
    // from another janiva program
    this.currentRoot.get().adoptChildren();
  }
}
