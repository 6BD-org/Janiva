package com.oracle.truffle.jx.analyzer;

import com.oracle.truffle.jx.JanivaLang;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;import java.util.List;

@RunWith(JUnit4.class)
public class CircAnalyzerTest {



  private AnalyzerRunner getRunner(String proj) {
    String rootPath =
            this.getClass().getClassLoader().getResource(String.format("analyzer/%s/main.janiva", proj)).getFile();
    String modulePath = new File(rootPath).getParent();
    String grammar = "";


    return new AnalyzerRunner(modulePath, grammar);
  }

  @Test
  @SneakyThrows
  public void testCircAnalyzer() {



    AnalyzerRunner runner = getRunner("circular");
    DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer("main");
    dependencyAnalyzer.addDetector(new CircDetector());
    runner.addAnalyzer(dependencyAnalyzer);
    runner.run();
    Assert.assertEquals(1, runner.getErrors().size());


    runner = getRunner("non-circular");
    dependencyAnalyzer = new DependencyAnalyzer("main");
    dependencyAnalyzer.addDetector(new CircDetector());
    runner.addAnalyzer(dependencyAnalyzer);
    runner.run();
    Assert.assertEquals(0, runner.getErrors().size());



  }
}
