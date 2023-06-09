package com.oracle.truffle.jx.nodes.util;

import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.jx.runtime.JXObject;
import java.util.function.Supplier;
import org.graalvm.polyglot.Value;

public abstract class ObjectUtils {
  public static JXObject create(
      DynamicObjectLibrary library, Supplier<JXObject> objectSupplier, Value src) {
    JXObject ob = objectSupplier.get();
    for (String key : src.getMemberKeys()) {
      library.put(ob, key, src.getMember(key));
    }
    return ob;
  }
}
