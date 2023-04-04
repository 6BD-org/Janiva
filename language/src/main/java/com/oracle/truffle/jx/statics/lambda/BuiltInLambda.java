package com.oracle.truffle.jx.statics.lambda;

import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.core.*;
import com.oracle.truffle.jx.parser.exceptions.JXSyntaxError;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum BuiltInLambda implements BuiltInLambdaFactory {
  IF {
    @Override
    public JXExpressionNode create(List<JXExpressionNode> arguments) {
      return new JXIfNode(arguments.get(0), arguments.get(1), arguments.get(2));
    }

    @Override
    public TruffleString lambdaName() {
      return TruffleString.fromJavaStringUncached("if", TruffleString.Encoding.UTF_8);
    }
  },
  RANGE {
    @Override
    public JXExpressionNode create(List<JXExpressionNode> arguments) {
      return JXRangeNodeGen.create(arguments.get(0));
    }

    @Override
    public TruffleString lambdaName() {
      return TruffleString.fromJavaStringUncached("range", TruffleString.Encoding.UTF_8);
    }
  },
  EXPORT {
    @Override
    public JXExpressionNode create(List<JXExpressionNode> arguments) {
      return JXExportNodeGen.create(arguments.get(0));
    }

    @Override
    public TruffleString lambdaName() {
      return TruffleString.fromJavaStringUncached("export", TruffleString.Encoding.UTF_8);
    }
  },

  STDOUT {
    @Override
    public JXExpressionNode create(List<JXExpressionNode> arguments) {
      return JXStdoutNodeGen.create(arguments.get(0));
    }

    @Override
    public TruffleString lambdaName() {
      return TruffleString.fromJavaStringUncached("stdout", TruffleString.Encoding.UTF_8);
    }
  };

  static final Map<TruffleString, BuiltInLambda> cache = new ConcurrentHashMap<>();

  public static BuiltInLambda valueOf(TruffleString ts) {
    if (!cache.containsKey(ts)) {
      for (BuiltInLambda bl : BuiltInLambda.values()) {
        if (bl.lambdaName().equals(ts)) {
          cache.put(bl.lambdaName(), bl);
        }
      }
    }
    return cache.get(ts);
  }
}
