package com.oracle.truffle.jx.core;


import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.runtime.JXObject;

public class DefaultLangContext implements LangContext{
    private final DefaultLangContext parent;

    public DefaultLangContext(DefaultLangContext parent) {
        this.parent = parent;
    }

    @Override
    public LangContext getParent() {
        return parent;
    }

    @Override
    public <T> T getValue(TruffleString name) {
        return null;
    }

    @Override
    public <T> void bindValue(TruffleString name, T value) {

    }

    @Override
    public <T> T getAttribute(TruffleString key) {
        return null;
    }

    @Override
    public <T> void setAttribute(TruffleString key, T val) {

    }

    @Override
    public LangContext spawn() {
        return new DefaultLangContext(this);
    }

    @Override
    public DynamicObject materialize() {
        return new JXObject(Shape.newBuilder().layout(JXObject.class).build());
    }


}
