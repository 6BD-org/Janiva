package com.oracle.truffle.jx.nodes.core;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.JXException;
import com.oracle.truffle.jx.JanivaLang;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.runtime.io.JXExported;
import com.oracle.truffle.jx.runtime.io.SourceEvaluator;
import com.xmbsmdsj.janiva.SourceFinder;
import com.oracle.truffle.api.source.Source;

import java.io.IOException;

public class JXImportNode extends JXExpressionNode {

    private final JXExpressionNode child;
    private final Source fromSource;
    public JXImportNode(JXExpressionNode importPath, Source fromSource) {
        this.child = importPath;
        this.fromSource = fromSource;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        try {
            TruffleString importPath = (TruffleString) this.child.executeGeneric(frame);
            org.graalvm.polyglot.Source s = SourceFinder.findImported(this, fromSource.getPath(), importPath);
            JXExported imported = SourceEvaluator.eval(s, JXExported.class);
            return imported.getValue();
        } catch (ClassCastException e) {
            throw new JXException("import path must be string", this);
        } catch (IOException ioe) {
            throw new JXException("Source not found", this);
        }

    }
}
