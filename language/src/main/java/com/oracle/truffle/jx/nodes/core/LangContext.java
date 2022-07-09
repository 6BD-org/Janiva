package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.jx.nodes.JXStatementNode;

import java.util.List;

/**
 * LangContext is the boundary of one execution context for example { // context1 "a": 1, "b": { //
 * context2 // parent=context1 } }
 */
public interface LangContext {
  /**
   * Get parent context
   *
   * @return
   */
  LangContext getParentContext();

  /**
   * Get a value from context The value can be ordinary value or function value is bounded using =
   * operator
   *
   * @param name name of the value
   * @return
   * @param <T>
   */
  <T> T getValue(String name);

  /**
   * Bind a value to its name
   *
   * @param name
   * @param value
   * @param <T>
   */
  <T> void bindValue(String name, T value);

  /**
   * Get attribute within the context attributes are defined as "key": value
   *
   * @param key key of the attribute
   * @return
   * @param <T>
   */
  <T> T getAttribute(String key);

  /**
   * Set attribute to context
   *
   * @param key
   * @param val
   * @param <T>
   */
  <T> void setAttribute(String key, T val);

  void setStatements(List<JXStatementNode> statements);

  LangContext spawn();
}
