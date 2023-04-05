package com.oracle.truffle.jx.runtime.io;

import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.jx.JanivaLang;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.File;
import java.io.IOException;

public class SourceEvaluator {
    private static final Context c = Context.newBuilder(JanivaLang.ID).out(System.out).build();

    /**
     * Evaluate source to target type
     * @param s
     * @param target target class.
     * @return casted evaluation result
     * @param <T>
     */
    public static <T extends TruffleObject> T eval(Source s, Class<T> target) throws IOException {
        Value v = c.eval(s);
        return v.as(target);
    }
}
