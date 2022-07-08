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

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.jx.JSONXLang;
import com.oracle.truffle.jx.builtins.JXNewObjectBuiltinFactory;
import com.oracle.truffle.jx.nodes.core.*;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.JXRootNode;
import com.oracle.truffle.jx.nodes.JXStatementNode;
import com.oracle.truffle.jx.nodes.expression.*;
import com.oracle.truffle.jx.nodes.expression.value.JXBoolLiteralNode;
import com.oracle.truffle.jx.nodes.expression.value.JXNumberLiteralNode;
import com.oracle.truffle.jx.nodes.expression.value.JXObjectNode;
import com.oracle.truffle.jx.nodes.local.*;
import com.oracle.truffle.jx.nodes.util.JXUnboxNodeGen;
import com.oracle.truffle.jx.runtime.JSNull;
import com.oracle.truffle.jx.runtime.JXObject;
import com.oracle.truffle.jx.runtime.JXStrings;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.nodes.expression.value.JXStringLiteralNode;

/**
 * Helper class used by the SL {@link Parser} to create nodes. The code is factored out of the
 * automatically generated parser to keep the attributed grammar of SL small.
 */
public class JXNodeFactory {

  public JXExpressionNode createBoolean(Token bool_literal) {
    return new JXBoolLiteralNode(bool_literal);
  }


  /**
   * Local variable names that are visible in the current block. Variables are not visible outside
   * of their defining block, to prevent the usage of undefined variables. Because of that, we can
   * decide during parsing if a name references a local variable or is a function name.
   */
  static class LexicalScope {
    protected final LexicalScope outer;
    protected final Map<TruffleString, Integer> locals;

    LexicalScope(LexicalScope outer) {
      this.outer = outer;
      this.locals = new HashMap<>();
    }

    public Integer find(TruffleString name) {
      Integer result = locals.get(name);
      if (result != null) {
        return result;
      } else if (outer != null) {
        return outer.find(name);
      } else {
        return null;
      }
    }
  }

  /* State while parsing a source unit. */
  private final Source source;
  private final TruffleString sourceString;

  /* State while parsing a function. */
  private int functionStartPos;
  private TruffleString functionName;
  private int functionBodyStartPos; // includes parameter list
  private int parameterCount;
  private final FrameDescriptor.Builder frameDescriptorBuilder = FrameDescriptor.newBuilder();;
  private List<JXStatementNode> methodNodes;


  private JXExpressionNode rootNode;



  /* State while parsing a block. */
  private LexicalScope lexicalScope;
  private final JSONXLang language;

  public JXNodeFactory(JSONXLang language, Source source) {
    this.language = language;
    this.source = source;
    this.sourceString = JXStrings.fromJavaString(source.getCharacters().toString());
  }


  public void addFormalParameter(Token nameToken) {
    /*
     * Method parameters are assigned to local variables at the beginning of the method. This
     * ensures that accesses to parameters are specialized the same way as local variables are
     * specialized.
     */
    final JXReadArgumentNode readArg = new JXReadArgumentNode(parameterCount);
    readArg.setSourceSection(nameToken.getStartIndex(), nameToken.getText().length());
    JXExpressionNode assignment =
        createAssignment(createStringLiteral(nameToken, false), readArg, parameterCount);
    methodNodes.add(assignment);
    parameterCount++;
  }


  public void registerRootNode(JXExpressionNode node) {
    this.rootNode = node;
  }

  public RootNode getRootNode() {
    return new JXRootNode(
            language, frameDescriptorBuilder.build(), rootNode, JXStrings.fromJavaString("#root")
    );
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
    switch (opToken.getText()) {
      case "+":
        result = JXAddNodeGen.create(leftUnboxed, rightUnboxed);
        break;
      case "*":
        result = JXMulNodeGen.create(leftUnboxed, rightUnboxed);
        break;
      case "/":
        result = JXDivNodeGen.create(leftUnboxed, rightUnboxed);
        break;
      case "-":
        result = JXSubNodeGen.create(leftUnboxed, rightUnboxed);
        break;
      case "<":
        result = JXLessThanNodeGen.create(leftUnboxed, rightUnboxed);
        break;
      case "<=":
        result = JXLessOrEqualNodeGen.create(leftUnboxed, rightUnboxed);
        break;
      case ">":
        result = JXLogicalNotNodeGen.create(JXLessOrEqualNodeGen.create(leftUnboxed, rightUnboxed));
        break;
      case ">=":
        result = JXLogicalNotNodeGen.create(JXLessThanNodeGen.create(leftUnboxed, rightUnboxed));
        break;
      case "==":
        result = JXEqualNodeGen.create(leftUnboxed, rightUnboxed);
        break;
      case "!=":
        result = JXLogicalNotNodeGen.create(JXEqualNodeGen.create(leftUnboxed, rightUnboxed));
        break;
      case "&&":
        result = new JXLogicalAndNode(leftUnboxed, rightUnboxed);
        break;
      case "||":
        result = new JXLogicalOrNode(leftUnboxed, rightUnboxed);
        break;
      default:
        throw new RuntimeException("unexpected operation: " + opToken.getText());
    }

    int start = leftNode.getSourceCharIndex();
    int length = rightNode.getSourceEndIndex() - start;
    result.setSourceSection(start, length);
    result.addExpressionTag();

    return result;
  }

