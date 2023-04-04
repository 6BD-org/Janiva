package com.oracle.truffle.jx.statics.lambda;

import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.parser.exceptions.JXSyntaxError;

import java.util.HashMap;
import java.util.Map;

public class LambdaRegistry {

  private static final LambdaRegistry instance = new LambdaRegistry();

  public static LambdaRegistry getInstance() {
    return instance;
  }

  private final Map<TruffleString, LambdaTemplate> registrations = new HashMap<>();
  private final Map<TruffleString, LambdaTemplate> builtIn = new HashMap<>();

  public LambdaTemplate lookupLambdaBody(TruffleString lambdaName) {
    return registrations.get(lambdaName);
  }

  public void register(TruffleString name, LambdaTemplate template) {
    if (builtIn.containsKey(name)) {
      throw new JXSyntaxError("Cannot register built-in lambdas");
    }
    if (registrations.containsKey(name)) {
      throw new JXSyntaxError("lambda overloading is not supported");
    }
    registrations.put(name, template);
  }

  public void registerBuiltIn(LambdaTemplate template) {
    builtIn.put(template.getName(), template);
  }

  public boolean isBuiltIn(TruffleString name) {
    return this.builtIn.containsKey(name);
  }

  private static TruffleString asTs(String s) {
    return TruffleString.fromJavaStringUncached(s, TruffleString.Encoding.UTF_8);
  }
}
