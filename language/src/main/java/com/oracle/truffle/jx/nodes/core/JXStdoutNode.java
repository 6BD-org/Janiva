package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.*;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.jx.JXException;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.io.IOUtils;
import java.io.IOException;

@NodeChild("child")
public abstract class JXStdoutNode extends JXExpressionNode {
  private static final String STDOUT_LIB_LIMIT = "5";

  @Specialization(limit = STDOUT_LIB_LIMIT)
  public Object executeObject(
          Object v
  ) {
    try {
      IOUtils.writeJanivaObjectIntoStream(InteropLibrary.getUncached(), System.out, v);
      return v;
    } catch (IOException | UnsupportedMessageException | UnsupportedTypeException | ArityException |
             InvalidArrayIndexException | UnknownIdentifierException e) {
      throw new JXException(e.getMessage(), this);
    }
  }
}
