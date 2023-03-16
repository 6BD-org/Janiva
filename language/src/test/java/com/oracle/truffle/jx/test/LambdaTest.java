package com.oracle.truffle.jx.test;

import com.oracle.truffle.jx.JSONXLang;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
public class LambdaTest {
    private static final Logger logger = LoggerFactory.getLogger(ObjectTest.class);

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
    public void testLambdaDefinition() {
        TestUtil.runWithStackTrace(() -> {
            String src = TestUtil.readResourceAsString("ut-lambda-def.jsonx");
            Value v = context.eval(JSONXLang.ID, src);

        });
    }
}
