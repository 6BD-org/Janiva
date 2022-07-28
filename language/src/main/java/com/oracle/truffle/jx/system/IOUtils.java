package com.oracle.truffle.jx.system;

import org.graalvm.polyglot.Value;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class IOUtils {
    public static void writeJSONXObjectIntoStream(OutputStream os, Object object) {
        try {
            if (object instanceof Value) {
                processValue(os, (Value) object);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void processValue(OutputStream os, Value value) throws IOException {
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
        }
    }

    private static String numberToString(Value object) {
        return "";
    }

    private static void processObject(OutputStream os, Value object) {

    }

    private static void processArray(OutputStream os, Value array) {

    }
}
