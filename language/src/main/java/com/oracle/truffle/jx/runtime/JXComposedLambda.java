package com.oracle.truffle.jx.runtime;

import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.Shape;import java.util.List;

public class JXComposedLambda extends DynamicObject implements TruffleObject, JXAbstractLambda{
    private static final Shape SHAPE = Shape.newBuilder().layout(JXComposedLambda.class).build();

  protected JXComposedLambda(List<JXAbstractLambda> composedLambdas) {
    super(SHAPE);
  }

  @Override
  public JXPartialLambda clone(DynamicObjectLibrary dynamicObjectLibrary) {
    return null;
  }

  @Override
  public JXPartialLambda mergeArgs(Object[] args, DynamicObjectLibrary library) {
    return null;
  }

  @Override
  public Object execute(Object[] args, DynamicObjectLibrary library) {
    return null;
  }

  @Override
  public boolean isExecutable() {
    return false;
  }
}
