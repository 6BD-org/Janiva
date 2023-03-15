package com.oracle.truffle.jx.parser.exceptions;

import com.oracle.truffle.api.exception.AbstractTruffleException;

public class JXSyntaxError extends AbstractTruffleException {

    public JXSyntaxError() {
        super();
    }

    public JXSyntaxError(String message) {
        super(message);
    }
}
