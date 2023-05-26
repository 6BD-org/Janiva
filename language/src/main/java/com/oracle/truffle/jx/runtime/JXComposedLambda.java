package com.oracle.truffle.jx.runtime;

import com.oracle.truffle.api.interop.*;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;
import java.util.Arrays;
import com.oracle.truffle.jx.constants.CacheLimits;
import lombok.extern.slf4j.Slf4j;

@ExportLibrary(InteropLibrary.class)
@ExportLibrary(LambdaLibrary.class)
@Slf4j
public class JXComposedLambda implements TruffleObject {
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
      Object[] args, @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary) {
    // clone, merge args and
    this.members[0] = lambdaLibrary.mergeArgs(lambdaLibrary.cloneLambda(this.members[0]), args);
    return this;
  }

  @ExportMessage
  public Object execute(
      Object[] args,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) InteropLibrary library,
      @CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) LambdaLibrary lambdaLibrary)
      throws UnsupportedMessageException, UnsupportedTypeException, ArityException {
    Object res = library.execute(this.members[0], args);
    for (int i = 1; i < this.members.length; i++) {
      // clone next, merge args and execute
      res =
          library.execute(
              lambdaLibrary.mergeArgs(
                  lambdaLibrary.cloneLambda(this.members[i]), new Object[] {res}));
    }
    return res;
  }

  @ExportMessage(library = LambdaLibrary.class)
  public boolean isLambda() {
    return true;
  }

  @ExportMessage
  public boolean isExecutable(@CachedLibrary(limit = CacheLimits.LIMIT_LAMBDA_LIB) InteropLibrary library) {
    return library.isExecutable(members[0]);
  }
}
