package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.jx.JanivaLang;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.JXPartialLambda;
import com.oracle.truffle.jx.statics.lambda.LambdaTemplate;

@NodeField(name = "lambdaTemplate", type = LambdaTemplate.class)
public abstract class JXLambdaNode extends JXExpressionNode {

  protected abstract LambdaTemplate getLambdaTemplate();

  @Specialization
  public Object executeSpecialized(VirtualFrame frame) {
    JXLambdaExecutor executor =
        new JXLambdaExecutor(
            JanivaLang.get(this),
            getLambdaTemplate().getFrameDescriptor(),
            getLambdaTemplate().getBody());
    return new JXPartialLambda(executor.getCallTarget(), getLambdaTemplate());
  }
}
