package com.oracle.truffle.jx.parser.lambda;

import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.parser.JXParseError;
import com.oracle.truffle.jx.parser.exceptions.JXSyntaxError;

import java.util.HashMap;
import java.util.Map;

public class LambdaRegistry {

    private Map<TruffleString, LambdaTemplate> registrations = new HashMap<>();

    public LambdaTemplate lookupLambdaBody(TruffleString lambdaName) {
        return registrations.get(lambdaName);
    }

    public void register(TruffleString name, LambdaTemplate template) {
        if (registrations.containsKey(name)) {
            throw new JXSyntaxError("lambda overloading is not supported");
        }
        registrations.put(name, template);
    }
}
