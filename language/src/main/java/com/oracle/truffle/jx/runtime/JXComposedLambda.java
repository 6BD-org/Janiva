package com.oracle.truffle.jx.runtime;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.interop.*;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.jx.JXException;
import com.oracle.truffle.jx.constants.CacheLimits;
import com.oracle.truffle.jx.constants.Constants;
import java.math.BigInteger;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

@ExportLibrary(InteropLibrary.class)
@ExportLibrary(LambdaLibrary.class)
@Slf4j
public class JXComposedLambda implements TruffleObject {
  private static final String DELEGATE_LIB_CACHE = CacheLimits.DELEGATE_LIB_CACHE;
  private Object outputCache = JanivaVoid.VOID;
  private static final Object[] EMPTY_ARGS = Constants.EMPTY_ARGS;

  private final Object[] members;

  public JXComposedLambda(Object[] composedLambdas) {
    this.members = composedLambdas;
  }

  @ExportMessage(library = LambdaLibrary.class)
  public JXComposedLambda cloneLambda(
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    return new JXComposedLambda(
        Arrays.stream(members)
            .peek(m -> log.debug("composing:{}", m.getClass()))
            .map(lambdaLibrary::cloneLambda)
            .toArray(Object[]::new));
  }

  @ExportMessage(library = LambdaLibrary.class)
  public JXComposedLambda mergeArgs(
      Object[] args,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    // clone, merge args and
    this.members[0] = lambdaLibrary.mergeArgs(lambdaLibrary.cloneLambda(this.members[0]), args);
    return this;
  }

  @ExportMessage
  public Object execute(
      Object[] args,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    try {
      if (this.outputCache != JanivaVoid.VOID) {
        return this.outputCache;
      }
      Object res = library.execute(this.members[0], args);
      for (int i = 1; i < this.members.length; i++) {
        // clone next, merge args and execute
        res =
            library.execute(
                lambdaLibrary.mergeArgs(
                    lambdaLibrary.cloneLambda(this.members[i]), new Object[] {res}));
      }
      outputCache = res;
      return outputCache;
    } catch (ArityException | UnsupportedTypeException | UnsupportedMessageException e) {
      throw new JXException(e.getMessage());
    }
  }

  @ExportMessage(library = LambdaLibrary.class)
  public boolean isLambda() {
    return true;
  }

  @ExportMessage
  public boolean isExecutable(
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) InteropLibrary library) {
    return library.isExecutable(members[0]);
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean isBoolean(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    return library.isBoolean(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage
  public boolean asBoolean(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary)
      throws UnsupportedMessageException {
    return library.asBoolean(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean isString(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    return library.isString(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public String asString(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary)
      throws UnsupportedMessageException {
    return library.asString(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean isNumber(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    return library.isNumber(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean fitsInByte(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    return library.fitsInByte(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public byte asByte(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary)
      throws UnsupportedMessageException {
    return library.asByte(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean fitsInShort(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    return library.fitsInShort(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public short asShort(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary)
      throws UnsupportedMessageException {
    return library.asShort(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean fitsInInt(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    return library.fitsInInt(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public int asInt(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary)
      throws UnsupportedMessageException {
    return library.asInt(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean fitsInLong(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    return library.fitsInLong(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public long asLong(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary)
      throws UnsupportedMessageException {
    return library.asLong(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean fitsInDouble(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    return library.fitsInDouble(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public double asDouble(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary)
      throws UnsupportedMessageException {
    return library.asDouble(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean fitsInFloat(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    return library.fitsInFloat(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public float asFloat(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary)
      throws UnsupportedMessageException {
    return library.asFloat(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean fitsInBigInteger(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    return library.fitsInBigInteger(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public BigInteger asBigInteger(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary)
      throws UnsupportedMessageException {
    return library.asBigInteger(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean hasMembers(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    return library.hasMembers(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public Object getMembers(
      boolean includeInternal,
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary)
      throws UnsupportedMessageException {
    return library.getMembers(execute(EMPTY_ARGS, library, lambdaLibrary), includeInternal);
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean isMemberReadable(
      String member,
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    return library.isMemberReadable(execute(EMPTY_ARGS, library, lambdaLibrary), member);
  }

  @ExportMessage(library = InteropLibrary.class)
  public Object readMember(
      String member,
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary)
      throws UnsupportedMessageException, UnknownIdentifierException {
    return library.readMember(execute(EMPTY_ARGS, library, lambdaLibrary), member);
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean isMemberModifiable(
      String member,
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    return library.isMemberModifiable(execute(EMPTY_ARGS, library, lambdaLibrary), member);
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean isMemberInsertable(
      String member,
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    return library.isMemberInsertable(execute(EMPTY_ARGS, library, lambdaLibrary), member);
  }

  @ExportMessage(library = InteropLibrary.class)
  public void writeMember(
      String member,
      Object value,
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary)
      throws UnsupportedMessageException, UnknownIdentifierException, UnsupportedTypeException {
    library.writeMember(execute(EMPTY_ARGS, library, lambdaLibrary), member, value);
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean hasArrayElements(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    return library.hasArrayElements(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public long getArraySize(
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary)
      throws UnsupportedMessageException {
    return library.getArraySize(execute(EMPTY_ARGS, library, lambdaLibrary));
  }

  @ExportMessage(library = InteropLibrary.class)
  public Object readArrayElement(
      long index,
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary)
      throws InvalidArrayIndexException, UnsupportedMessageException {
    return library.readArrayElement(execute(EMPTY_ARGS, library, lambdaLibrary), index);
  }

  @ExportMessage(library = InteropLibrary.class)
  public boolean isArrayElementReadable(
      long index,
      @Cached.Shared("s") @CachedLibrary(limit = DELEGATE_LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    return library.isArrayElementReadable(execute(EMPTY_ARGS, library, lambdaLibrary), index);
  }
}
