package com.oracle.truffle.jx.runtime;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.interop.*;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;import com.oracle.truffle.jx.JXException;import com.oracle.truffle.jx.statics.lambda.LambdaTemplate;import java.util.List;

@ExportLibrary(InteropLibrary.class)
public class JXPartialLambda implements TruffleObject {

  private CallTarget callTarget;
  private Object[] partialArgs;
  private int offset;
  private LambdaTemplate template;

  public JXPartialLambda(CallTarget callTarget, LambdaTemplate template) {
    this.callTarget = callTarget;
    this.partialArgs = new Object[template.parameterCount()];
    this.offset = 0;
    this.template = template;
  }

  @ExportMessage
  public boolean isExecutable() {
    return true;
  }

  @ExportMessage
  public Object execute(Object[] args) {
    mergeArgs(args);
    return callTarget.call(partialArgs);
  }

  private void mergeArgs(Object[] args) {
    if (args.length + offset > template.parameterCount()) {
      template.throwParameterLenNotMatch(args.length + offset);
    }
    for (Object arg : args) {
      partialArgs[offset] = arg;
      offset ++;
    }
  }
}
