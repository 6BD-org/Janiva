package com.oracle.truffle.jx.runtime;

import com.oracle.truffle.api.interop.*;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

@ExportLibrary(InteropLibrary.class)
@ExportLibrary(LambdaLibrary.class)
@Slf4j
public class JXComposedLambda extends DynamicObject implements TruffleObject {
  private static final Shape SHAPE = Shape.newBuilder().layout(JXComposedLambda.class).build();

  private static final String PROP_MEMBERS = "members";
  private static final String LIB_CACHE = "2";

  @DynamicField private Object _o1;

  private final Object[] members;

  public JXComposedLambda(Object[] composedLambdas) {
    super(SHAPE);
    this.members = composedLambdas;
  }

  @ExportMessage(library = LambdaLibrary.class)
  public JXComposedLambda cloneLambda(
      @CachedLibrary(limit = LIB_CACHE) LambdaLibrary lambdaLibrary) {
    return new JXComposedLambda(
        Arrays.stream(members)
            .peek(m -> log.debug("composing:{}", m.getClass()))
            .map(lambdaLibrary::cloneLambda)
            .toArray(Object[]::new));
  }

  @ExportMessage(library = LambdaLibrary.class)
  public JXComposedLambda mergeArgs(
      Object[] args, @CachedLibrary(limit = LIB_CACHE) LambdaLibrary lambdaLibrary) {
    // clone, merge args and
    this.members[0] = lambdaLibrary.mergeArgs(lambdaLibrary.cloneLambda(this.members[0]), args);
    return this;
  }

  @ExportMessage
  public Object execute(
      Object[] args,
      @CachedLibrary(limit = LIB_CACHE) InteropLibrary library,
      @CachedLibrary(limit = LIB_CACHE) LambdaLibrary lambdaLibrary)
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
  public boolean isExecutable(@CachedLibrary(limit = LIB_CACHE) InteropLibrary library) {
    return library.isExecutable(members[0]);
  }
}
