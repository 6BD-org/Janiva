package com.oracle.truffle.jx.analyzer;

import com.xmbsmdsj.janiva.SourceFinder;import org.antlr.runtime.RecognitionException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.tool.Grammar;
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

  public ParserInterpreter newParser(String codePath) throws RecognitionException, IOException {
    Grammar g = new Grammar(this.grammar);
    Lexer lexer = g.createLexerInterpreter(CharStreams.fromString(readJanivaFile(codePath)));
    CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
    return g.createParserInterpreter(commonTokenStream);
  }

  public List<RuntimeException> getErrors() {
    return errors;
  }
}
