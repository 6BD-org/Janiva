package com.oracle.truffle.jx.analyzer;

import com.xmbsmdsj.janiva.io.SourceFinder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnalyzerContext {
  private final String moduleRoot;
  private final String grammar;
  private final List<RuntimeException> errors;

  public AnalyzerContext(String moduleRoot, String grammar) {
    this.moduleRoot = moduleRoot;
    this.errors = new ArrayList<>();
    this.grammar = grammar;
  }

  public void reportError(RuntimeException err) {
    this.errors.add(err);
  }

  /**
   * Read a plain file
   *
   * @param codePath relative path in module
   * @return
   */
  public String readJanivaFile(String codePath) throws IOException, FileNotFoundException {
    return SourceFinder.getSourceString(this.moduleRoot, SourceFinder.translate(codePath));
  }

  public List<RuntimeException> getErrors() {
    return errors;
  }
}
