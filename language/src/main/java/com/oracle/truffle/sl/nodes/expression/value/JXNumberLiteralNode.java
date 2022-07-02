package com.oracle.truffle.sl.nodes.expression.value;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.JXExpressionNode;
import org.graalvm.compiler.nodeinfo.NodeInfo;

import java.math.BigDecimal;

@NodeInfo(shortName = "j_number")
public class JXNumberLiteralNode extends JXExpressionNode {

  private final BigDecimal val;

  public JXNumberLiteralNode(BigDecimal val) {
    this.val = val;
  }

  @Override
  public Number executeGeneric(VirtualFrame frame) {
    return val.doubleValue();
  }
}
