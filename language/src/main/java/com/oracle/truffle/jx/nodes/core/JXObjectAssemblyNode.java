package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.jx.builtins.JXNewObjectBuiltin;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.JXStatementNode;
import com.oracle.truffle.jx.nodes.core.JXAttributeBindingNode;
import com.oracle.truffle.jx.runtime.JXObject;

import java.util.Arrays;
import java.util.List;


public class JXObjectAssemblyNode extends JXExpressionNode {
    private final List<JXStatementNode> bindings;
    private final List<JXValueAccessNode> accessors;
    private final JXNewObjectBuiltin newObjectBuiltin;

    public JXObjectAssemblyNode(List<JXStatementNode> bindings, List<JXValueAccessNode> accessors, JXNewObjectBuiltin newObjectBuiltin) {
        this.accessors = accessors;
        this.newObjectBuiltin = newObjectBuiltin;
        this.bindings = bindings;
    }


    @Override
    public Object executeGeneric(VirtualFrame frame) {
        DynamicObjectLibrary dynamicObjectLibrary = DynamicObjectLibrary.getFactory().getUncached();
        JXObject jxObject = (JXObject) newObjectBuiltin.executeGeneric(frame);
        for(JXStatementNode bindingNode : bindings) {
            bindingNode.executeVoid(frame);
        }
        for (JXValueAccessNode n : accessors) {
            dynamicObjectLibrary.put(jxObject, n.getName(), n.executeGeneric(frame));
        }
        return jxObject;
    }
}
