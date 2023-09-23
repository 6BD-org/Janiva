package com.oracle.truffle.jx.runtime.io;

import java.io.IOException;
import java.io.OutputStream;

import com.oracle.truffle.api.interop.*;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOUtils {

  private static final Logger logger = LoggerFactory.getLogger(IOUtils.class);

  // TODO: support cached library
  public static void writeJanivaObjectIntoStream(
          InteropLibrary library,
          OutputStream os,
          Object object
  )
          throws IOException, UnsupportedMessageException, UnsupportedTypeException, ArityException, InvalidArrayIndexException, UnknownIdentifierException {
    processValue(library, os, object);
  }

  private static void processValue(InteropLibrary library, OutputStream os, Object value) throws IOException, UnsupportedMessageException, UnsupportedTypeException, ArityException, InvalidArrayIndexException, UnknownIdentifierException {
    if (library.isExecutable(value)) {
      processValue(library, os, library.execute(value));
      return;
    }
    if (library.hasMembers(value)) {
      processObject(library, os, value);
      return;
    }
    if (library.hasArrayElements(value)) {
      processArray(library, os, value);
    }
    if (library.isBoolean(value)) {
      if (library.asBoolean(value)) {
        os.write("true".getBytes());
      } else {
        os.write("false".getBytes());
      }
    }
    if (library.isNumber(value)) {
      os.write(numberToString(library, value).getBytes());
    }
    if (library.isString(value)) {
      os.write(quote(library.asString(value)).getBytes());
    }
  }

  private static String numberToString(InteropLibrary library, Object object) throws UnsupportedMessageException {
    if (library.fitsInLong(object)) return Long.valueOf(library.asLong(object)).toString();
    if (library.fitsInDouble(object)) return Double.valueOf(library.asDouble(object)).toString();
    return "NAN";
  }

  private static void processObject(InteropLibrary library, OutputStream os, Object value) throws IOException, UnsupportedMessageException, UnsupportedTypeException, ArityException, InvalidArrayIndexException, UnknownIdentifierException {
    // apply executable first
    if (library.isExecutable(value)) {
      processObject(library, os, library.execute(value));
      return;
    }

    os.write("{\n".getBytes());
    var memberArray = library.getMembers(value);

    for (int i=0; i<library.getArraySize(memberArray); i++) {
      var memberKey = library.asString(library.readArrayElement(memberArray, i));
      os.write(quote(memberKey).getBytes());
      os.write(": ".getBytes());
      Object member = library.readMember(value, memberKey);
      processValue(library, os, member);
      if (i < library.getArraySize(memberArray) - 1) {
        os.write(", \n".getBytes());
      }
      i++;
    }
    os.write("\n}".getBytes());
  }



  private static void processArray(InteropLibrary library, OutputStream os, Object array) throws IOException, UnsupportedMessageException, InvalidArrayIndexException, UnsupportedTypeException, ArityException, UnknownIdentifierException {
    os.write("[ ".getBytes());
    long arrSize = library.getArraySize(array);
    for (int i = 0; i < arrSize; i++) {
      processValue(library, os, library.readArrayElement(array, i));
      if (i < arrSize - 1) {
        os.write(", ".getBytes());
      }
    }
    os.write(" ]".getBytes());
  }

  private static String quote(String token) {
    return "\"" + token + "\"";
  }
}
