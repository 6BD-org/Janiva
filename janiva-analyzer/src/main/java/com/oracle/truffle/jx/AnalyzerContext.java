package com.oracle.truffle.jx;

import com.xmbsmdsj.janiva.SourceFinder;import org.antlr.runtime.RecognitionException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.tool.Grammar;
import java.util.ArrayList;
import java.util.List;

public class AnalyzerContext {
  private final String moduleRoot;
  private final String grammar;
  private final List<AnalysisError> errors;

  public AnalyzerContext(String moduleRoot, String grammar) {
    this.moduleRoot = moduleRoot;
    this.errors = new ArrayList<>();
    this.grammar = grammar;
  }

  public void reportError(AnalysisError err) {
    this.errors.add(err);
  }

  /**
   * Read a plain file
   *
   * @param codePath relative path in module
   * @return
   */
  public String readJanivaFile(String codePath) {
    return SourceFinder.getSourceString(this.moduleRoot, codePath);
  }

  public Parser newParser(String codePath) throws RecognitionException {
    Grammar g = new Grammar(this.grammar);
    Lexer lexer = g.createLexerInterpreter(CharStreams.fromString(readJanivaFile(codePath)));
    CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
    return g.createParserInterpreter(commonTokenStream);
  }
}
