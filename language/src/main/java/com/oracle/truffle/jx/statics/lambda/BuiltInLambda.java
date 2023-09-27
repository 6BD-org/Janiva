package com.oracle.truffle.jx.statics.lambda;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.controlflow.JXIfNode;
import com.oracle.truffle.jx.nodes.controlflow.JXRangeNodeGen;
import com.oracle.truffle.jx.nodes.core.*;
import com.oracle.truffle.jx.nodes.functional.JXComposeNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum BuiltInLambda implements BuiltInLambdaFactory {
  IF {
    @Override
    public JXExpressionNode create(List<JXExpressionNode> arguments, Source source) {
      return new JXIfNode(arguments.get(0), arguments.get(1), arguments.get(2));
    }

    @Override
    public TruffleString lambdaName() {
      return TruffleString.fromJavaStringUncached("if", TruffleString.Encoding.UTF_8);
    }

    @Override
    public List<TruffleString> parameters() {
      return Collections.emptyList();
    }

    @Override
    public boolean partialApplicable() {
      return false;
    }
  },
  RANGE {
    @Override
    public JXExpressionNode create(List<JXExpressionNode> arguments, Source source) {
      return JXRangeNodeGen.create();
    }

    @Override
    public TruffleString lambdaName() {
      return TruffleString.fromJavaStringUncached("range", TruffleString.Encoding.UTF_8);
    }

    @Override
    public List<TruffleString> parameters() {
      return Arrays.asList(s("target"));
    }
  },
  EXPORT {
    @Override
    public JXExpressionNode create(List<JXExpressionNode> arguments, Source source) {
      return JXExportNodeGen.create();
    }

    @Override
    public TruffleString lambdaName() {
      return TruffleString.fromJavaStringUncached("export", TruffleString.Encoding.UTF_8);
    }

    @Override
    public List<TruffleString> parameters() {
      return Arrays.asList(s("target"));
    }
  },

  STDOUT {
    @Override
    public JXExpressionNode create(List<JXExpressionNode> arguments, Source source) {
      return JXStdoutNodeGen.create();
    }

    @Override
    public TruffleString lambdaName() {
      return TruffleString.fromJavaStringUncached("stdout", TruffleString.Encoding.UTF_8);
    }

    @Override
    public List<TruffleString> parameters() {
      return Arrays.asList(s("target"));
    }
  },
  COMPOSE {

    @Override
    public JXExpressionNode create(List<JXExpressionNode> arguments, Source source) {
      return new JXComposeNode(arguments);
    }

    @Override
    public TruffleString lambdaName() {
      return TruffleString.fromJavaStringUncached("compose", TruffleString.Encoding.UTF_8);
    }

    @Override
    public List<TruffleString> parameters() {
      return Collections.emptyList();
    }

    @Override
    public boolean partialApplicable() {
      return false;
    }
  };

  private static TruffleString s(String s) {
    return TruffleString.fromJavaStringUncached(s, TruffleString.Encoding.UTF_8);
  }

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
