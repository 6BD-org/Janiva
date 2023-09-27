package com.oracle.truffle.jx.nodes.expression;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.jx.nodes.JXBinaryNode;
import com.oracle.truffle.jx.runtime.JXBigNumber;

public abstract class JXModuloNode extends JXBinaryNode {
  @Specialization
  public Object executeInteger(JXBigNumber left, JXBigNumber right) {
    return new JXBigNumber(left.intValue() % right.intValue());
  }
}
