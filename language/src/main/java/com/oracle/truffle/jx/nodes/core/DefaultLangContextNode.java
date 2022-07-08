package com.oracle.truffle.jx.nodes.core;


import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.jx.builtins.JXNewObjectBuiltin;
import com.oracle.truffle.jx.builtins.JXNewObjectBuiltinFactory;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.JXStatementNode;
import com.oracle.truffle.jx.runtime.JXObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultLangContextNode extends JXExpressionNode implements LangContext{
    private final DefaultLangContextNode parent;
    private DynamicObject dynamicObject;
    private List<JXStatementNode> statementNodes = new ArrayList<>();
    private JXNewObjectBuiltin newObjectBuiltin;
    private Map<String, Object> attrMap = new HashMap<>();

    public DefaultLangContextNode(DefaultLangContextNode parent, JXNewObjectBuiltin newObjectNode) {
        this.parent = parent;
        this.newObjectBuiltin = newObjectNode;
    }

    @Override
    public LangContext getParentContext() {
        return parent;
    }

    @Override
    public <T> T getValue(String name) {
        return null;
    }

    @Override
    public <T> void bindValue(String name, T value) {

    }

    @Override
    public <T> T getAttribute(String key) {
        return (T) attrMap.get(key);
    }

    @Override
    public <T> void setAttribute(String key, T val) {
        attrMap.put(key, val);
    }

    @Override
    public void setStatements(List<JXStatementNode> statements) {
        this.statementNodes = statements;
    }

    @Override
    public DefaultLangContextNode spawn() {
        return new DefaultLangContextNode(this, JXNewObjectBuiltinFactory.getInstance().createNode());
    }

    public DynamicObject materialize(VirtualFrame frame) {
        JXObject res = (JXObject) newObjectBuiltin.executeGeneric(frame);
        DynamicObjectLibrary library = DynamicObjectLibrary.getUncached();
        attrMap.forEach((k, v) -> library.put(res, k, v));
        return res;
    }


    @Override
    public Object executeGeneric(VirtualFrame frame) {
        statementNodes.forEach(n -> n.executeVoid(frame));
        return materialize(frame);
    }
}
