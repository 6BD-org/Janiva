package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.JXPartialLambda;
import com.oracle.truffle.jx.statics.lambda.LambdaTemplate;
import java.util.List;

public class JXLambdaNode extends JXExpressionNode {

  private JXLambdaExecutor executor;

  private LambdaTemplate lambdaTemplate;

  public JXLambdaNode(
      TruffleLanguage<?> language,
      LambdaTemplate template,
      JXExpressionNode evalNode) {
    this.lambdaTemplate = template;
    this.executor =
        new JXLambdaExecutor(
            language, template.getFrameDescriptor(), evalNode);
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    return new JXPartialLambda(this.executor.getCallTarget(), lambdaTemplate);
  }
}
