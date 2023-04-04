package com.oracle.truffle.jx.statics.lambda;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.nodes.JXExpressionNode;

import java.util.ArrayList;
import java.util.List;

public class LambdaTemplate {
  private List<TruffleString> parameterNames;
  private TruffleString name;
  private JXExpressionNode body;
  private FrameDescriptor descriptor;

  public LambdaTemplate(TruffleString name) {
    this.parameterNames = new ArrayList<>();
    this.name = name;
  }

  public TruffleString getName() {
    return this.name;
  }

  public void addFormalParam(TruffleString paramName) {
    this.parameterNames.add(paramName);
  }

  public int parameterCount() {
    return parameterNames.size();
  }

  public List<TruffleString> getParameterNames() {
    return this.parameterNames;
  }

  public void finish(LambdaRegistry registry, FrameDescriptor descriptor) {
    registry.register(this.name, this);
    this.descriptor = descriptor;
  }

  public void addBody(JXExpressionNode body) {
    this.body = body;
  }

  public JXExpressionNode getBody() {
    return this.body;
  }

  public FrameDescriptor getFrameDescriptor() {
    return this.descriptor;
  }
}
