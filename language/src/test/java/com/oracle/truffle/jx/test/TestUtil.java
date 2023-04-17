package com.oracle.truffle.jx.test;

import org.graalvm.polyglot.Value;import java.io.IOException;
import java.io.InputStream;

public class TestUtil {
  public static void runWithStackTrace(Runnable r) {
    try {
      r.run();
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  public static String readResourceAsString(String resourceFile) {
    try (InputStream is = TestUtil.class.getClassLoader().getResourceAsStream(resourceFile)) {
      assert is != null;
      return new String(is.readAllBytes());
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  /**
   * Recursively evaluate a value until it is not executable any more
   * @param v
   * @return
   */
  public static Value deepEval(Value v) {
    Value res = v;
    while (res.canExecute()) {
      res = res.execute();
    }
    return res;
  }
}
