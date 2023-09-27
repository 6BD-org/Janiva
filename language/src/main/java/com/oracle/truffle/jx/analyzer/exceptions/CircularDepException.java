package com.oracle.truffle.jx.analyzer.exceptions;

public class CircularDepException extends RuntimeException {
  public CircularDepException(String message) {
    super(message);
  }
}
