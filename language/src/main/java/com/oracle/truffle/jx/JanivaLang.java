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
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.builtins.*;
import com.oracle.truffle.jx.nodes.*;
import com.oracle.truffle.jx.nodes.local.JXReadArgumentNode;
import com.oracle.truffle.jx.parser.JanivaLangParser;
import com.oracle.truffle.jx.runtime.*;
import com.oracle.truffle.jx.statics.lambda.BuiltInLambda;
import com.oracle.truffle.jx.statics.lambda.LambdaRegistry;
import com.oracle.truffle.jx.statics.lambda.LambdaTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** */
@TruffleLanguage.Registration(
    id = JanivaLang.ID,
    name = "Janiva",
    defaultMimeType = JanivaLang.MIME_TYPE,
    characterMimeTypes = JanivaLang.MIME_TYPE,
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
public final class JanivaLang extends TruffleLanguage<JXContext> {

  public static final String ID = "janiva";
  public static final String MIME_TYPE = "application/janiva";

  public static final TruffleString.Encoding STRING_ENCODING = TruffleString.Encoding.UTF_16;

  private final Assumption singleContext =
      Truffle.getRuntime().createAssumption("Single SL context.");

  private final Map<NodeFactory<? extends JXBuiltinNode>, RootCallTarget> builtinTargets =
      new ConcurrentHashMap<>();
  private final Map<TruffleString, RootCallTarget> undefinedFunctions = new ConcurrentHashMap<>();

  private final Shape rootShape;
  private final Shape jxArrayShape;

  public JanivaLang() {
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
  public CallTarget parse(ParsingRequest request) throws Exception {
    Source source = request.getSource();
    RootNode rootNode;

    this.installBuiltInLambdas();
    this.initializeReservedKeywords();

    /*
     * Parse the provided source. At this point, we do not have a SLContext yet. Registration of
     * the functions with the SLContext happens lazily in SLEvalRootNode.
     */
    rootNode = JanivaLangParser.parseSL(this, source, null);

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

  private static final LanguageReference<JanivaLang> REFERENCE =
      LanguageReference.create(JanivaLang.class);

  public static JanivaLang get(Node node) {
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

  private void installBuiltInLambdas() {
    for (BuiltInLambda builtIn : BuiltInLambda.values()) {
      LambdaTemplate lt = new LambdaTemplate(builtIn.lambdaName());
      LambdaRegistry.registerBuiltIn(lt);
      String name = builtIn.lambdaNameInJavaString();
      Reserved.register(name, "Built-in lambda: @" + name);
    }
  }

  private void initializeReservedKeywords() {
    Reserved.register("import", "primitive: @import");
    Reserved.register("namespace", "primitive: @namespace");
  }
}
