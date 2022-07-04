package com.oracle.truffle.jx.nodes.core;


import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.JXStatementNode;
import com.oracle.truffle.jx.runtime.JXObject;

import java.util.ArrayList;
import java.util.List;

public class DefaultLangContextNode extends JXExpressionNode implements LangContext{
    private final DefaultLangContextNode parent;
    private DynamicObject dynamicObject;
    private List<JXStatementNode> statementNodes = new ArrayList<>();

    public DefaultLangContextNode(DefaultLangContextNode parent) {
        this.parent = parent;
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
        return null;
    }

    @Override
    public <T> void setAttribute(String key, T val) {

    }

    @Override
    public void setStatements(List<JXStatementNode> statements) {
        this.statementNodes = statements;
    }

    @Override
    public DefaultLangContextNode spawn() {
        return new DefaultLangContextNode(this);
    }

    @Override
    public DynamicObject materialize() {
        JXObject res = new JXObject(Shape.newBuilder().layout(JXObject.class).build());
        return res;
    }


    @Override
    public Object executeGeneric(VirtualFrame frame) {
        statementNodes.forEach(n -> {n.executeVoid(frame);});
        return materialize();
    }
}
