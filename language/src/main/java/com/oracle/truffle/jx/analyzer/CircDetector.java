package com.oracle.truffle.jx.analyzer;

import com.oracle.truffle.jx.analyzer.exceptions.CircularDepException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CircDetector implements DependencyAnalyzer.Detector {
  private final List<String> history = new ArrayList<>();

  @Override
  public void enter(AnalyzerContext context, String codePath) {
    log.info("enter {}", codePath);
    int existing = find(codePath);
    if (existing >= 0) {
      StringBuilder sb = new StringBuilder();
      for (String s : history) {
        sb.append(s);
        sb.append("->");
      }
      sb.append(history.get(existing));
      throw new CircularDepException(sb.toString());
    }
  }

  private int find(String codePath) {
    for (int i = 0; i < history.size(); i++) {
      if (history.get(i).equals(codePath)) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public void exit(AnalyzerContext context, String codePath) {
    log.info("exit {}", codePath);
    // history.remove(codePath);
  }

  @Override
  public void visit(AnalyzerContext context, String codePath) {
    history.add(codePath);
  }
}
