/*
 * Copyright (c) 2012, 2019, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.jx.nodes.controlflow;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.jx.JXException;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.util.JXUnboxNodeGen;

@NodeInfo(shortName = "if", description = "The node implementing a condional statement")
public final class JXIfNode extends JXExpressionNode {

  /**
   * The condition of the {@code if}. This in a {@link JXExpressionNode} because we require a result
   * value. We do not have a node type that can only return a {@code boolean} value, so {@link
   * #evaluateCondition executing the condition} can lead to a type error.
   */
  @Child private JXExpressionNode conditionNode;

  /** Statement (or {@link SLBlockNode block}) executed when the condition is true. */
  @Child private JXExpressionNode thenPartNode;

  /** Statement (or {@link SLBlockNode block}) executed when the condition is false. */
  @Child private JXExpressionNode elsePartNode;

  private final ConditionProfile condition = ConditionProfile.createCountingProfile();

  public JXIfNode(
      JXExpressionNode conditionNode,
      JXExpressionNode thenPartNode,
      JXExpressionNode elsePartNode) {
    this.conditionNode = JXUnboxNodeGen.create(conditionNode);
    this.thenPartNode = thenPartNode;
    this.elsePartNode = elsePartNode;
    this.insert(conditionNode);
    this.insert(thenPartNode);
    this.insert(elsePartNode);
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    /*
     * In the interpreter, record profiling information that the condition was executed and with
     * which outcome.
     */
    boolean cond = evaluateCondition(frame);
    if (condition.profile(cond)) {
      /* Execute the then-branch. */
      return thenPartNode.executeGeneric(frame);
    } else {
      /* Execute the else-branch (which is optional according to the SL syntax). */
      if (elsePartNode != null) {
        return elsePartNode.executeGeneric(frame);
      }
    }
    return null;
  }

  private boolean evaluateCondition(VirtualFrame frame) {
    try {
      /*
       * The condition must evaluate to a boolean value, so we call the boolean-specialized
       * execute method.
       */
      return conditionNode.executeBoolean(frame);
    } catch (UnexpectedResultException ex) {
      /*
       * The condition evaluated to a non-boolean result. This is a type error in the SL
       * program.
       */
      throw JXException.typeError(this, ex.getResult());
    }
  }
}
