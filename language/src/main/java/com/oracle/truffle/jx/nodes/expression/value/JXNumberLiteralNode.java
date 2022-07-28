package com.oracle.truffle.jx.nodes.expression.value;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.JXBigNumber;
import org.graalvm.compiler.nodeinfo.NodeInfo;

import java.math.BigDecimal;

@NodeInfo(shortName = "j_number")
public class JXNumberLiteralNode extends JXExpressionNode {

  private final BigDecimal val;

  private final boolean hasDecimal;

  public JXNumberLiteralNode(BigDecimal val, boolean hasDecimal) {
    this.val = val;
    this.hasDecimal = hasDecimal;
  }

  public boolean hasDecimal() {
    return hasDecimal;
  }

  @Override
  public JXBigNumber executeGeneric(VirtualFrame frame) {
    return new JXBigNumber(val);
  }
}
