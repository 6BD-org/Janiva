package com.xmbsmdsj.janiva.exceptions;

import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.nodes.Node;

public class JanivaIOException extends AbstractTruffleException {
  public JanivaIOException(String message, Node n) {
        super(message, n);
    }
}
