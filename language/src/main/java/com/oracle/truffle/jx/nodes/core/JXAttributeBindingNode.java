package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.jx.nodes.JXStatementNode;
import org.antlr.v4.runtime.Token;

public class JXAttributeBindingNode extends JXStatementNode {

    private final String valName;
    private final Object val;

    private final LangContext context;

    public JXAttributeBindingNode(Token valName, Object val, LangContext context) {
        this.valName = valName.getText();
        this.val = val;
        this.context = context;
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        context.setAttribute(valName, val);

    }
}
