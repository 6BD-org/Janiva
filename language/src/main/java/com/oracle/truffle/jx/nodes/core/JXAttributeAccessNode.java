package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.JXException;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.util.SLToMemberNode;
import com.oracle.truffle.jx.runtime.JXArray;
import com.oracle.truffle.jx.runtime.JXBigNumber;
import com.oracle.truffle.jx.runtime.JXObject;
import com.oracle.truffle.jx.runtime.exceptions.JXRuntimeException;
import com.oracle.truffle.jx.runtime.view.AbstractArrayView;
import org.graalvm.nativebridge.In;

@NodeChild("val")
@NodeChild("attr")
public abstract class JXAttributeAccessNode extends JXExpressionNode {

  /**
   * Access a member in dynamic object
   *
   * @param val JXObject
   * @param attr TruffleString indicating attribute key
   * @return
   */
  @Specialization(guards = "objectMember(val, attr)", limit = "3")
  public Object executeObject(
      Object val,
      Object attr,
      @Cached SLToMemberNode n) {
    InteropLibrary library = InteropLibrary.getUncached(); // don't know why cached library doesn't work
    try {
      return library.readMember((JXObject) val, n.execute(attr));
    } catch (UnsupportedMessageException | UnknownIdentifierException e) {
      throw new JXException("error reading object attribute " + e.getMessage(), this);
    }
  }

  /**
   * Access an element in array
   *
   * @param val JXArray value
   * @param attr JXBigNumber with int value
   * @return
   */
  @Specialization(guards = "arrayElem(val, attr)", limit = "3")
  public Object executeArray(
      Object val,
      Object attr) {
    InteropLibrary library = InteropLibrary.getUncached();
    try {
      return library.readArrayElement(val, library.asLong(attr));
    } catch (UnsupportedMessageException | InvalidArrayIndexException e) {
      throw new JXException("error reading array element " + e.getMessage(), this);
    }
  }

  boolean objectMember(Object o, Object attr) {
    return o instanceof JXObject && attr instanceof TruffleString;
  }

  boolean arrayElem(Object o, Object attr) {
    return (o instanceof JXArray || o instanceof AbstractArrayView) && attr instanceof JXBigNumber;
  }
}
