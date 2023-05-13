/*
 * Copyright (c) 2012, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.truffle.jx.parser;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.JanivaLang;
import com.oracle.truffle.jx.Reserved;
import com.oracle.truffle.jx.builtins.JXNewObjectBuiltinFactory;
import com.oracle.truffle.jx.nodes.JXBinaryNode;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.JXRootNode;
import com.oracle.truffle.jx.nodes.JXStatementNode;
import com.oracle.truffle.jx.nodes.core.*;
import com.oracle.truffle.jx.nodes.expression.JXFeedValueNode;
import com.oracle.truffle.jx.nodes.expression.JXFeedValueNodeGen;
import com.oracle.truffle.jx.nodes.expression.value.JXBoolLiteralNode;
import com.oracle.truffle.jx.nodes.expression.value.JXNumberLiteralNode;
import com.oracle.truffle.jx.nodes.expression.value.JXStringLiteralNode;
import com.oracle.truffle.jx.nodes.util.JXUnboxNodeGen;
import com.oracle.truffle.jx.parser.exceptions.JXSyntaxError;
import com.oracle.truffle.jx.runtime.JXStrings;
import com.oracle.truffle.jx.statics.lambda.BuiltInLambda;
import com.oracle.truffle.jx.statics.lambda.LambdaRegistry;
import com.oracle.truffle.jx.statics.lambda.LambdaTemplate;
import com.xmbsmdsj.janiva.SourceFinder;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.graalvm.util.CollectionsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class used by the SL {@link Parser} to create nodes. The code is factored out of the
 * automatically generated parser to keep the attributed grammar of SL small.
 */
public class JXNodeFactory {
  private static final TruffleString defaultNamespace = JXStrings.fromJavaString("default");
  static {
  }

  private static final Logger logger = LoggerFactory.getLogger(JXNodeFactory.class);

  public JXExpressionNode createBoolean(Token bool_literal) {
    return new JXBoolLiteralNode(bool_literal);
  }

  /* State while parsing a source unit. */
  private final Source source;
  private final TruffleString sourceString;
  private TruffleString namespace;
  private JXExpressionNode rootNode;

  private final MetaStack metaStack = new MetaStack();
  private final JanivaLang language;

  private OutputStream rootOutPutStream = null;

  private LambdaTemplate lambdaTemplate;

  private List<JXStatementNode> imports = new ArrayList<>();

  public JXNodeFactory(JanivaLang language, Source source) {
    this.language = language;
    this.source = source;
    this.sourceString = JXStrings.fromJavaString(source.getCharacters().toString());
    this.namespace = defaultNamespace;
  }

  public void defineNamespace(Token token) {
    // We'll not re-define our namespace
    if (defaultNamespace.equals(this.namespace)) {
      this.namespace = asTruffleString(token, false);
      logger.info("Defining namespace {}", this.namespace);

    }
  }

  public void setRootStream(Token streamName) {
    if (streamName == null) return;
    switch (streamName.getText()) {
      case "stdout":
        if (this.rootOutPutStream != null) {
          throw new RuntimeException("Only one root stream can be assigned");
        }
        this.rootOutPutStream = System.out;
        break;
      default:
        throw new RuntimeException("Illegal stream name");
    }
  }

  public void registerRootNode(JXExpressionNode node) {
    this.rootNode = node;
  }

  public RootNode getRootNode() {
    return new JXRootNode(
        language, metaStack.buildRoot(), rootNode, JXStrings.fromJavaString("#root")) {

      @Override
      public Object execute(VirtualFrame frame) {
        imports.forEach(i -> i.executeVoid(frame));
        Object res = super.execute(frame);
        return res;
      }
    };
  }

  public JXExpressionNode createDecimal(Token whole, Token dec) {
    if (dec == null) {
      return new JXNumberLiteralNode(new BigDecimal(whole.getText()), false);
    }
    return new JXNumberLiteralNode(new BigDecimal(whole.getText() + "." + dec.getText()), true);
  }

