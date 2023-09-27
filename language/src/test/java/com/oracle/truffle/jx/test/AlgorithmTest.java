package com.oracle.truffle.jx.test;

import com.oracle.truffle.jx.JanivaLang;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlgorithmTest {
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
  public void testFibonacci() {

    TestUtil.runWithStackTrace(
        () -> {
          Value v =
              context.eval(
                  JanivaLang.ID, TestUtil.readResourceAsString("algorithms/fibonacci.janiva"));
          Assert.assertEquals(5, v.getMember("result").asInt());
        });
  }
}
