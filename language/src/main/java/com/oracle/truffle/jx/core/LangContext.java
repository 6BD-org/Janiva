package com.oracle.truffle.jx.core;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.nodes.JXExpressionNode;

/**
 * LangContext is the boundary of one execution context
 * for example
 * {
 *      // context1
 *     "a": 1,
 *     "b": {
 *         // context2
 *         // parent=context1
 *     }
 * }
 */
public interface LangContext {
    /**
     * Get parent context
     * @return
     */
    LangContext getParent();

    /**
     * Get a value from context
     * The value can be ordinary value or function
     * value is bounded using = operator
     * @param name name of the value
     * @return
     * @param <T>
     */
    <T> T getValue(TruffleString name);

    /**
     * Bind a value to its name
     * @param name
     * @param value
     * @param <T>
     */
    <T> void bindValue(TruffleString name, T value);

    /**
     * Get attribute within the context
     * attributes are defined as
     * "key": value
     * @param key key of the attribute
     * @return
     * @param <T>
     */
    <T> T getAttribute(TruffleString key);

    /**
     * Set attribute to context
     * @param key
     * @param val
     * @param <T>
     */
    <T> void setAttribute(TruffleString key, T val);

    LangContext spawn();

    DynamicObject materialize();

}
