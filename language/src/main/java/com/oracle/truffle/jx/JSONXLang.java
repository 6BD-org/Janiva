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
package com.oracle.truffle.jx;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.TruffleLanguage.ContextPolicy;
import com.oracle.truffle.api.debug.DebuggerTags;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.builtins.*;
import com.oracle.truffle.jx.nodes.*;
import com.oracle.truffle.jx.nodes.controlflow.*;
import com.oracle.truffle.jx.nodes.expression.*;
import com.oracle.truffle.jx.nodes.expression.value.JXStringLiteralNode;
import com.oracle.truffle.jx.nodes.local.JXReadArgumentNode;
import com.oracle.truffle.jx.nodes.local.JXReadLocalVariableNode;
import com.oracle.truffle.jx.nodes.local.JXWriteLocalVariableNode;
import com.oracle.truffle.jx.parser.JSONXLangParser;
import com.oracle.truffle.jx.parser.JXNodeFactory;
import com.oracle.truffle.jx.runtime.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SL is a simple language to demonstrate and showcase features of Truffle. The implementation is as
 * simple and clean as possible in order to help understanding the ideas and concepts of Truffle.
 * The language has first class functions, and objects are key-value stores.
 *
 * <p>SL is dynamically typed, i.e., there are no type names specified by the programmer. SL is
 * strongly typed, i.e., there is no automatic conversion between types. If an operation is not
 * available for the types encountered at run time, a type error is reported and execution is
 * stopped. For example, {@code 4 - "2"} results in a type error because subtraction is only defined
 * for numbers.
 *
 * <p><b>Types:</b>
 *
 * <ul>
 *   <li>Number: arbitrary precision integer numbers. The implementation uses the Java primitive
 *       type {@code long} to represent numbers that fit into the 64 bit range, and {@link
 *       JXBigNumber} for numbers that exceed the range. Using a primitive type such as {@code long}
 *       is crucial for performance.
 *   <li>Boolean: implemented as the Java primitive type {@code boolean}.
 *   <li>String: implemented as the Java standard type {@link String}.
 *   <li>Function: implementation type {@link JXFunction}.
 *   <li>Object: efficient implementation using the object model provided by Truffle. The
 *       implementation type of objects is a subclass of {@link DynamicObject}.
 *   <li>Null (with only one value {@code null}): implemented as the singleton {@link
 *       JSNull#SINGLETON}.
 * </ul>
 *
 * The class {@link SLTypes} lists these types for the Truffle DSL, i.e., for type-specialized
 * operations that are specified using Truffle DSL annotations.
 *
 * <p><b>Language concepts:</b>
 *
 * <ul>
 *   <li>Literals for {@link JXBigIntegerLiteralNode numbers} , {@link JXStringLiteralNode strings},
 *       and {@link JXFunctionLiteralNode functions}.
 *   <li>Basic arithmetic, logical, and comparison operations: {@link JXAddNode +}, {@link JXSubNode
 *       -}, {@link JXMulNode *}, {@link JXDivNode /}, {@link JXLogicalAndNode logical and}, {@link
 *       JXLogicalOrNode logical or}, {@link JXEqualNode ==}, !=, {@link JXLessThanNode &lt;},
 *       {@link JXLessOrEqualNode &le;}, &gt;, &ge;.
 *   <li>Local variables: local variables must be defined (via a {@link JXWriteLocalVariableNode
 *       write}) before they can be used (by a {@link JXReadLocalVariableNode read}). Local
 *       variables are not visible outside of the block where they were first defined.
 *   <li>Basic control flow statements: {@link SLBlockNode blocks}, {@link JXIfNode if}, {@link
 *       SLWhileNode while} with {@link JXBreakNode break} and {@link SLContinueNode continue},
 *       {@link SLReturnNode return}.
 *   <li>Debugging control: {@link SLDebuggerNode debugger} statement uses {@link
 *       DebuggerTags#AlwaysHalt} tag to halt the execution when run under the debugger.
 *   <li>Function calls: {@link JXInvokeNode invocations} are efficiently implemented with {@link
 *       SLDispatchNode polymorphic inline caches}.
 *   <li>Object access: {@link JXReadPropertyNode} and {@link JXWritePropertyNode} use a cached
 *       {@link DynamicObjectLibrary} as the polymorphic inline cache for property reads and writes,
 *       respectively.
 * </ul>
 *
 * <p><b>Syntax and parsing:</b><br>
 * The syntax is described as an attributed grammar. The {@link SimpleLanguageParser} and {@link
 * SimpleLanguageLexer} are automatically generated by ANTLR 4. The grammar contains semantic
 * actions that build the AST for a method. To keep these semantic actions short, they are mostly
 * calls to the {@link JXNodeFactory} that performs the actual node creation. All functions found in
 * the SL source are added to the {@link JXFunctionRegistry}, which is accessible from the {@link
 * JXContext}.
 *
 * <p><b>Builtin functions:</b><br>
 * Library functions that are available to every SL source without prior definition are called
 * builtin functions. They are added to the {@link JXFunctionRegistry} when the {@link JXContext} is
 * created. Some of the current builtin functions are
 *
 * <ul>
 *   <li>{@link JXReadLnBuiltin readln}: Read a String from the {@link JXContext#getInput() standard
 *       input}.
 *   <li>{@link JXPrintLnBuiltin println}: Write a value to the {@link JXContext#getOutput()
 *       standard output}.
 *   <li>{@link JXNanoTimeBuiltin nanoTime}: Returns the value of a high-resolution time, in
 *       nanoseconds.
 *   <li>{@link JXDefineFunctionBuiltin defineFunction}: Parses the functions provided as a String
 *       argument and adds them to the function registry. Functions that are already defined are
 *       replaced with the new version.
 *   <li>{@link JXStackTraceBuiltin stckTrace}: Print all function activations with all local
 *       variables.
 * </ul>
 */
@TruffleLanguage.Registration(
    id = JSONXLang.ID,
    name = "JSONX",
    defaultMimeType = JSONXLang.MIME_TYPE,
    characterMimeTypes = JSONXLang.MIME_TYPE,
    contextPolicy = ContextPolicy.SHARED,
    fileTypeDetectors = JXFileDetector.class, //
    website = "https://www.graalvm.org/graalvm-as-a-platform/implement-language/")
@ProvidedTags({
  StandardTags.CallTag.class,
  StandardTags.StatementTag.class,
  StandardTags.RootTag.class,
  StandardTags.RootBodyTag.class,
  StandardTags.ExpressionTag.class,
  DebuggerTags.AlwaysHalt.class,
  StandardTags.ReadVariableTag.class,
  StandardTags.WriteVariableTag.class
})
public final class JSONXLang extends TruffleLanguage<JXContext> {
  public static volatile int counter;

  public static final String ID = "jsonx";
  public static final String MIME_TYPE = "application/jsonx";
  private static final Source BUILTIN_SOURCE =
      Source.newBuilder(JSONXLang.ID, "", "SL builtin").build();

  public static final TruffleString.Encoding STRING_ENCODING = TruffleString.Encoding.UTF_16;

  private final Assumption singleContext =
      Truffle.getRuntime().createAssumption("Single SL context.");

  private final Map<NodeFactory<? extends JXBuiltinNode>, RootCallTarget> builtinTargets =
      new ConcurrentHashMap<>();
  private final Map<TruffleString, RootCallTarget> undefinedFunctions = new ConcurrentHashMap<>();

  private final Shape rootShape;
  private final Shape jxArrayShape;

  public JSONXLang() {
    counter++;
    this.rootShape = Shape.newBuilder().layout(JXObject.class).build();
    this.jxArrayShape = Shape.newBuilder().layout(JXArray.class).build();
  }

  @Override
  protected JXContext createContext(Env env) {
    return new JXContext(this, env, new ArrayList<>(EXTERNAL_BUILTINS));
  }

  @Override
  protected boolean patchContext(JXContext context, Env newEnv) {
    context.patchContext(newEnv);
    return true;
  }

  public RootCallTarget getOrCreateUndefinedFunction(TruffleString name) {
    RootCallTarget target = undefinedFunctions.get(name);
    if (target == null) {
      target = new JXUndefinedFunctionRootNode(this, name).getCallTarget();
      RootCallTarget other = undefinedFunctions.putIfAbsent(name, target);
      if (other != null) {
        target = other;
      }
    }
    return target;
  }

  public RootCallTarget lookupBuiltin(NodeFactory<? extends JXBuiltinNode> factory) {
    RootCallTarget target = builtinTargets.get(factory);
    if (target != null) {
      return target;
    }

    /*
     * The builtin node factory is a class that is automatically generated by the Truffle DSL.
     * The signature returned by the factory reflects the signature of the @Specialization
     *
     * methods in the builtin classes.
     */
    int argumentCount = factory.getExecutionSignature().size();
    JXExpressionNode[] argumentNodes = new JXExpressionNode[argumentCount];
    /*
     * Builtin functions are like normal functions, i.e., the arguments are passed in as an
     * Object[] array encapsulated in SLArguments. A SLReadArgumentNode extracts a parameter
     * from this array.
     */
    for (int i = 0; i < argumentCount; i++) {
      argumentNodes[i] = new JXReadArgumentNode(i);
    }
    /* Instantiate the builtin node. This node performs the actual functionality. */
    JXBuiltinNode builtinBodyNode = factory.createNode((Object) argumentNodes);
    builtinBodyNode.addRootTag();
    /* The name of the builtin function is specified via an annotation on the node class. */
    TruffleString name =
        JXStrings.fromJavaString(lookupNodeInfo(builtinBodyNode.getClass()).shortName());
    builtinBodyNode.setUnavailableSourceSection();

    /* Wrap the builtin in a RootNode. Truffle requires all AST to start with a RootNode. */
    JXRootNode rootNode = new JXRootNode(this, new FrameDescriptor(), builtinBodyNode, name);

    /*
     * Register the builtin function in the builtin registry. Call targets for builtins may be
     * reused across multiple contexts.
     */
    RootCallTarget newTarget = rootNode.getCallTarget();
    RootCallTarget oldTarget = builtinTargets.putIfAbsent(factory, newTarget);
    if (oldTarget != null) {
      return oldTarget;
    }
    return newTarget;
  }

  public static NodeInfo lookupNodeInfo(Class<?> clazz) {
    if (clazz == null) {
      return null;
    }
    NodeInfo info = clazz.getAnnotation(NodeInfo.class);
    if (info != null) {
      return info;
    } else {
      return lookupNodeInfo(clazz.getSuperclass());
    }
  }

  @Override
  protected CallTarget parse(ParsingRequest request) throws Exception {
    Source source = request.getSource();
    RootNode rootNode;
    /*
     * Parse the provided source. At this point, we do not have a SLContext yet. Registration of
     * the functions with the SLContext happens lazily in SLEvalRootNode.
     */
    rootNode = JSONXLangParser.parseSL(this, source);

    RootCallTarget main = rootNode.getCallTarget();
    RootNode evalMain;
    if (main != null) {
      /*
       * We have a main function, so "evaluating" the parsed source means invoking that main
       * function. However, we need to lazily register functions into the SLContext first, so
       * we cannot use the original SLRootNode for the main function. Instead, we create a new
       * SLEvalRootNode that does everything we need.
       */
      evalMain = new SLEvalRootNode(this, main);
    } else {
      /*
       * Even without a main function, "evaluating" the parsed source needs to register the
       * functions into the SLContext.
       */
      evalMain = new SLEvalRootNode(this, null);
    }
    return evalMain.getCallTarget();
  }

  /**
   * SLLanguage specifies the {@link ContextPolicy#SHARED} in {@link Registration#contextPolicy()}.
   * This means that a single {@link TruffleLanguage} instance can be reused for multiple language
   * contexts. Before this happens the Truffle framework notifies the language by invoking {@link
   * #initializeMultipleContexts()}. This allows the language to invalidate certain assumptions
   * taken for the single context case. One assumption SL takes for single context case is located
   * in {@link SLEvalRootNode}. There functions are only tried to be registered once in the single
   * context case, but produce a boundary call in the multi context case, as function registration
   * is expected to happen more than once.
   *
   * <p>Value identity caches should be avoided and invalidated for the multiple contexts case as no
   * value will be the same. Instead, in multi context case, a language should only use types,
   * shapes and code to speculate.
   *
   * <p>For a new language it is recommended to start with {@link ContextPolicy#EXCLUSIVE} and as
   * the language gets more mature switch to {@link ContextPolicy#SHARED}.
   */
  @Override
  protected void initializeMultipleContexts() {
    singleContext.invalidate();
  }

  public boolean isSingleContext() {
    return singleContext.isValid();
  }

  @Override
  protected Object getLanguageView(JXContext context, Object value) {
    return JXLanguageView.create(value);
  }

  @Override
  protected boolean isVisible(JXContext context, Object value) {
    return !InteropLibrary.getFactory().getUncached(value).isNull(value);
  }

  @Override
  protected Object getScope(JXContext context) {
    return context.getFunctionRegistry().getFunctionsObject();
  }

  public Shape getRootShape() {
    return rootShape;
  }

  /**
   * Allocate an empty object. All new objects initially have no properties. Properties are added
   * when they are first stored, i.e., the store triggers a shape change of the object.
   */
  public JXObject createObject(AllocationReporter reporter) {
    reporter.onEnter(null, 0, AllocationReporter.SIZE_UNKNOWN);
    JXObject object = new JXObject(rootShape);
    reporter.onReturnValue(object, 0, AllocationReporter.SIZE_UNKNOWN);
    return object;
  }

  public JXArray createJXArray(AllocationReporter reporter, int size) {
    reporter.onEnter(null, 0, AllocationReporter.SIZE_UNKNOWN);
    JXArray array = new JXArray(jxArrayShape, new Object[size]);
    reporter.onReturnValue(array, 0, AllocationReporter.SIZE_UNKNOWN);
    return array;
  }

  private static final LanguageReference<JSONXLang> REFERENCE =
      LanguageReference.create(JSONXLang.class);

  public static JSONXLang get(Node node) {
    return REFERENCE.get(node);
  }

  private static final List<NodeFactory<? extends JXBuiltinNode>> EXTERNAL_BUILTINS =
      Collections.synchronizedList(new ArrayList<>());

  public static void installBuiltin(NodeFactory<? extends JXBuiltinNode> builtin) {
    EXTERNAL_BUILTINS.add(builtin);
  }

  @Override
  protected void exitContext(JXContext context, ExitMode exitMode, int exitCode) {
    /*
     * Runs shutdown hooks during explicit exit triggered by TruffleContext#closeExit(Node, int)
     * or natural exit triggered during natural context close.
     */
    context.runShutdownHooks();
  }
}
