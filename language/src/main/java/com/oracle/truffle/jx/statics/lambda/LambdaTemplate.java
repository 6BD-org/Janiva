package com.oracle.truffle.jx.statics.lambda;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.JXException;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import java.util.ArrayList;
import java.util.List;

public class LambdaTemplate {

  public boolean isBuiltIn() {
    return isBuiltIn;
  }

  public void setBuiltIn(boolean builtIn) {
    isBuiltIn = builtIn;
  }

  enum State {
    DEFINED,
    FINALIZED
  }

  private final List<TruffleString> parameterNames;
  private final TruffleString name;
  private JXExpressionNode body;
  private FrameDescriptor descriptor;

  /** State is used for early expose of partially defined lambdas */
  private volatile State state;

  private boolean isBuiltIn = false;

  public LambdaTemplate(TruffleString name) {
    this.parameterNames = new ArrayList<>();
    this.name = name;
    this.state = State.DEFINED;
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

  public void finish(FrameDescriptor descriptor) {
    if (this.state == State.FINALIZED) {
      throw new JXException("Cannot re-finalize lambda: " + this.name);
    }
    this.descriptor = descriptor;
    this.state = State.FINALIZED;
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

  public void throwParameterLenNotMatch(int actual) {
    throw new JXException(
        "Parameter length does not match, expecting: "
            + this.parameterCount()
            + ", getting: "
            + actual);
  }
}
