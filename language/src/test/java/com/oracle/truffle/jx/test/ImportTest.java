package com.oracle.truffle.jx.test;

import com.oracle.truffle.jx.JanivaLang;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Slf4j
public class ImportTest {
  Context context;

  @Before
  public void initialize() {
    String rootPath = this.getClass().getClassLoader().getResource("io/ut-import.janiva").getFile();
    String modulePath = new File(rootPath).getParent();
    context =
        Context.newBuilder(JanivaLang.ID)
            .in(System.in)
            .out(System.out)
            .option("janiva.module-root", modulePath)
            .allowAllAccess(true)
            .build();
  }

  @After
  public void dispose() {
    context.close();
  }

  @Test
  public void testImport() {
    TestUtil.runWithStackTrace(
        () -> {
          Source s = null;
          try {
            s =
                Source.newBuilder(
                        JanivaLang.ID,
                        Objects.requireNonNull(
                            this.getClass().getClassLoader().getResource("io/ut-import.janiva")))
                    .build();
            Value v = context.eval(s);
            Assert.assertEquals(3, v.getMember("value").asInt());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }
}
