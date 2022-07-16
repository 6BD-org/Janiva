package com.oracle.truffle.jx.test;

import com.oracle.truffle.jx.JSONXLang;
import com.oracle.truffle.jx.runtime.JXArray;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    TestUtil.runWithStackTrace(
        () -> {
          Value v = context.eval(JSONXLang.ID, "{\"a\":1}");
          System.out.println("Object v has members: " + v.getMemberKeys());
          Assert.assertTrue(v.hasMember("a"));
          Assert.assertEquals(1, v.getMember("a").asInt());
        });
  }

  @Test
  public void testNestedObject() {
    TestUtil.runWithStackTrace(
        () -> {
          String src = TestUtil.readResourceAsString("ut-nested-object.jsonx");
          Value v = context.eval(JSONXLang.ID, src);
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
    TestUtil.runWithStackTrace(() -> {
        String src = "[1,2,3]";
        Value v = context.eval(JSONXLang.ID, src);
        //JXArray  = v.as(JXArray.class);
        int[] arr = v.as(int[].class);

        Assert.assertEquals(1, arr[0]);
    });
  }

  @Test
  public void testNestedList() {
      TestUtil.runWithStackTrace(() -> {
          String src = TestUtil.readResourceAsString("ut-nested-list.jsonx");
          Value v = context.eval(JSONXLang.ID, src);
      });
  }
}
