package com.xmbsmdsj.janiva;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;
import com.xmbsmdsj.janiva.constants.LanguageConstants;
import com.xmbsmdsj.janiva.exceptions.JanivaIOException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;

@Slf4j
public class SourceFinder {
  private static final Pattern DOT_SPLITTER = Pattern.compile("\\.");
  private static final String SYS_ROOT =
      System.getProperty("os.name").toLowerCase().equals("win") ? "C:\\" : "/";
  /**
   * Find a source file imported by src.
   *
   * @param src the the import is performed
   * @param importPath path of imported source. In Janiva, importing is performed by {@code
   *     imported_val << @import << "path.sub.sub2.obj" // this corresponds to
   *     path/sub/sub2/obj.janiva // the relative path should be evaluated in under src's path }
   * @return
   */
  public static Source findImported(String basePath, TruffleString importPath) {
    if (basePath == null || basePath.length() == 0) {
      throw new IllegalArgumentException("import is not permitted when base path is absent");
    }
    String s = importPath.toJavaStringUncached();
    return findImported(basePath, s);
  }

  public static Source findImported(String basePath, String importPath) {
    if (basePath == null || basePath.length() == 0) {
      throw new IllegalArgumentException("import is not permitted when base path is absent");
    }
    String targetSourcePath;
    log.debug("basePath={}", basePath);
    if (basePath.endsWith(File.separator)) {
      targetSourcePath = basePath + translate(importPath);
    } else {
      targetSourcePath = basePath + File.separator + translate(importPath);
    }
    return getSource(targetSourcePath);
  }

  public static Source getSource(String absPath) {
    try {
      return Source.newBuilder(LanguageConstants.LANG, new URL("file:///" + absPath))
              .build();
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IOException ioe) {
      throw new JanivaIOException("Cannot read file: " + absPath, ioe);
    }
  }

  public static String getSourceString(String base, String path) {
    String absPath = base + File.separator + path;
    Source s = getSource(absPath);
    CharSequence charSeq = s.getCharacters();
    return charSeq.toString();
  }

  /**
   * translate to slash path
   *
   * @param dotPath
   * @return
   */
  private static String translate(String dotPath) {
    String[] splitted = DOT_SPLITTER.split(dotPath);
    if (splitted.length == 0) {
      throw new JanivaIOException("Empty imported path");
    }
    String fileName = splitted[splitted.length - 1] + LanguageConstants.FILE_EXT;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < splitted.length - 1; i++) {
      sb.append(splitted[i]).append(File.separator);
    }
    return sb.append(fileName).toString();
  }
}
