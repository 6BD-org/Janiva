package com.oracle.truffle.jx;

import com.oracle.truffle.jx.parser.exceptions.JXSyntaxError;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.antlr.v4.runtime.Token;

public class Reserved {

  private static final Map<String, Entry> keywords = new ConcurrentHashMap<>();

  @Data
  @AllArgsConstructor
  private static class Entry {
    private final String keyword;
    private final String message;

    public int hashCode() {
      return Objects.hashCode(keyword);
    }

    public boolean equals(Object that) {
      return (that instanceof Entry) && ((Entry)that).keyword.equals(this.keyword);
    }
  }

  public static void register(String keyword, String message) {
    assert keyword != null;
    keywords.put(keyword, new Entry(keyword, message));
  }

  /**
   * Validate that a token is free to use
   * @param token
   */
  public static void validate(Token token) {
    String s = token.getText();
    if (keywords.containsKey(s)) {
      throw new JXSyntaxError("Keyword: " + s + " is reserved: " + keywords.get(s).message);
    }
  }
}
