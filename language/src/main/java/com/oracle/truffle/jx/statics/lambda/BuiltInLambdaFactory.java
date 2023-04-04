package com.oracle.truffle.jx.statics.lambda;

import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.nodes.JXExpressionNode;

import java.util.List;

/** This is a factory that constructs node from built-in lambda invocations */
public interface BuiltInLambdaFactory {
  JXExpressionNode create(List<JXExpressionNode> arguments);

  TruffleString lambdaName();
}
