package com.oracle.truffle.jx.test;

import com.oracle.truffle.jx.JSONXLang;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class IOTest {

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
    public void testStdin() {
        TestUtil.runWithStackTrace(() -> {
            String src = TestUtil.readResourceAsString("ut-object-to-std.jsonx");
            Value v = context.eval(JSONXLang.ID, src);
        });
    }

}
