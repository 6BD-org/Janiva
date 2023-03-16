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
import com.oracle.truffle.jx.nodes.core.JXLambdaNode;
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
lambda_def*
bind_latent[false]?
(
  stream=IDENTIFIER
  STREAM_ACCEPTS                        {factory.setRootStream($stream);}
)?
j_root_value
EOF
;

bind_latent [boolean isFunc] returns [JXStatementNode result]
:
(
    IDENTIFIER                   {Token valName = $IDENTIFIER;}
    STREAM_ACCEPTS
    j_value[false]                      {$result = factory.bindLatent(valName, $j_value.result, $isFunc);}
)
;

j_root_value:
j_value[false]                                  {factory.registerRootNode($j_value.result);}
;

j_list returns [JXExpressionNode result]
:
LIST_OPEN                                {factory.startArray();}
j_value[false]                                  {factory.appendArray($j_value.result);}
(
','
j_value[false]                                  {factory.appendArray($j_value.result);}
)*
LIST_CLOSE                               {$result=factory.closeArray();}
;

object [boolean isFunc] returns [JXExpressionNode result]
:
OBJECT_OPEN                             {factory.startObject(); List<JXStatementNode> body = new LinkedList();}
(
(
bind_latent[isFunc]                             {body.add($bind_latent.result);}
','
)*
STRING_LITERAL                          {Token valName = $STRING_LITERAL;}
':'
j_value[$isFunc]                                 {body.add(factory.bindVal(valName, $j_value.result));}
','?
)
(
','
(
bind_latent[isFunc]                             {body.add($bind_latent.result);}
','
)*
STRING_LITERAL                          {Token valName = $STRING_LITERAL;}
':'
j_value[$isFunc]                                 {body.add(factory.bindVal(valName, $j_value.result));}
','?
)*
OBJECT_CLOSE                            {$result = factory.endObject(body);}
;


j_value [boolean isFunc] returns [JXExpressionNode result]:
ref_attribute[$isFunc]                           {$result=$ref_attribute.result;}
|
primitive                               {$result=$primitive.result;}
|
object[$isFunc]                                  {$result=$object.result;}
|
j_list                                  {$result=$j_list.result;}
|
arithmatics[$isFunc]                             {$result=$arithmatics.result;}
|
lambda_invocation                       {$result=$lambda_invocation.result;}
;

// refer to bounded attribute
ref_attribute[boolean isFunc] returns [JXExpressionNode result]
:
REF_ATTR
attr=IDENTIFIER                     {$result = factory.referAttribute($attr, $isFunc);}
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

arithmatics[boolean isFunc] returns [JXExpressionNode result]
:
term[$isFunc]                                    {$result = $term.result;}
(
    op=L1_OP
    term[$isFunc]                                {$result = factory.createBinary($op, $result, $term.result);}
)*
;

term[boolean isFunc] returns [JXExpressionNode result]
:
factor[$isFunc]                                  {$result = $factor.result;}
(
    op=L2_OP
    factor[$isFunc]                              {$result = factory.createBinary($op, $result, $factor.result);}
)*
;

factor[boolean isFunc] returns [JXExpressionNode result]:
j_string                                {$result = $j_string.result;}
|
j_number                                {$result = $j_number.result;}
|
ref_attribute[isFunc]                           {$result = $ref_attribute.result;}
|
BRACKET_OPEN
arithmatics[$isFunc]                             {$result = $arithmatics.result;}
BRACKET_CLOSE
;


arg_list
:
( // NO ARG
BRACKET_OPEN
BRACKET_CLOSE
)
|
(
BRACKET_OPEN
arg0=IDENTIFIER                         {factory.addFormalParameter($arg0);}
    (
    ','
    argN=IDENTIFIER                     {factory.addFormalParameter($argN);}
    )?
BRACKET_CLOSE
)
;

lambda_def
:
REF_LAMBDA
funcName=IDENTIFIER                     {factory.defLambda($funcName);}
LAMBDA_DEF_INTRO
arg_list
STREAM_PRODUCE
body=j_value[true]                      {factory.addBody($body.result);}
END                                     {factory.finishDefLambda();}
;

lambda_invocation returns [JXExpressionNode result]
:
REF_LAMBDA                              {List<JXExpressionNode> args = new ArrayList<>();}
lambdaName=IDENTIFIER
(
STREAM_ACCEPTS
arg=j_value[false]                          {args.add($arg.result);}
)*                                      {$result = factory.materialize($lambdaName, args);}
;



// lexer

WS : [ \t\r\n\u000C]+ -> skip;
COMMENT : '/*' .*? '*/' -> skip;
LINE_COMMENT : '//' ~[\r\n]* -> skip;

fragment LETTER : [A-Z] | [a-z] | '_';
fragment NON_ZERO_DIGIT : [1-9];
fragment DIGIT : [0-9];
fragment HEX_DIGIT : [0-9] | [a-f] | [A-F];
fragment OCT_DIGIT : [0-7];
fragment BINARY_DIGIT : '0' | '1';
fragment TAB : '\t';
fragment STRING_CHAR : ~('"' | '\r' | '\n');
fragment TRUE : 'true';
fragment FALSE : 'false';
fragment REF : '$';

L1_OP : ('+'|'-');
L2_OP : ('*'|'/');

BOOL_LITERAL : TRUE | FALSE;
IDENTIFIER : LETTER (LETTER | DIGIT)*;
STRING_LITERAL : '"' STRING_CHAR* '"';
NUMERIC_LITERAL : '0' | NON_ZERO_DIGIT DIGIT*;
LIST_OPEN: '[';
LIST_CLOSE: ']';
OBJECT_OPEN: '{';
OBJECT_CLOSE: '}';
BRACKET_OPEN: '(';
BRACKET_CLOSE: ')';
STREAM_ACCEPTS: '<<';
STREAM_PRODUCE: '>>';
REF_ATTR : REF;
REF_LAMBDA: '@';
LAMBDA_DEF_INTRO: '::';

END: '#';