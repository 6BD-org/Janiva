package com.oracle.truffle.jx.test;

import com.oracle.truffle.jx.JSONXLang;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class ObjectTest {

    Context context;

    @Before
    public void initialize() {
        context = Context.create();
    }

    @After
    public void dispose() {
        context.close();
    }

    @Test
    public void testObject() {
        TestUtil.runWithStackTrace(() -> {
            Value v = context.eval(JSONXLang.ID, "{\"a\":1}");
            System.out.println("Object v has members: " + v.getMemberKeys());
            System.out.println(v.getMember("a"));
            System.out.println(v.hasMember("a"));
        });
    }
}
