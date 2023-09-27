package com.oracle.truffle.jx.runtime;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.interop.*;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.jx.constants.CacheLimits;
import com.oracle.truffle.jx.constants.Constants;
import com.oracle.truffle.jx.statics.lambda.LambdaTemplate;
import java.math.BigInteger;

@ExportLibrary(InteropLibrary.class)
@ExportLibrary(LambdaLibrary.class)
public class JXPartialLambda extends DynamicObject implements TruffleObject {
  private static final Shape SHAPE = Shape.newBuilder().layout(JXPartialLambda.class).build();
  private static final String DELEGATE_LIB_CACHE = CacheLimits.DELEGATE_LIB_CACHE;
  private static final Object[] EMPTY_ARGS = Constants.EMPTY_ARGS;
  private final CallTarget callTarget;
  private final Object[] partialArgs;
  private int offset;
  private final LambdaTemplate template;

  /** Once partial lambda is executed, result is cached to this field */
  private Object outputCache = JanivaVoid.VOID;

  public JXPartialLambda(CallTarget callTarget, LambdaTemplate template) {
    super(SHAPE);
    this.callTarget = callTarget;
    this.partialArgs = new Object[template.parameterCount()];
    this.offset = 0;
    this.template = template;
  }

  @ExportMessage
  public boolean isExecutable() {
    return !isPartialApplication();
  }

  private boolean isPartialApplication() {
    return offset < template.parameterCount();
  }

  @ExportMessage(library = InteropLibrary.class)
  public Object execute(Object[] args) {
    if (this.outputCache != JanivaVoid.VOID) {
      return this.outputCache;
    }
    this.outputCache = callTarget.call(this.partialArgs);
    return this.outputCache;
  }

  @ExportMessage(library = LambdaLibrary.class)
  public JXPartialLambda mergeArgs(Object[] args) {
    if (args.length == 0) {
      return this;
    }
    if (args.length + offset > template.parameterCount()) {
      template.throwParameterLenNotMatch(args.length + offset);
    }
    for (Object arg : args) {
      partialArgs[offset] = arg;
      offset++;
    }
    return this;
  }

  @ExportMessage(library = LambdaLibrary.class)
  public JXPartialLambda cloneLambda() {
    JXPartialLambda res = new JXPartialLambda(this.callTarget, this.template);
    res.offset = this.offset;
    // values must be immutable
    if (offset >= 0) System.arraycopy(this.partialArgs, 0, res.partialArgs, 0, offset);
    return res;
  }

  @ExportMessage(library = LambdaLibrary.class)
  public boolean isLambda() {
    return true;
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean isBoolean(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library) {
    return library.isBoolean(execute(EMPTY_ARGS));
  }

  @ExportMessage
  public boolean asBoolean(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library)
      throws UnsupportedMessageException {
    return library.asBoolean(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean isString(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library) {
    return library.isString(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public String asString(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library)
      throws UnsupportedMessageException {
    return library.asString(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean isNumber(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library) {
    return library.isNumber(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean fitsInByte(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library) {
    return library.fitsInByte(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public byte asByte(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library)
      throws UnsupportedMessageException {
    return library.asByte(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean fitsInShort(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library) {
    return library.fitsInShort(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public short asShort(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library)
      throws UnsupportedMessageException {
    return library.asShort(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean fitsInInt(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library) {
    return library.fitsInInt(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public int asInt(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library)
      throws UnsupportedMessageException {
    return library.asInt(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean fitsInLong(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library) {
    return library.fitsInLong(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public long asLong(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library)
      throws UnsupportedMessageException {
    return library.asLong(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean fitsInDouble(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library) {
    return library.fitsInDouble(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public double asDouble(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library)
      throws UnsupportedMessageException {
    return library.asDouble(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean fitsInFloat(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library) {
    return library.fitsInFloat(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public float asFloat(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library)
      throws UnsupportedMessageException {
    return library.asFloat(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean fitsInBigInteger(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library) {
    return library.fitsInBigInteger(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public BigInteger asBigInteger(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library)
      throws UnsupportedMessageException {
    return library.asBigInteger(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean hasMembers(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library) {
    return library.hasMembers(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public Object getMembers(
      boolean includeInternal,
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library)
      throws UnsupportedMessageException {
    return library.getMembers(execute(EMPTY_ARGS), includeInternal);
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean isMemberReadable(
      String member,
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library) {
    return library.isMemberReadable(execute(EMPTY_ARGS), member);
  }

  @ExportMessage(library = InteropLibrary.class)
  public Object readMember(
      String member,
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library)
      throws UnsupportedMessageException, UnknownIdentifierException {
    return library.readMember(execute(EMPTY_ARGS), member);
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean isMemberModifiable(
      String member,
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library) {
    return library.isMemberModifiable(execute(EMPTY_ARGS), member);
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean isMemberInsertable(
      String member,
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library) {
    return library.isMemberInsertable(execute(EMPTY_ARGS), member);
  }

  @ExportMessage(library = InteropLibrary.class)
  public void writeMember(
      String member,
      Object value,
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library)
      throws UnsupportedMessageException, UnknownIdentifierException, UnsupportedTypeException {
    library.writeMember(execute(EMPTY_ARGS), member, value);
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean hasArrayElements(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library) {
    return library.hasArrayElements(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public long getArraySize(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library)
      throws UnsupportedMessageException {
    return library.getArraySize(execute(EMPTY_ARGS));
  }

  @ExportMessage(library = InteropLibrary.class)
  public Object readArrayElement(
      long index,
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library)
      throws InvalidArrayIndexException, UnsupportedMessageException {
    return library.readArrayElement(execute(EMPTY_ARGS), index);
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean isArrayElementReadable(
      long index,
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library) {
    return library.isArrayElementReadable(execute(EMPTY_ARGS), index);
  }

  private Object[] getArgs() {
    return this.partialArgs;
  }
}
