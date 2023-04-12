package com.oracle.truffle.jx.test;

import com.oracle.truffle.jx.JanivaLang;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.After;
import org.junit.Assert;
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
    TestUtil.runWithStackTrace(
        () -> {
          String src = TestUtil.readResourceAsString("ut-lambda-def.janiva");
          Value v = context.eval(JanivaLang.ID, src);
        });
  }

  @Test
  public void testBuiltInIf() {
    TestUtil.runWithStackTrace(
        () -> {
          String src = TestUtil.readResourceAsString("ut-built-in-if.janiva");
          Value v = context.eval(JanivaLang.ID, src);
          Integer a = v.getMember("a").as(Integer.class);
          Assert.assertEquals(Integer.valueOf(1), a);
          Integer b = v.getMember("b").as(Integer.class);
          Assert.assertEquals(Integer.valueOf(2), b);
        });
  }

  @Test
  public void testBuildInRange() {
    TestUtil.runWithStackTrace(
        () -> {
          String src = TestUtil.readResourceAsString("ut-built-in-range.janiva");
          Value v = context.eval(JanivaLang.ID, src);
          int a0 = v.getMember("a").getArrayElement(0).asInt();
          Assert.assertEquals(0, a0);

          int a1 = v.getMember("a").getArrayElement(1).asInt();
          Assert.assertEquals(1, a1);
        });
  }

  @Test
  public void testPartialApplication() {
    TestUtil.runWithStackTrace(
        () -> {
          String src = TestUtil.readResourceAsString("ut-partial-application.janiva");
          Value v = context.eval(JanivaLang.ID, src);
          Assert.assertEquals(5, v.getMember("sum1").execute().asInt());
          Assert.assertEquals(8, v.getMember("sum2").execute().asInt());
        });
  }
}
