package com.oracle.truffle.jx.runtime.io;

import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.jx.JanivaLang;
import com.oracle.truffle.jx.parser.JanivaLangParser;import jdk.internal.jimage.decompressor.SignatureParser;import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.io.File;
import java.io.IOException;

public class SourceEvaluator {
    private static final Context c = Context.getCurrent();


    public static RootNode parse(JanivaLang lang, Source s) {
        return JanivaLangParser.parseSL(lang, s);
    }

}
