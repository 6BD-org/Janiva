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
package com.oracle.truffle.jx.runtime;

import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.JSONXLang;
import com.oracle.truffle.jx.builtins.*;
import org.graalvm.polyglot.Context;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.ContextReference;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.jx.builtins.JXReadLnBuiltin;

/**
 * The run-time state of SL during execution. The context is created by the {@link JSONXLang}. It
 * is used, for example, by {@link JXBuiltinNode#getContext() builtin functions}.
 *
 * <p>It would be an error to have two different context instances during the execution of one
 * script. However, if two separate scripts run in one Java VM at the same time, they have a
 * different context. Therefore, the context is not a singleton.
 */
public final class JXContext {

  private final JSONXLang language;
  @CompilationFinal private Env env;
  private final BufferedReader input;
  private final PrintWriter output;
  private final JXFunctionRegistry functionRegistry;
  private final AllocationReporter allocationReporter;
  private final List<JXFunction> shutdownHooks = new ArrayList<>();

  public JXContext(
      JSONXLang language,
      TruffleLanguage.Env env,
      List<NodeFactory<? extends JXBuiltinNode>> externalBuiltins) {
    this.env = env;
    this.input = new BufferedReader(new InputStreamReader(env.in()));
    this.output = new PrintWriter(env.out(), true);
    this.language = language;
    this.allocationReporter = env.lookup(AllocationReporter.class);
    this.functionRegistry = new JXFunctionRegistry(language);
    installBuiltins();
    for (NodeFactory<? extends JXBuiltinNode> builtin : externalBuiltins) {
      installBuiltin(builtin);
    }
  }

  /**
   * Patches the {@link JXContext} to use a new {@link Env}. The method is called during the native
   * image execution as a consequence of {@link Context#create(java.lang.String...)}.
   *
   * @param newEnv the new {@link Env} to use.
   * @see TruffleLanguage#patchContext(Object, Env)
   */
  public void patchContext(Env newEnv) {
    this.env = newEnv;
  }

  /** Return the current Truffle environment. */
  public Env getEnv() {
    return env;
  }

  /**
   * Returns the default input, i.e., the source for the {@link JXReadLnBuiltin}. To allow unit
   * testing, we do not use {@link System#in} directly.
   */
  public BufferedReader getInput() {
    return input;
  }

  /**
   * The default default, i.e., the output for the {@link JXPrintLnBuiltin}. To allow unit testing,
   * we do not use {@link System#out} directly.
   */
  public PrintWriter getOutput() {
    return output;
  }

  /** Returns the registry of all functions that are currently defined. */
  public JXFunctionRegistry getFunctionRegistry() {
    return functionRegistry;
  }

  /**
   * Adds all builtin functions to the {@link JXFunctionRegistry}. This method lists all {@link
   * JXBuiltinNode builtin implementation classes}.
   */
  private void installBuiltins() {
    installBuiltin(JXReadLnBuiltinFactory.getInstance());
    installBuiltin(JXPrintLnBuiltinFactory.getInstance());
    installBuiltin(JXNanoTimeBuiltinFactory.getInstance());
    installBuiltin(JXDefineFunctionBuiltinFactory.getInstance());
    installBuiltin(JXStackTraceBuiltinFactory.getInstance());
    installBuiltin(JXHelloEqualsWorldBuiltinFactory.getInstance());
    installBuiltin(JXEvalBuiltinFactory.getInstance());
    installBuiltin(JXImportBuiltinFactory.getInstance());
    installBuiltin(JXGetSizeBuiltinFactory.getInstance());
    installBuiltin(JXHasSizeBuiltinFactory.getInstance());
    installBuiltin(JXIsExecutableBuiltinFactory.getInstance());
    installBuiltin(JXIsNullBuiltinFactory.getInstance());
    installBuiltin(JXWrapPrimitiveBuiltinFactory.getInstance());
    installBuiltin(JXTypeOfBuiltinFactory.getInstance());
    installBuiltin(JXIsInstanceBuiltinFactory.getInstance());
    installBuiltin(JXJavaTypeBuiltinFactory.getInstance());
    installBuiltin(JXExitBuiltinFactory.getInstance());
    installBuiltin(JXRegisterShutdownHookBuiltinFactory.getInstance());
    installBuiltin(JXAddToHostClassPathBuiltinFactory.getInstance());
  }

  public void installBuiltin(NodeFactory<? extends JXBuiltinNode> factory) {
    /* Register the builtin function in our function registry. */
    RootCallTarget target = language.lookupBuiltin(factory);
    getFunctionRegistry().register(JXStrings.getSLRootName(target.getRootNode()), target);
  }

  /*
   * Methods for object creation / object property access.
   */
  public AllocationReporter getAllocationReporter() {
    return allocationReporter;
  }

  /*
   * Methods for language interoperability.
   */
  public static Object fromForeignValue(Object a) {
    if (a instanceof Long
        || a instanceof JXBigNumber
        || a instanceof String
        || a instanceof TruffleString
        || a instanceof Boolean) {
      return a;
    } else if (a instanceof Character) {
      return fromForeignCharacter((Character) a);
    } else if (a instanceof Number) {
      return fromForeignNumber(a);
    } else if (a instanceof TruffleObject) {
      return a;
    } else if (a instanceof JXContext) {
      return a;
    }
    throw shouldNotReachHere("Value is not a truffle value.");
  }

  @TruffleBoundary
  private static long fromForeignNumber(Object a) {
    return ((Number) a).longValue();
  }

  @TruffleBoundary
  private static String fromForeignCharacter(char c) {
    return String.valueOf(c);
  }

  public CallTarget parse(Source source) {
    return env.parsePublic(source);
  }

  /**
   * Returns an object that contains bindings that were exported across all used languages. To read
   * or write from this object the {@link TruffleObject interop} API can be used.
   */
  public TruffleObject getPolyglotBindings() {
    return (TruffleObject) env.getPolyglotBindings();
  }

  private static final ContextReference<JXContext> REFERENCE =
      ContextReference.create(JSONXLang.class);

  public static JXContext get(Node node) {
    return REFERENCE.get(node);
  }

  /**
   * Register a function as a shutdown hook. Only no-parameter functions are supported.
   *
   * @param func no-parameter function to be registered as a shutdown hook
   */
  @TruffleBoundary
  public void registerShutdownHook(JXFunction func) {
    shutdownHooks.add(func);
  }

  /**
   * Run registered shutdown hooks. This method is designed to be executed in {@link
   * TruffleLanguage#exitContext(Object, TruffleLanguage.ExitMode, int)}.
   */
  public void runShutdownHooks() {
    InteropLibrary interopLibrary = InteropLibrary.getUncached();
    for (JXFunction shutdownHook : shutdownHooks) {
      try {
        interopLibrary.execute(shutdownHook);
      } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
        throw shouldNotReachHere("Shutdown hook is not executable!", e);
      }
    }
  }
}
