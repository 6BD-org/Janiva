package com.xmbsmdsj.janiva.io.exceptions;

public class JanivaIOException extends RuntimeException {
  public JanivaIOException(String message) {
    super(message);
  }

  public JanivaIOException(String message, Throwable t) {
    super(message, t);
  }
}
