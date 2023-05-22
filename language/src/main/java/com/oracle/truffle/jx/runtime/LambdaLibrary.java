package com.oracle.truffle.jx.runtime;

import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.Library;import com.oracle.truffle.api.library.LibraryFactory;

@GenerateLibrary
public abstract class LambdaLibrary extends Library {

    private static final LibraryFactory<LambdaLibrary> FACTORY = LibraryFactory.resolve(LambdaLibrary.class);

    public abstract Object cloneLambda(Object receiver);

    public abstract Object mergeArgs(Object receiver, Object[] args);

    public static LambdaLibrary getUncached() {
        return FACTORY.getUncached();
    }

}
