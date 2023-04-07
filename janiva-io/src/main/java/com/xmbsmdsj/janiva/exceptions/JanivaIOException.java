package com.xmbsmdsj.janiva.exceptions;

import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.nodes.Node;

public class JanivaIOException extends RuntimeException {
  public JanivaIOException(String message) {
        super(message);
    }

    public JanivaIOException(String message, Throwable t) {
      super(message, t);
    }
}
