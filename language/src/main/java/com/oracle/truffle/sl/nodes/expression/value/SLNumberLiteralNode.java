package com.oracle.truffle.sl.nodes.expression.value;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import org.graalvm.compiler.nodeinfo.NodeInfo;

import java.math.BigDecimal;

@NodeInfo(shortName = "j_number")
public class SLNumberLiteralNode extends SLExpressionNode {

  private final BigDecimal val;

  public SLNumberLiteralNode(BigDecimal val) {
    this.val = val;
  }

  @Override
  public Number executeGeneric(VirtualFrame frame) {
    return val.doubleValue();
  }
}
