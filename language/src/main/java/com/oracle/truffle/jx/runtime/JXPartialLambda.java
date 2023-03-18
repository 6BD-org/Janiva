package com.oracle.truffle.jx.runtime;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.*;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.jx.JXException;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.core.JXAttributeBindingNode;
import com.oracle.truffle.jx.nodes.core.JXLambdaExecutor;
import com.oracle.truffle.jx.nodes.core.JXLambdaNode;
import com.oracle.truffle.jx.parser.JXParseError;
import com.oracle.truffle.jx.parser.MetaStack;
import com.oracle.truffle.jx.parser.exceptions.JXSyntaxError;
import com.oracle.truffle.jx.parser.lambda.LambdaTemplate;
import com.oracle.truffle.jx.runtime.exceptions.JXRuntimeException;

import java.util.ArrayList;
import java.util.List;


@ExportLibrary(InteropLibrary.class)
public class JXPartialLambda implements TruffleObject {

    private CallTarget callTarget;

    public JXPartialLambda(CallTarget callTarget) {
        this.callTarget = callTarget;
    }


    @ExportMessage
    public boolean isExecutable() {
        return true;
    }

    @ExportMessage
    public Object execute(Object[] args) {
        return callTarget.call(args);
    }


}
