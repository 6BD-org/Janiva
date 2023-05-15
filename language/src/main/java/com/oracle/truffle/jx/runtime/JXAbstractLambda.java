package com.oracle.truffle.jx.runtime;

import com.oracle.truffle.api.library.CachedLibrary;import com.oracle.truffle.api.object.DynamicObjectLibrary;

/**
 * This is a standard interface represen
 */
public interface JXAbstractLambda {

    /**
     * Clone is used when partially applying a delta, in order to avoid side effects on lambda's internal states
     * @return a deep copy of current lambda, including temporary states
     */
    JXPartialLambda clone(DynamicObjectLibrary dynamicObjectLibrary);

    /**
     * mergeArgs is another name of partial-application
     * arguments are fed into lambda as an array. If enough arguments are present,
     * the lambda becomes executable
     * @param args argument list
     * @return a lambda with states updated. implementer can return same lambda, or make a copy
     */
    JXPartialLambda mergeArgs(Object[] args, DynamicObjectLibrary library);

    /**
     * Execute a lambda if it is executable
     * @param args this is don't care in Janiva's lambda design. can always be an empty array
     * @return result of execution
     */
    Object execute(Object[] args, DynamicObjectLibrary library);

    /**
     * Whether a lambda is executable
     * In Janiva's design, a lambda becomes executable of enough arguments are given
     * @return true if lambda is executable
     */
    boolean isExecutable();
}
