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
public class ObjectTest {

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
  public void testObject() {
    TestUtil.runWithStackTrace(
        () -> {
          Value v = context.eval(JanivaLang.ID, "{\"a\":1}");
          System.out.println("Object v has members: " + v.getMemberKeys());
          Assert.assertTrue(v.hasMember("a"));
          Assert.assertEquals(1, v.getMember("a").asInt());
        });
  }

  @Test
  public void testNestedObject() {
    TestUtil.runWithStackTrace(
        () -> {
          String src = TestUtil.readResourceAsString("ut-nested-object.janiva");
          Value v = context.eval(JanivaLang.ID, src);
          Assert.assertTrue(v.hasMember("a"));
          Assert.assertTrue(v.hasMember("b"));
          Assert.assertEquals(1, v.getMember("a").asInt());
          Value b = v.getMember("b");
          Assert.assertTrue(b.hasMember("c"));
          Assert.assertTrue(b.hasMember("d"));
          Assert.assertEquals("val_c", b.getMember("c").asString());
          Assert.assertFalse(b.getMember("d").asBoolean());
        });
  }

  @Test
  public void testList() {
    TestUtil.runWithStackTrace(
        () -> {
          String src = "[1,2,3]";
          Value v = context.eval(JanivaLang.ID, src);
          // JXArray  = v.as(JXArray.class);
          int[] arr = v.as(int[].class);

          Assert.assertEquals(1, arr[0]);
          Assert.assertEquals(3, arr.length);
        });
  }

  @Test
  public void testArithmeticsAsValue() {
    TestUtil.runWithStackTrace(
        () -> {
          String src = TestUtil.readResourceAsString("ut-arithmetics-as-value.janiva");
          Value v = context.eval(JanivaLang.ID, src);
          Assert.assertEquals(3, v.getMember("a").asInt());
          Assert.assertEquals(6, v.getMember("b").asInt());
        });
  }

  @Test
  public void testNestedList() {
    logger.debug("Start nested list test");

    TestUtil.runWithStackTrace(
        () -> {
          String src = TestUtil.readResourceAsString("ut-nested-list.janiva");
          Value v = context.eval(JanivaLang.ID, src);
          Value item1 = v.getArrayElement(0);
          Value item2 = v.getArrayElement(1);
          Assert.assertEquals(3, item2.asInt());
          int[] arr123 = item1.getMember("a").as(int[].class);
          Assert.assertArrayEquals(new int[] {1, 2, 3}, arr123);
        });
  }

  @Test
  public void testLatentBinding() {
    logger.debug("Start latent binding test");
    TestUtil.runWithStackTrace(
        () -> {
          String src = TestUtil.readResourceAsString("ut-latent-bind-1.janiva");
          Value v = context.eval(JanivaLang.ID, src);
          Integer c = v.getMember("c").as(Integer.class);
          Assert.assertEquals(Integer.valueOf(2), c);

          Value d = v.getMember("d");
          Value e = v.getMember("e");

          Assert.assertEquals(2, d.getMember("c").asInt());
          Assert.assertEquals(3, e.getMember("c").asInt());
          Assert.assertEquals(2, e.getMember("d").asInt());
        });
  }

  @Test
  public void testAttrAccess() {
    logger.debug("start attribute accessing test");
    TestUtil.runWithStackTrace(() -> {
        String src = TestUtil.readResourceAsString("object/ut-attribute-accessing.janiva");
        Value v = context.eval(JanivaLang.ID, src);
        Assert.assertEquals("foo", v.getMember("result_1").asString());
        Assert.assertEquals("bar", v.getMember("result_2").asString());
    });
  }
}
