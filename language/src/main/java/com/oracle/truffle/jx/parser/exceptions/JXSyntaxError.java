package com.oracle.truffle.jx.parser.exceptions;

import com.oracle.truffle.api.exception.AbstractTruffleException;

public class JXSyntaxError extends AbstractTruffleException {

  public enum Type {
    USE_RESERVED_KEYWORD,
    OTHER
  }

  private final Type type;

  public JXSyntaxError() {
    super();
    type = Type.OTHER;
  }

  public JXSyntaxError(String message) {
    super(message);
    this.type = Type.OTHER;
  }

  public JXSyntaxError(Type t, String message) {
    super(message);
    assert t != null;
    this.type = t;
  }

  public Type getType() {
    return this.type;
  }


}
