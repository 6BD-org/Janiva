package com.oracle.truffle.jx.runtime.io;

import java.io.IOException;
import java.io.OutputStream;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOUtils {

  private static final Logger logger = LoggerFactory.getLogger(IOUtils.class);

  public static void writeJanivaObjectIntoStream(OutputStream os, Object object)
      throws IOException {
    processValue(os, Value.asValue(object));
  }

  private static void processValue(OutputStream os, Value value) throws IOException {
    if (value.canExecute()) {
      processValue(os, value.execute());
      return;
    }
    if (value.hasMembers()) {
      processObject(os, value);
      return;
    }
    if (value.hasArrayElements()) {
      processArray(os, value);
    }
    if (value.isBoolean()) {
      if (value.asBoolean()) {
        os.write("true".getBytes());
      } else {
        os.write("false".getBytes());
      }
    }
    if (value.isNumber()) {
      os.write(numberToString(value).getBytes());
    }
    if (value.isString()) {
      os.write(quote(value.asString()).getBytes());
    }
  }

  private static String numberToString(Value object) {
    if (object.fitsInLong()) return Long.valueOf(object.asLong()).toString();
    if (object.fitsInDouble()) return Double.valueOf(object.asDouble()).toString();
    return "NAN";
  }

  private static void processObject(OutputStream os, Value object) throws IOException {
    // apply executable first
    if (object.canExecute()) {
      processObject(os, object.execute());
      return;
    }

    os.write("{\n".getBytes());
    int i = 0;
    for (String memberKey : object.getMemberKeys()) {
      os.write(quote(memberKey).getBytes());
      os.write(": ".getBytes());
      Value member = object.getMember(memberKey);
      processValue(os, member);
      if (i < object.getMemberKeys().size() - 1) {
        os.write(", \n".getBytes());
      }
      i++;
    }
    os.write("\n}".getBytes());
  }

  private static void processArray(OutputStream os, Value array) throws IOException {
    os.write("[ ".getBytes());
    for (int i = 0; i < array.getArraySize(); i++) {
      processValue(os, array.getArrayElement(i));
      if (i < array.getArraySize() - 1) {
        os.write(", ".getBytes());
      }
    }
    os.write(" ]".getBytes());
  }

  private static String quote(String token) {
    return "\"" + token + "\"";
  }
}
