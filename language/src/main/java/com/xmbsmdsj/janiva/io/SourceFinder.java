package com.xmbsmdsj.janiva.io;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;
import com.xmbsmdsj.janiva.io.constants.LanguageConstants;
import com.xmbsmdsj.janiva.io.exceptions.JanivaIOException;

import java.io.*;
import java.net.URL;
import java.util.regex.Pattern;

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
    if (basePath == null || basePath.isEmpty()) {
      throw new IllegalArgumentException("import is not permitted when base path is absent");
    }
    String s = importPath.toJavaStringUncached();
    return findImported(basePath, s);
  }

  public static Source findImported(String basePath, String importPath) {
    if (basePath == null || basePath.isEmpty()) {
      throw new IllegalArgumentException("import is not permitted when base path is absent");
    }
    String targetSourcePath;
    if (basePath.endsWith(File.separator)) {
      targetSourcePath = basePath + translate(importPath);
    } else {
      targetSourcePath = basePath + File.separator + translate(importPath);
    }
    return getSource(targetSourcePath);
  }

  public static Source getSource(String absPath) {
    try {
      return Source.newBuilder(LanguageConstants.LANG, new URL("file:///" + absPath)).build();
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IOException ioe) {
      throw new JanivaIOException("Cannot read file: " + absPath, ioe);
    }
  }

  public static String getSourceString(String base, String path) throws FileNotFoundException, IOException{
    String absPath = base + File.separator + path;
    File f = new File(absPath);
    try (InputStream is = new FileInputStream(f)) {
      return new String(is.readAllBytes());
    }
  }

  /**
   * translate import path to (relative) slash path
   * for example
   * com.xmbsmdsj.lib1.code -> com/xmbsmdsj/lib1/code.janiva
   *
   * @param dotPath
   * @return
   */
  public static String translate(String dotPath) {
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