  /**
   * Returns the corresponding subclass of {@link JXExpressionNode} for binary expressions. </br>
   * These nodes are currently not instrumented.
   *
   * @param opToken The operator of the binary expression
   * @param leftNode The left node of the expression
   * @param rightNode The right node of the expression
   * @return A subclass of SLExpressionNode using the given parameters based on the given opToken.
   *     null if either leftNode or rightNode is null.
   */
  public JXExpressionNode createBinary(
      Token opToken, JXExpressionNode leftNode, JXExpressionNode rightNode) {
    if (leftNode == null || rightNode == null) {
      return null;
    }
    final JXExpressionNode leftUnboxed = JXUnboxNodeGen.create(leftNode);
    final JXExpressionNode rightUnboxed = JXUnboxNodeGen.create(rightNode);

    final JXExpressionNode result;

    result = JXBinaryNode.create(opToken, leftUnboxed, rightUnboxed);
    result.addExpressionTag();

    return result;
  }

  public JXExpressionNode createStringLiteral(Token literalToken, boolean removeQuotes) {
    final JXStringLiteralNode result =
        new JXStringLiteralNode(asTruffleString(literalToken, removeQuotes));
    srcFromToken(result, literalToken);
    result.addExpressionTag();
    return result;
  }

  private TruffleString asTruffleString(Token literalToken, boolean removeQuotes) {
    int fromIndex = literalToken.getStartIndex();
    int length = literalToken.getStopIndex() - literalToken.getStartIndex() + 1;
    if (removeQuotes) {
      /* Remove the trailing and ending " */
      assert literalToken.getText().length() >= 2
          && literalToken.getText().startsWith("\"")
          && literalToken.getText().endsWith("\"");
      fromIndex += 1;
      length -= 2;
    }
    return sourceString.substringByteIndexUncached(
        fromIndex * 2, length * 2, JanivaLang.STRING_ENCODING, true);
  }

  public void startObject() {
    // logger.debug("Start object");
    metaStack.startObject();
  }

  public JXObjectAssemblyNode endObject(List<JXStatementNode> nodes) {
    // logger.debug("End object");
    JXObjectAssemblyNode res =
        new JXObjectAssemblyNode(
            nodes,
            metaStack.locals().entrySet().stream()
                .map(e -> JXSlotAccessNodeGen.create(e.getValue(), e.getKey()))
                .collect(Collectors.toList()),
            JXNewObjectBuiltinFactory.getInstance().createNode());
    metaStack.close();
    return res;
  }

  public void startArray() {
    // logger.debug("Start array");
    // Create one scope, but this time is for list
    metaStack.startArray();
  }

  public void appendArray(JXExpressionNode n) {
    // logger.debug("Append to array");
    metaStack.appendArray(n);
  }

  public JXExpressionNode closeArray() {
    // logger.debug("Close array");

    JXExpressionNode res =
        new JXArrayAssemblyNode(
            metaStack.arrayNodes(),
            JXArrayAllocationNodeFactory.getInstance().createNode(metaStack.arrayNodes().size()));
    metaStack.close();
    return res;
  }

  /**
   * Import code from a given path, and parse it to an AST
   *
   * @param importedName is a dot-separated identifier of code to import for example, you have
   *     directory tree like this | |-a.janiva |-b/ |--c.janiva |--d.janiva by using "b.c", you can
   *     refer to b/c.janiva from a.janiva.
   */
  public RootNode importFile(Token importedName) {
    TruffleString ts = asTruffleString(importedName, true);
    return JanivaLangParser.parseSL(language, SourceFinder.findImported(source.getPath(), ts));
  }

  /**
   * Bind imported ast to a global attribute
   *
   * @param valName global attribute name
   * @param imported imported ast
   * @return a statement node that will be executed at the beginning of root node
   */
  public JXStatementNode bindImport(Token valName, RootNode imported) {
    Reserved.validate(valName);
    TruffleString ts = asTruffleString(valName, false);
    int slot = metaStack.requestForGlobal(ts);
    JXStatementNode newImport = new JXImportBindingNode(slot, imported);
    this.imports.add(newImport);
    return newImport;
  }

  public JXStatementNode bindLatent(Token valName, JXExpressionNode val, boolean isFunction) {
    Reserved.validate(valName);
    TruffleString ts = asTruffleString(valName, false);
    Integer slot = this.metaStack.lookupAttribute(ts, false);
    if (slot == null) {
      slot = metaStack.requestForLatentSlot(ts, val);
    }
    return JXAttributeBindingNodeGen.create(val, slot, true);
  }

