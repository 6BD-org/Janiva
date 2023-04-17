package com.oracle.truffle.jx.parser;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.expression.value.JXBoolLiteralNode;
import com.oracle.truffle.jx.nodes.expression.value.JXNumberLiteralNode;
import com.oracle.truffle.jx.nodes.expression.value.JXObjectNode;
import com.oracle.truffle.jx.nodes.expression.value.JXStringLiteralNode;
import java.util.*;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaStack {
  private static final Logger logger = LoggerFactory.getLogger(MetaStack.class);

  enum ScopeType {
    OBJECT,
    ARRAY,
    LAMBDA,
    GLOBAL,
  }

  /**
   * Local variable names that are visible in the current block. Variables are not visible outside
   * of their defining block, to prevent the usage of undefined variables. Because of that, we can
   * decide during parsing if a name references a local variable or is a function name.
   */
  static class LexicalScope {
    protected final ScopeType type;
    protected final LexicalScope outer;
    protected final Map<TruffleString, Integer> locals;
    protected final Map<TruffleString, Integer> latents;
    protected final List<JXExpressionNode> arrayNodes;

    LexicalScope(LexicalScope outer, ScopeType type) {
      this.outer = outer;
      this.locals = new HashMap<>();
      this.latents = new HashMap<>();
      this.arrayNodes = new LinkedList<>();
      this.type = type;
    }

    public Integer find(TruffleString name, boolean includeOuter) {
      Integer result = or(() -> latents.get(name), () -> locals.get(name));
      if (result != null) {
        return result;
      } else if (outer != null && includeOuter) {
        return outer.find(name, true);
      } else {
        return null;
      }
    }

    private <T> T or(Supplier<T> first, Supplier<T> second) {
      T f = first.get();
      if (f != null) {
        return f;
      }
      return second.get();
    }
  }

  public MetaStack() {
    this.lexicalScope = new LexicalScope(null, ScopeType.GLOBAL);
    ;
    this.frameStack = new Stack<>();
    frameStack.push(FrameDescriptor.newBuilder());
    root = frameStack.peek();

    // init global
    this.globalScope = this.lexicalScope;
    this.globalFrameDescBuilder = frameStack.peek();
  }

  private Stack<FrameDescriptor.Builder> frameStack;
  private FrameDescriptor.Builder root;
  private LexicalScope lexicalScope;
  private final LexicalScope globalScope;
  private final FrameDescriptor.Builder globalFrameDescBuilder;

  public void startObject() {
    this.lexicalScope = new LexicalScope(lexicalScope, ScopeType.OBJECT);
  }

  public void startArray() {
    this.lexicalScope = new LexicalScope(lexicalScope, ScopeType.ARRAY);
  }

  public void startLambda() {
    logger.debug("Opening lambda scope");
    this.lexicalScope = new LexicalScope(lexicalScope, ScopeType.LAMBDA);
    this.frameStack.push(FrameDescriptor.newBuilder());
  }

  public boolean isCurrentLambdaScope() {
    LexicalScope s = this.lexicalScope;
    while (s != null) {
      if (s.type == ScopeType.LAMBDA) {
        return true;
      }
      s = s.outer;
    }
    return false;
  }

  public void appendArray(JXExpressionNode node) {
    this.lexicalScope.arrayNodes.add(node);
  }

  public void close() {
    if (this.lexicalScope.type == ScopeType.LAMBDA) {
      logger.debug("Closing lambda scope");
      this.frameStack.pop();
    }
    this.lexicalScope = lexicalScope.outer;
  }

  public Integer lookupAttribute(TruffleString attributeName, boolean includeOuter) {
    return lexicalScope.find(attributeName, includeOuter);
  }

  public Integer requestForLatentSlot(TruffleString attributeName, JXExpressionNode val) {
    int slot = frameStack.peek().addSlot(inferSlotKind(val), attributeName, null);
    lexicalScope.latents.putIfAbsent(attributeName, slot);
    logger.debug("requesting latent for slot {} -> {}", attributeName, slot);
    return slot;
  }

  public Integer requestForSlot(TruffleString attributeName, JXExpressionNode val) {
    int slot = frameStack.peek().addSlot(inferSlotKind(val), attributeName, null);
    lexicalScope.locals.putIfAbsent(attributeName, slot);
    logger.debug("requesting for slot {} -> {}", attributeName, slot);

    return slot;
  }

  /**
   * Request for a global slot, which is located in the outer most scope
   *
   * @param attributeName
   * @return
   */
  public Integer requestForGlobal(TruffleString attributeName) {
    int slot = globalFrameDescBuilder.addSlot(FrameSlotKind.Object, attributeName, null);
    globalScope.locals.putIfAbsent(attributeName, slot);
    logger.debug("requesting for slot {} -> {}", attributeName, slot);
    return slot;
  }

  public FrameDescriptor buildRoot() {
    return root.build();
  }

  public FrameDescriptor buildTop() {
    return frameStack.peek().build();
  }

  public Map<TruffleString, Integer> locals() {
    return lexicalScope.locals;
  }

  public List<JXExpressionNode> arrayNodes() {
    return lexicalScope.arrayNodes;
  }

  private FrameSlotKind inferSlotKind(JXExpressionNode val) {
    if (val instanceof JXStringLiteralNode || val instanceof JXObjectNode) {
      return FrameSlotKind.Object;
    }
    if (val instanceof JXBoolLiteralNode) {
      return FrameSlotKind.Boolean;
    }
    if (val instanceof JXNumberLiteralNode) {
      return ((JXNumberLiteralNode) val).hasDecimal() ? FrameSlotKind.Double : FrameSlotKind.Long;
    }
    return FrameSlotKind.Object;
  }
}
