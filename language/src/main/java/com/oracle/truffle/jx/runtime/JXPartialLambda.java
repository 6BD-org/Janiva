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
@ExportLibrary(LambdaLibrary.class)
public class JXPartialLambda extends DynamicObject implements TruffleObject {
  private static final Shape SHAPE = Shape.newBuilder().layout(JXPartialLambda.class).build();

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
  public Object execute(Object[] args) {
    return callTarget.call(this.partialArgs);
  }

  @ExportMessage(library = LambdaLibrary.class)
  public JXPartialLambda mergeArgs(Object[] args) {
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
    return this;
  }

  @ExportMessage(library = LambdaLibrary.class)
  public JXPartialLambda cloneLambda() {
    JXPartialLambda res = new JXPartialLambda(this.callTarget, this.template);
    res.offset = this.offset;
    // values must be immutable
    if (offset >= 0) System.arraycopy(this.partialArgs, 0, res.partialArgs, 0, offset);
    return res;
  }

  @ExportMessage(library = LambdaLibrary.class)
  public boolean isLambda() {
    return true;
  }

  private Object[] getArgs() {
    return this.partialArgs;
  }
}