  public JXExpressionNode referAttribute(Token attributeName, @Deprecated boolean isFunc) {
    isFunc = this.metaStack.isCurrentLambdaScope();
    TruffleString ts = asTruffleString(attributeName, false);

    if (!isFunc) {
      Integer slot = this.metaStack.lookupAttribute(ts, true);
      if (slot == null) {
        throw new JXSyntaxError("Can not find attribute: " + attributeName);
      }
      return JXSlotAccessNodeGen.create(slot, ts);
    } else {
      assert this.lambdaTemplate != null;
      return new JXLambdaSlotAccessNode(ts, lambdaTemplate);
    }
  }

  /**
   * @param valName
   * @param val
   * @return
   */
  public JXStatementNode bindVal(Token valName, JXExpressionNode val) {
    TruffleString ts = asTruffleString(valName, true);
    Integer existingSlot = this.metaStack.lookupAttribute(ts, false);
    if (existingSlot != null) {
      throw new JXSyntaxError();
    }
    int frameSlot = metaStack.requestForSlot(ts, null);
    return JXAttributeBindingNodeGen.create(val, frameSlot, false);
  }

  public void defLambda(Token name) {
    Reserved.validate(name);
    TruffleString lambdaName = asTruffleString(name, false);
    this.lambdaTemplate = new LambdaTemplate(lambdaName);
    this.metaStack.startLambda();
    logger.debug("Defining {} in namespace: {}", lambdaName, namespace);
    LambdaRegistry.getInstance(namespace).register(lambdaName, lambdaTemplate);
  }

  public void addFormalParameter(Token name) {
    assert this.lambdaTemplate != null;
    TruffleString paramName = asTruffleString(name, false);
    this.lambdaTemplate.addFormalParam(paramName);
  }

  public void addBody(JXExpressionNode body) {
    this.lambdaTemplate.addBody(body);
  }

  public void finishDefLambda() {
    this.lambdaTemplate.finish(metaStack.buildTop());
    metaStack.close();
    this.lambdaTemplate = null;
  }

  public JXExpressionNode feedValue(JXExpressionNode value, List<JXExpressionNode> parameters) {
    if (parameters.size() == 0) {
      return value;
    }
    return JXFeedValueNodeGen.create(value).feed(parameters);
  }

  public JXExpressionNode materialize(Token lambdaName, List<JXExpressionNode> parameters) {
    TruffleString ts = asTruffleString(lambdaName, false);
    if (LambdaRegistry.getInstance(namespace).isBuiltIn(ts)) {
      return BuiltInLambda.valueOf(ts).create(parameters, source);
    }

    /* First we lookup local attributes
     *
     * Note that local attributes can be non-lambda values,
     * in which case, type-checking is done at run-time
     * */
    Integer slot = metaStack.lookupAttribute(ts, true);
    if (slot != null) {
      JXFeedValueNode res = JXFeedValueNodeGen.create(JXSlotAccessNodeGen.create(slot, ts));
      res.feed(parameters);
      return res;
    }

    // Then we look at already defined ones
    LambdaTemplate lt = LambdaRegistry.getInstance(namespace).lookupLambdaBody(ts);
    if (lt == null) {
      throw new JXSyntaxError("Referring to non existing lambda: " + ts);
    }
    // We use lazy lambda access, because it's body may not be finalized yet
    JXFeedValueNode res = JXFeedValueNodeGen.create(JXLambdaNodeGen.create(lt));
    res.feed(parameters);
    return res;
  }

  public JXExpressionNode createAttrAccess(JXExpressionNode val, Token attr, boolean isObject) {
    return JXAttributeAccessNodeGen.create(
        val,
        isObject
            ? new JXStringLiteralNode(asTruffleString(attr, true))
            : new JXNumberLiteralNode(BigDecimal.valueOf(Integer.parseInt(attr.getText())), false));
  }


  /** Creates source description of a single token. */
  private static void srcFromToken(JXStatementNode node, Token token) {
    node.setSourceSection(token.getStartIndex(), token.getText().length());
  }

  /** Checks whether a list contains a null. */
  private static boolean containsNull(List<?> list) {
    for (Object e : list) {
      if (e == null) {
        return true;
      }
    }
    return false;
  }
}
