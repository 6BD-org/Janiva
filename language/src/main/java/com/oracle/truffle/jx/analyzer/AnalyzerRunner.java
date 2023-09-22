package com.oracle.truffle.jx.analyzer;

import java.util.ArrayList;
import java.util.List;

public class AnalyzerRunner {
  private final String modulePath;
  private final String grammar;
  private final List<Analyzer> analyzers = new ArrayList<>();

  private AnalyzerContext ctx;

  public AnalyzerRunner(String modulePath, String grammar) {
    this.modulePath = modulePath;
    this.grammar = grammar;
    this.ctx = new AnalyzerContext(modulePath, grammar);
  }

  public void addAnalyzer(Analyzer analyzer) {
    analyzers.add(analyzer);
  }

  public void run() {
    for (Analyzer analyzer : analyzers) {
      analyzer.analyse(ctx);
    }
  }

  public List<RuntimeException> getErrors() {
    return this.ctx.getErrors();
  }

  protected AnalyzerContext getCtx() {
    return ctx;
  }
}
