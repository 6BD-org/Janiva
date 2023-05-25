package com.oracle.truffle.jx.statics.lambda;

import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.parser.exceptions.JXSyntaxError;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LambdaRegistry {

  private static final Logger logger = LoggerFactory.getLogger(LambdaRegistry.class);
  private static final Map<TruffleString, LambdaRegistry> registries = new ConcurrentHashMap<>();
  // TODO: namespacing
  public static LambdaRegistry getInstance(TruffleString namespace) {
    if (!registries.containsKey(namespace)) {
      logger.debug("Initializing lambda registry in namespace {}", namespace);
      registries.put(namespace, new LambdaRegistry());
    }
    return registries.get(namespace);
  }

  private final Map<TruffleString, LambdaTemplate> registrations = new HashMap<>();
  private static final Map<TruffleString, LambdaTemplate> builtIn = new ConcurrentHashMap<>();

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

  public static void registerBuiltIn(LambdaTemplate template) {
    builtIn.putIfAbsent(template.getName(), template);
  }

  public boolean isBuiltIn(TruffleString name) {
    return builtIn.containsKey(name);
  }
}
