package com.oracle.truffle.jx.parser.lambda;

import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.JSONXLang;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.core.JXAttributeBindingNode;
import com.oracle.truffle.jx.nodes.core.JXLambdaNode;
import com.oracle.truffle.jx.parser.MetaStack;
import com.oracle.truffle.jx.parser.exceptions.JXSyntaxError;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LambdaTemplate {
    private List<TruffleString> parameterNames;
    private TruffleString name;
    private JXExpressionNode body;
    private Map<TruffleString, Integer> paramSlotMapping = new HashMap();

    public LambdaTemplate(TruffleString name) {
        this.parameterNames = new ArrayList<>();
        this.name = name;
    }


    public void addFormalParam(TruffleString paramName) {
        this.parameterNames.add(paramName);
    }

    public void finish(LambdaRegistry registry) {
        registry.register(this.name, this);
    }

    public void addBody(JXExpressionNode body) {
        this.body = body;
    }

    public JXLambdaNode materialize(
            List<JXExpressionNode> parameters,
            MetaStack metaStack,
            JSONXLang lang
    ) {
        metaStack.startLambda();
        if (parameters.size() != this.parameterNames.size()) {
            throw new JXSyntaxError("Parameter list does not match. Expected lenght: " + this.parameterNames.size() + " getting: " + parameters.size());
        }
        List<JXAttributeBindingNode> parameterBindings = new ArrayList<>();
        for (int i=0; i<parameters.size(); i++) {
            JXExpressionNode param = parameters.get(i);
            int slot = metaStack.requestForSlot(this.parameterNames.get(i), param);
            parameterBindings.add(new JXAttributeBindingNode(slot, param, true));
            paramSlotMapping.put(parameterNames.get(i), slot);
        }
        // push to stack
        return new JXLambdaNode(lang, metaStack.buildTop(), parameterBindings, this.body);
    }

    public int lookupParamSlot(TruffleString parameterName) {
        return this.paramSlotMapping.get(parameterName);
    }
}
