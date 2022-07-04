/*
 * Copyright (c) 2012, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/*
 * The parser and lexer need to be generated using "mx create-sl-parser".
 */

grammar JSONXLang;

@parser::header
{
// DO NOT MODIFY - generated from SimpleLanguage.g4 using "mx create-sl-parser"

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.strings.TruffleString;

import com.oracle.truffle.jx.JSONXLang;
import com.oracle.truffle.jx.nodes.JXExpressionNode;
import com.oracle.truffle.jx.nodes.JXRootNode;
import com.oracle.truffle.jx.nodes.JXStatementNode;
import com.oracle.truffle.jx.parser.JXParseError;
import com.oracle.truffle.api.nodes.RootNode;

}

@lexer::header
{
// DO NOT MODIFY - generated from SimpleLanguage.g4 using "mx create-sl-parser"
}

@parser::members
{
private JXNodeFactory factory;
private Source source;

private static final class BailoutErrorListener extends BaseErrorListener {
    private final Source source;
    BailoutErrorListener(Source source) {
        this.source = source;
    }
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        throwParseError(source, line, charPositionInLine, (Token) offendingSymbol, msg);
    }
}

public void SemErr(Token token, String message) {
    assert token != null;
    throwParseError(source, token.getLine(), token.getCharPositionInLine(), token, message);
}

private static void throwParseError(Source source, int line, int charPositionInLine, Token token, String message) {
    int col = charPositionInLine + 1;
    String location = "-- line " + line + " col " + col + ": ";
    int length = token == null ? 1 : Math.max(token.getStopIndex() - token.getStartIndex(), 0);
    throw new JXParseError(source, line, col, length, String.format("Error(s) parsing script:%n" + location + message));
}

public static RootNode parseSL(JSONXLang language, Source source) {
    JSONXLangLexer lexer = new JSONXLangLexer(CharStreams.fromString(source.getCharacters().toString()));
    JSONXLangParser parser = new JSONXLangParser(new CommonTokenStream(lexer));
    lexer.removeErrorListeners();
    parser.removeErrorListeners();
    BailoutErrorListener listener = new BailoutErrorListener(source);
    lexer.addErrorListener(listener);
    parser.addErrorListener(listener);
    parser.factory = new JXNodeFactory(language, source);
    parser.source = source;
    parser.simplelanguage();
    return parser.factory.getRootNode();
}
}

// parser

simplelanguage
:
j_value
EOF
;

j_value returns [JXExpressionNode result]:
object                                  {factory.registerRootNode($object.result);}
|
primitive                               {factory.registerRootNode($primitive.result);}
;

object returns [JXExpressionNode result]
:
OBJECT_OPEN                             {factory.startObject(); List<JXStatementNode> body = new LinkedList();}
(
STRING_LITERAL                          {Token valName = $STRING_LITERAL;}
':'
j_value                                 {body.add(factory.bindVal(valName, $j_value.result));}
)
(
','
STRING_LITERAL                          {Token valName = $STRING_LITERAL;}
':'
j_value                                 {body.add(factory.bindVal(valName, $j_value.result));}
)*
OBJECT_CLOSE                            {factory.endObject(body);}
;


primitive returns [JXExpressionNode result]
:
j_number                                  {$result = $j_number.result;}
|
j_string                                  {$result = $j_string.result;}
|
j_boolean                                 {$result = $j_boolean.result;}
;

j_number returns [JXExpressionNode result]
:
NUMERIC_LITERAL                         {Token whole = $NUMERIC_LITERAL; Token dec = null;}
(
'.'NUMERIC_LITERAL                      {dec = $NUMERIC_LITERAL;}
)?                                      {$result = factory.createDecimal(whole, dec);}
;

j_string returns [JXExpressionNode result]
:
STRING_LITERAL                          {$result = factory.createStringLiteral($STRING_LITERAL, true);}
;

j_boolean returns [JXExpressionNode result]
:
BOOL_LITERAL                            {$result = factory.createBoolean($BOOL_LITERAL);}
;



// lexer

WS : [ \t\r\n\u000C]+ -> skip;
COMMENT : '/*' .*? '*/' -> skip;
LINE_COMMENT : '//' ~[\r\n]* -> skip;

fragment LETTER : [A-Z] | [a-z] | '_' | '$';
fragment NON_ZERO_DIGIT : [1-9];
fragment DIGIT : [0-9];
fragment HEX_DIGIT : [0-9] | [a-f] | [A-F];
fragment OCT_DIGIT : [0-7];
fragment BINARY_DIGIT : '0' | '1';
fragment TAB : '\t';
fragment STRING_CHAR : ~('"' | '\r' | '\n');
fragment TRUE : 'true';
fragment FALSE : 'false';

BOOL_LITERAL : TRUE | FALSE;
IDENTIFIER : LETTER (LETTER | DIGIT)*;
STRING_LITERAL : '"' STRING_CHAR* '"';
NUMERIC_LITERAL : '0' | NON_ZERO_DIGIT DIGIT*;
LIST_OPEN: '[';
LIST_CLOSE: ']';
OBJECT_OPEN: '{';
OBJECT_CLOSE: '}';