package com.oracle.truffle.jx.runtime;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.interop.*;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.statics.lambda.LambdaTemplate;

@ExportLibrary(InteropLibrary.class)
public class JXPartialLambda extends DynamicObject implements TruffleObject, JXAbstractLambda {

  private static final TruffleString PROP_OFFSET =
      TruffleString.fromJavaStringUncached("__OFFSET", TruffleString.Encoding.UTF_8);
  private static final TruffleString PROP_ARGS =
      TruffleString.fromJavaStringUncached("__ARGS", TruffleString.Encoding.UTF_8);

  private static final Shape SHAPE = Shape.newBuilder().layout(JXPartialLambda.class).build();

  @DynamicField private int _i1;
  @DynamicField private Object _o1;

  private CallTarget callTarget;
  private Object[] partialArgs;
  private int offset;
  private LambdaTemplate template;

  public JXPartialLambda(CallTarget callTarget, LambdaTemplate template) {
    super(SHAPE);
    this.callTarget = callTarget;
    this.partialArgs = new Object[template.parameterCount()];
    this.offset = 0;
    this.template = template;
  }

  @ExportMessage
  public boolean isExecutable() {
    return !isPartialApplication();
  }

  private boolean isPartialApplication() {
    return offset < template.parameterCount();
  }

  @ExportMessage
  public Object execute(Object[] args, @CachedLibrary("this") DynamicObjectLibrary library) {
    return callTarget.call(getArgs(library));
  }

  public JXPartialLambda mergeArgs(Object[] args, DynamicObjectLibrary library) {
    if (args.length == 0) {
      return this;
    }
    if (args.length + offset > template.parameterCount()) {
      template.throwParameterLenNotMatch(args.length + offset);
    }
    for (Object arg : args) {
      partialArgs[offset] = arg;
      offset++;
    }
    flushState(library);
    return this;
  }

  public JXPartialLambda clone(DynamicObjectLibrary dynamicObjectLibrary) {
    JXPartialLambda res = new JXPartialLambda(this.callTarget, this.template);
    try {
      res.offset = dynamicObjectLibrary.getIntOrDefault(this, PROP_OFFSET, 0);
      // values must be immutable
      if (offset >= 0)
        System.arraycopy(this.getArgs(dynamicObjectLibrary), 0, res.partialArgs, 0, offset);
    } catch (UnexpectedResultException e) {
      throw new RuntimeException(e);
    }
    res.flushState(dynamicObjectLibrary);
    return res;
  }

  private void flushState(DynamicObjectLibrary library) {
    library.put(this, PROP_ARGS, this.partialArgs);
    library.put(this, PROP_OFFSET, offset);
  }

  private Object[] getArgs(DynamicObjectLibrary library) {
    return (Object[]) library.getOrDefault(this, PROP_ARGS, new Object[] {});
  }
}