  /**
   * Returns an {@link JXWriteLocalVariableNode} for the given parameters.
   *
   * @param nameNode The name of the variable being assigned
   * @param valueNode The value to be assigned
   * @return An SLExpressionNode for the given parameters. null if nameNode or valueNode is null.
   */
  public JXExpressionNode createAssignment(JXExpressionNode nameNode, JXExpressionNode valueNode) {
    return createAssignment(nameNode, valueNode, null);
  }

  /**
   * Returns an {@link JXWriteLocalVariableNode} for the given parameters.
   *
   * @param nameNode The name of the variable being assigned
   * @param valueNode The value to be assigned
   * @param argumentIndex null or index of the argument the assignment is assigning
   * @return An SLExpressionNode for the given parameters. null if nameNode or valueNode is null.
   */
  public JXExpressionNode createAssignment(
          JXExpressionNode nameNode, JXExpressionNode valueNode, Integer argumentIndex) {
    if (nameNode == null || valueNode == null) {
      return null;
    }

    TruffleString name = ((JXStringLiteralNode) nameNode).executeGeneric(null);

    Integer frameSlot = lexicalScope.find(name);
    boolean newVariable = false;
    if (frameSlot == null) {
      frameSlot = frameDescriptorBuilder.addSlot(FrameSlotKind.Illegal, name, argumentIndex);
      lexicalScope.locals.put(name, frameSlot);
      newVariable = true;
    }
    final JXExpressionNode result =
        JXWriteLocalVariableNodeGen.create(valueNode, frameSlot, nameNode, newVariable);

    if (valueNode.hasSource()) {
      final int start = nameNode.getSourceCharIndex();
      final int length = valueNode.getSourceEndIndex() - start;
      result.setSourceSection(start, length);
    }
    if (argumentIndex == null) {
      result.addExpressionTag();
    }

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
        fromIndex * 2, length * 2, JSONXLang.STRING_ENCODING, true);
  }

  public JXExpressionNode startObject() {
    lexicalScope = new LexicalScope(null);
    return JXNewObjectBuiltinFactory.getInstance().createNode();
  }

  public JXObjectAssemblyNode endObject(List<JXStatementNode> nodes) {
    JXObjectAssemblyNode res = new JXObjectAssemblyNode(
            nodes,
            lexicalScope.locals.entrySet().stream().map(e -> new JXValueAccessNode(e.getValue(), e.getKey())).collect(Collectors.toList()),
            JXNewObjectBuiltinFactory.getInstance().createNode()
    );
    lexicalScope = lexicalScope.outer;
    return res;
  }

  /**
   *
   * @param valName
   * @param val
   * @return
   */
  public JXStatementNode bindVal(Token valName, JXExpressionNode val) {
    System.out.println(valName);
    int frameSlot = frameDescriptorBuilder.addSlot(inferSlotKind(val), valName.getText(), null);
    // Map value name to slot
    lexicalScope.locals.put(asTruffleString(valName, true), frameSlot);
    return new JXAttributeBindingNode(frameSlot, val);
  }

  public FrameSlotKind inferSlotKind(JXExpressionNode val) {
    if (val instanceof JXStringLiteralNode || val instanceof JXObjectNode) {
      return FrameSlotKind.Object;
    }
    if (val instanceof JXBoolLiteralNode) {
      return FrameSlotKind.Boolean;
    }
    if (val instanceof JXNumberLiteralNode) {
      return ((JXNumberLiteralNode)val).hasDecimal() ? FrameSlotKind.Double : FrameSlotKind.Long;
    }
    return FrameSlotKind.Object;
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
