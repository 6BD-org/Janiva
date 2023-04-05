package com.xmbsmdsj.janiva;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;
import com.xmbsmdsj.janiva.constants.LanguageConstants;
import org.graalvm.polyglot.Source;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;

@RunWith(JUnit4.class)
public class SourceFinderTest {
  static class MockNode extends Node {}
  @Test
  public void findImported() throws IOException {
    try {
      Source s =
              Source.newBuilder(LanguageConstants.LANG, this.getClass().getClassLoader().getResource("a.janiva")).build();
      Source newS =
              SourceFinder.findImported(
                      new MockNode(),
                      s.getPath(),
                      TruffleString.fromJavaStringUncached("a.b.c", TruffleString.Encoding.UTF_8));
      System.out.println(newS.getPath());
      Assert.assertTrue(newS.getPath().endsWith("a" + File.separator + "b"  + File.separator + "c.janiva"));
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
