package com.oracle.truffle.jx.test;

import com.oracle.truffle.jx.JanivaLang;
import com.oracle.truffle.jx.parser.exceptions.JXSyntaxError;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;import org.junit.After;
import org.junit.Assert;import org.junit.Before;
import org.junit.Test;import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
@Slf4j
public class ReservedKeywordTest {

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
  public void test() {
    for (int i=0; i<1; i++) {
      String fileName = "exceptional/ut-use-reserved-kw-" + (i+1) + ".janiva";
      Throwable thrown = null;
      try {
        context.eval(JanivaLang.ID, TestUtil.readResourceAsString(fileName));
      } catch (Throwable t) {
        if (t instanceof PolyglotException) {
          thrown = t;
        }
      }
      Assert.assertNotNull(thrown);
      log.info("Throwing {}", thrown.getMessage());
    }
  }
}
