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
package com.oracle.truffle.sl.parser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.sl.JSONXLang;
import com.oracle.truffle.sl.nodes.JXExpressionNode;
import com.oracle.truffle.sl.nodes.JXRootNode;
import com.oracle.truffle.sl.nodes.JXStatementNode;
import com.oracle.truffle.sl.nodes.controlflow.*;
import com.oracle.truffle.sl.nodes.expression.*;
import com.oracle.truffle.sl.nodes.expression.value.JXBoolLiteralNode;
import com.oracle.truffle.sl.nodes.expression.value.JXNumberLiteralNode;
import com.oracle.truffle.sl.nodes.local.*;
import com.oracle.truffle.sl.nodes.util.JXUnboxNodeGen;
import com.oracle.truffle.sl.runtime.SLStrings;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.sl.nodes.controlflow.JXFunctionBodyNode;
import com.oracle.truffle.sl.nodes.expression.JXFunctionLiteralNode;
import com.oracle.truffle.sl.nodes.expression.value.JXStringLiteralNode;
import com.oracle.truffle.sl.nodes.local.JXReadLocalVariableNode;

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
  private final Map<TruffleString, RootCallTarget> allFunctions;

  /* State while parsing a function. */
  private int functionStartPos;
  private TruffleString functionName;
  private int functionBodyStartPos; // includes parameter list
  private int parameterCount;
  private FrameDescriptor.Builder frameDescriptorBuilder;
  private List<JXStatementNode> methodNodes;

  private JXExpressionNode rootNode;

  /* State while parsing a block. */
  private LexicalScope lexicalScope;
  private final JSONXLang language;

  public JXNodeFactory(JSONXLang language, Source source) {
    this.language = language;
    this.source = source;
    this.sourceString = SLStrings.fromJavaString(source.getCharacters().toString());
    this.allFunctions = new HashMap<>();
  }

  public Map<TruffleString, RootCallTarget> getAllFunctions() {
    return allFunctions;
  }

  public void startFunction(Token nameToken, Token bodyStartToken) {
    assert functionStartPos == 0;
    assert functionName == null;
    assert functionBodyStartPos == 0;
    assert parameterCount == 0;
    assert frameDescriptorBuilder == null;
    assert lexicalScope == null;

    functionStartPos = nameToken.getStartIndex();
    functionName = asTruffleString(nameToken, false);
    functionBodyStartPos = bodyStartToken.getStartIndex();
    frameDescriptorBuilder = FrameDescriptor.newBuilder();
    methodNodes = new ArrayList<>();
    startBlock();
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

  public void finishFunction(JXStatementNode bodyNode) {
    if (bodyNode == null) {
      // a state update that would otherwise be performed by finishBlock
      lexicalScope = lexicalScope.outer;
    } else {
      methodNodes.add(bodyNode);
      final int bodyEndPos = bodyNode.getSourceEndIndex();
      final SourceSection functionSrc =
          source.createSection(functionStartPos, bodyEndPos - functionStartPos);
      final JXStatementNode methodBlock =
          finishBlock(
              methodNodes, parameterCount, functionBodyStartPos, bodyEndPos - functionBodyStartPos);
      assert lexicalScope == null : "Wrong scoping of blocks in parser";

      final JXFunctionBodyNode functionBodyNode = new JXFunctionBodyNode(methodBlock);
      functionBodyNode.setSourceSection(functionSrc.getCharIndex(), functionSrc.getCharLength());

      final JXRootNode rootNode =
          new JXRootNode(
              language,
              frameDescriptorBuilder.build(),
              functionBodyNode,
              functionName);
      allFunctions.put(functionName, rootNode.getCallTarget());
    }

    functionStartPos = 0;
    functionName = null;
    functionBodyStartPos = 0;
    parameterCount = 0;
    frameDescriptorBuilder = null;
    lexicalScope = null;
  }

  public void registerRootNode(JXExpressionNode node) {
    frameDescriptorBuilder = FrameDescriptor.newBuilder();
    this.rootNode = node;
  }

  public RootNode getRootNode() {
    return new JXRootNode(
            language, frameDescriptorBuilder.build(), rootNode, SLStrings.fromJavaString("#root")
    );
  }


  public JXExpressionNode createDecimal(Token whole, Token dec) {
    if (dec == null) {
      return new JXNumberLiteralNode(new BigDecimal(whole.getText()));
    }
    return new JXNumberLiteralNode(new BigDecimal(whole.getText() + "." + dec.getText()));
  }

  public void startBlock() {
    lexicalScope = new LexicalScope(lexicalScope);
  }

  public JXStatementNode finishBlock(List<JXStatementNode> bodyNodes, int startPos, int length) {
    return finishBlock(bodyNodes, 0, startPos, length);
  }

  public JXStatementNode finishBlock(
          List<JXStatementNode> bodyNodes, int skipCount, int startPos, int length) {
    lexicalScope = lexicalScope.outer;

    if (containsNull(bodyNodes)) {
      return null;
    }

    List<JXStatementNode> flattenedNodes = new ArrayList<>(bodyNodes.size());
    flattenBlocks(bodyNodes, flattenedNodes);
    int n = flattenedNodes.size();
    for (int i = skipCount; i < n; i++) {
      JXStatementNode statement = flattenedNodes.get(i);
      if (statement.hasSource() && !isHaltInCondition(statement)) {
        statement.addStatementTag();
      }
    }
    SLBlockNode blockNode =
        new SLBlockNode(flattenedNodes.toArray(new JXStatementNode[flattenedNodes.size()]));
    blockNode.setSourceSection(startPos, length);
    return blockNode;
  }

  private static boolean isHaltInCondition(JXStatementNode statement) {
    return (statement instanceof JXIfNode) || (statement instanceof SLWhileNode);
  }

  private void flattenBlocks(
          Iterable<? extends JXStatementNode> bodyNodes, List<JXStatementNode> flattenedNodes) {
    for (JXStatementNode n : bodyNodes) {
      if (n instanceof SLBlockNode) {
        flattenBlocks(((SLBlockNode) n).getStatements(), flattenedNodes);
      } else {
        flattenedNodes.add(n);
      }
    }
  }

  /**
   * Returns an {@link SLDebuggerNode} for the given token.
   *
   * @param debuggerToken The token containing the debugger node's info.
   * @return A SLDebuggerNode for the given token.
   */
  JXStatementNode createDebugger(Token debuggerToken) {
    final SLDebuggerNode debuggerNode = new SLDebuggerNode();
    srcFromToken(debuggerNode, debuggerToken);
    return debuggerNode;
  }

  /**
   * Returns an {@link JXBreakNode} for the given token.
   *
   * @param breakToken The token containing the break node's info.
   * @return A SLBreakNode for the given token.
   */
  public JXStatementNode createBreak(Token breakToken) {
    final JXBreakNode breakNode = new JXBreakNode();
    srcFromToken(breakNode, breakToken);
    return breakNode;
  }

  /**
   * Returns an {@link SLContinueNode} for the given token.
   *
   * @param continueToken The token containing the continue node's info.
   * @return A SLContinueNode built using the given token.
   */
  public JXStatementNode createContinue(Token continueToken) {
    final SLContinueNode continueNode = new SLContinueNode();
    srcFromToken(continueNode, continueToken);
    return continueNode;
  }

  /**
   * Returns an {@link SLWhileNode} for the given parameters.
   *
   * @param whileToken The token containing the while node's info
   * @param conditionNode The conditional node for this while loop
   * @param bodyNode The body of the while loop
   * @return A SLWhileNode built using the given parameters. null if either conditionNode or
   *     bodyNode is null.
   */
  public JXStatementNode createWhile(
          Token whileToken, JXExpressionNode conditionNode, JXStatementNode bodyNode) {
    if (conditionNode == null || bodyNode == null) {
      return null;
    }

    conditionNode.addStatementTag();
    final int start = whileToken.getStartIndex();
    final int end = bodyNode.getSourceEndIndex();
    final SLWhileNode whileNode = new SLWhileNode(conditionNode, bodyNode);
    whileNode.setSourceSection(start, end - start);
    return whileNode;
  }

  /**
   * Returns an {@link JXIfNode} for the given parameters.
   *
   * @param ifToken The token containing the if node's info
   * @param conditionNode The condition node of this if statement
   * @param thenPartNode The then part of the if
   * @param elsePartNode The else part of the if (null if no else part)
   * @return An SLIfNode for the given parameters. null if either conditionNode or thenPartNode is
   *     null.
   */
  public JXStatementNode createIf(
      Token ifToken,
      JXExpressionNode conditionNode,
      JXStatementNode thenPartNode,
      JXStatementNode elsePartNode) {
    if (conditionNode == null || thenPartNode == null) {
      return null;
    }

    conditionNode.addStatementTag();
    final int start = ifToken.getStartIndex();
    final int end =
        elsePartNode == null ? thenPartNode.getSourceEndIndex() : elsePartNode.getSourceEndIndex();
    final JXIfNode ifNode = new JXIfNode(conditionNode, thenPartNode, elsePartNode);
    ifNode.setSourceSection(start, end - start);
    return ifNode;
  }

  /**
   * Returns an {@link SLReturnNode} for the given parameters.
   *
   * @param t The token containing the return node's info
   * @param valueNode The value of the return (null if not returning a value)
   * @return An SLReturnNode for the given parameters.
   */
  public JXStatementNode createReturn(Token t, JXExpressionNode valueNode) {
    final int start = t.getStartIndex();
    final int length =
        valueNode == null ? t.getText().length() : valueNode.getSourceEndIndex() - start;
    final SLReturnNode returnNode = new SLReturnNode(valueNode);
    returnNode.setSourceSection(start, length);
    return returnNode;
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
   * Returns an {@link JXInvokeNode} for the given parameters.
   *
   * @param functionNode The function being called
   * @param parameterNodes The parameters of the function call
   * @param finalToken A token used to determine the end of the sourceSelection for this call
   * @return An SLInvokeNode for the given parameters. null if functionNode or any of the
   *     parameterNodes are null.
   */
  public JXExpressionNode createCall(
          JXExpressionNode functionNode, List<JXExpressionNode> parameterNodes, Token finalToken) {
    if (functionNode == null || containsNull(parameterNodes)) {
      return null;
    }

    final JXExpressionNode result =
        new JXInvokeNode(
            functionNode, parameterNodes.toArray(new JXExpressionNode[parameterNodes.size()]));

    final int startPos = functionNode.getSourceCharIndex();
    final int endPos = finalToken.getStartIndex() + finalToken.getText().length();
    result.setSourceSection(startPos, endPos - startPos);
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

  /**
   * Returns a {@link JXReadLocalVariableNode} if this read is a local variable or a {@link
   * JXFunctionLiteralNode} if this read is global. In SL, the only global names are functions.
   *
   * @param nameNode The name of the variable/function being read
   * @return either:
   *     <ul>
   *       <li>A SLReadLocalVariableNode representing the local variable being read.
   *       <li>A SLFunctionLiteralNode representing the function definition.
   *       <li>null if nameNode is null.
   *     </ul>
   */
  public JXExpressionNode createRead(JXExpressionNode nameNode) {
    if (nameNode == null) {
      return null;
    }

    TruffleString name = ((JXStringLiteralNode) nameNode).executeGeneric(null);
    final JXExpressionNode result;
    final Integer frameSlot = lexicalScope.find(name);
    if (frameSlot != null) {
      /* Read of a local variable. */
      result = JXReadLocalVariableNodeGen.create(frameSlot);
    } else {
      /* Read of a global name. In our language, the only global names are functions. */
      result = new JXFunctionLiteralNode(name);
    }
    result.setSourceSection(nameNode.getSourceCharIndex(), nameNode.getSourceLength());
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
        fromIndex * 2, length * 2, JSONXLang.STRING_ENCODING, true);
  }

  public JXExpressionNode createNumericLiteral(Token literalToken) {
    JXExpressionNode result;
    try {
      /* Try if the literal is small enough to fit into a long value. */
      result = new JXLongLiteralNode(Long.parseLong(literalToken.getText()));
    } catch (NumberFormatException ex) {
      /* Overflow of long value, so fall back to BigInteger. */
      result = new JXBigIntegerLiteralNode(new BigInteger(literalToken.getText()));
    }
    srcFromToken(result, literalToken);
    result.addExpressionTag();
    return result;
  }

  public JXExpressionNode createParenExpression(
          JXExpressionNode expressionNode, int start, int length) {
    if (expressionNode == null) {
      return null;
    }

    final JXParenExpressionNode result = new JXParenExpressionNode(expressionNode);
    result.setSourceSection(start, length);
    return result;
  }

  /**
   * Returns an {@link JXReadPropertyNode} for the given parameters.
   *
   * @param receiverNode The receiver of the property access
   * @param nameNode The name of the property being accessed
   * @return An SLExpressionNode for the given parameters. null if receiverNode or nameNode is null.
   */
  public JXExpressionNode createReadProperty(
          JXExpressionNode receiverNode, JXExpressionNode nameNode) {
    if (receiverNode == null || nameNode == null) {
      return null;
    }

    final JXExpressionNode result = JXReadPropertyNodeGen.create(receiverNode, nameNode);

    final int startPos = receiverNode.getSourceCharIndex();
    final int endPos = nameNode.getSourceEndIndex();
    result.setSourceSection(startPos, endPos - startPos);
    result.addExpressionTag();

    return result;
  }

  /**
   * Returns an {@link JXWritePropertyNode} for the given parameters.
   *
   * @param receiverNode The receiver object of the property assignment
   * @param nameNode The name of the property being assigned
   * @param valueNode The value to be assigned
   * @return An SLExpressionNode for the given parameters. null if receiverNode, nameNode or
   *     valueNode is null.
   */
  public JXExpressionNode createWriteProperty(
          JXExpressionNode receiverNode, JXExpressionNode nameNode, JXExpressionNode valueNode) {
    if (receiverNode == null || nameNode == null || valueNode == null) {
      return null;
    }

    final JXExpressionNode result =
        JXWritePropertyNodeGen.create(receiverNode, nameNode, valueNode);

    final int start = receiverNode.getSourceCharIndex();
    final int length = valueNode.getSourceEndIndex() - start;
    result.setSourceSection(start, length);
    result.addExpressionTag();

    return result;
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
