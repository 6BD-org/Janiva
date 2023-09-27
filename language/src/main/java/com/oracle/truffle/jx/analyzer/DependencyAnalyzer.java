package com.oracle.truffle.jx.analyzer;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DependencyAnalyzer implements Analyzer {
  private final String entranceFile;
  private static final Pattern importPattern =
      Pattern.compile(
          "(([_a-zA-Z]+[_a-zA-Z0-9]*)\\s*<<)?\\s*@import\\s*<<\\s\"(?<importPath>.*)\"\\s*#\\s*$");
  private final Map<String, List<String>> cache = new HashMap<>();
  private final List<Detector> detectors = new ArrayList<>();

  public DependencyAnalyzer(String entranceFile) {
    this.entranceFile = entranceFile;
  }

  @Override
  public void analyse(AnalyzerContext ctx) {
    try {
      dfs(ctx);
    } catch (RuntimeException e) {
      ctx.reportError(e);
    }
  }

  private void dfs(AnalyzerContext ctx) {
    Stack<String> stack = new Stack<>();
    stack.push(entranceFile);
    detectors.forEach(d -> d.enter(ctx, entranceFile));
    while (!stack.isEmpty()) {
      String next = stack.pop();
      detectors.forEach(d -> d.exit(ctx, next));
      try {
        detectors.forEach(d -> d.visit(ctx, next));

        resolveDependency(ctx, next)
            .forEach(
                dep -> {
                  stack.push(dep);
                  detectors.forEach(d -> d.enter(ctx, dep));
                });
      } catch (IOException e) {
        ctx.reportError(new RuntimeException(e));
      }
    }
  }

  /**
   * @param codePath relative import path of the code. for example, lib1.lib, lib2.lib
   * @return
   */
  protected List<String> resolveDependency(AnalyzerContext ctx, String codePath)
      throws IOException {
    if (cache.containsKey(codePath)) {
      return cache.get(codePath);
    }
    String code = ctx.readJanivaFile(codePath);
    List<String> res = new ArrayList<>();
    for (String codeLine : code.split(System.lineSeparator())) {
      String importPath = extractImport(codeLine);
      if (importPath != null) {
        res.add(importPath);
      }
    }
    cache.put(codePath, res);
    return res;
  }

  private String extractImport(String line) {
    Matcher m = importPattern.matcher(line);
    if (!m.find()) {
      return null;
    }
    return m.group("importPath");
  }

  public void addDetector(Detector detector) {
    this.detectors.add(detector);
  }

  public interface Detector {
    /**
     * Invoked when dependency is pushed to stack
     *
     * @param context
     * @param codePath
     */
    void enter(AnalyzerContext context, String codePath);

    /**
     * invoked when dependency is poped from stack
     *
     * @param context
     * @param codePath
     */
    void exit(AnalyzerContext context, String codePath);

    /**
     * invoked when dependency is visited
     *
     * @param context
     * @param codePath
     */
    void visit(AnalyzerContext context, String codePath);
  }
}
