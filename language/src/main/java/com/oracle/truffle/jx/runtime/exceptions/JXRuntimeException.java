package com.oracle.truffle.jx.runtime.exceptions;

import com.oracle.truffle.jx.nodes.JXExpressionNode;

public class JXRuntimeException extends RuntimeException {
  public JXRuntimeException(String message) {
    super(message);
  }
}
