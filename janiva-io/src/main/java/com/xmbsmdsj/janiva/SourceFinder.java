package com.xmbsmdsj.janiva;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;
import com.xmbsmdsj.janiva.constants.LanguageConstants;
import com.xmbsmdsj.janiva.exceptions.JanivaIOException;
import org.graalvm.polyglot.Source;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class SourceFinder {
  private static final Pattern DOT_SPLITTER = Pattern.compile("\\.");

  /**
   * Find a source file imported by src.
   *
   * @param node is where the logic executes. Used for exception
   * @param src the the import is performed
   * @param importPath path of imported source. In Janiva, importing is performed by {@code
   *     imported_val << @import << "path.sub.sub2.obj" // this corresponds to
   *     path/sub/sub2/obj.janiva // the relative path should be evaluated in under src's path }
   * @return
   */
  public static Source findImported(Node node, Source src, TruffleString importPath) {
    String s = importPath.toJavaStringUncached();
    String basePath = new File(src.getPath()).getParent();
    if (basePath == null) basePath = "/";
    String targetSourcePath;
    if (basePath.endsWith("/")) {
      targetSourcePath = basePath + translate(node, s);
    } else {
      targetSourcePath = basePath + "/" + translate(node, s);
    }
    try {
      return Source.newBuilder(LanguageConstants.LANG, new File(targetSourcePath)).build();
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IOException ioe) {
      throw new JanivaIOException("Cannot read file", node);
    }
  }

  /**
   * translate to slash path
   *
   * @param dotPath
   * @return
   */
  private static String translate(Node node, String dotPath) {
    String[] splitted = DOT_SPLITTER.split(dotPath);
    if (splitted.length == 0) {
      throw new JanivaIOException("Empty imported path", node);
    }
    String fileName = splitted[splitted.length - 1] + LanguageConstants.FILE_EXT;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < splitted.length - 1; i++) {
      sb.append(splitted[i]).append("/");
    }
    return sb.append(fileName).toString();
  }
}
