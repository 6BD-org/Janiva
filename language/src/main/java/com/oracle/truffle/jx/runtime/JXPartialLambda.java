package com.oracle.truffle.jx.runtime;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.interop.*;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(InteropLibrary.class)
public class JXPartialLambda implements TruffleObject {

  private CallTarget callTarget;

  public JXPartialLambda(CallTarget callTarget) {
    this.callTarget = callTarget;
  }

  @ExportMessage
  public boolean isExecutable() {
    return true;
  }

  @ExportMessage
  public Object execute(Object[] args) {
    return callTarget.call(args);
  }
}
