// Generated from language/src/main/java/com/oracle/truffle/sl/parser/SimpleLanguage.g4 by ANTLR 4.9.2
package com.oracle.truffle.sl.parser;

// DO NOT MODIFY - generated from SimpleLanguage.g4 using "mx create-sl-parser"

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.strings.TruffleString;

import com.oracle.truffle.sl.SLLanguage;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLRootNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;
import com.oracle.truffle.sl.parser.SLParseError;
import com.oracle.truffle.api.nodes.RootNode;


import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SimpleLanguageParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, WS=3, COMMENT=4, LINE_COMMENT=5, IDENTIFIER=6, STRING_LITERAL=7, 
		NUMERIC_LITERAL=8, BOOL_LITERAL=9;
	public static final int
		RULE_simplelanguage = 0, RULE_object = 1, RULE_primitive = 2, RULE_j_number = 3, 
		RULE_j_string = 4, RULE_j_boolean = 5;
	private static String[] makeRuleNames() {
		return new String[] {
			"simplelanguage", "object", "primitive", "j_number", "j_string", "j_boolean"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{}'", "'.'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, "WS", "COMMENT", "LINE_COMMENT", "IDENTIFIER", "STRING_LITERAL", 
			"NUMERIC_LITERAL", "BOOL_LITERAL"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "SimpleLanguage.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }


	private SLNodeFactory factory;
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
	    throw new SLParseError(source, line, col, length, String.format("Error(s) parsing script:%n" + location + message));
	}

	public static RootNode parseSL(SLLanguage language, Source source) {
	    SimpleLanguageLexer lexer = new SimpleLanguageLexer(CharStreams.fromString(source.getCharacters().toString()));
	    SimpleLanguageParser parser = new SimpleLanguageParser(new CommonTokenStream(lexer));
	    lexer.removeErrorListeners();
	    parser.removeErrorListeners();
	    BailoutErrorListener listener = new BailoutErrorListener(source);
	    lexer.addErrorListener(listener);
	    parser.addErrorListener(listener);
	    parser.factory = new SLNodeFactory(language, source);
	    parser.source = source;
	    parser.simplelanguage();
	    return parser.factory.getRootNode();
	}

	public SimpleLanguageParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class SimplelanguageContext extends ParserRuleContext {
		public ObjectContext object;
		public PrimitiveContext primitive;
		public ObjectContext object() {
			return getRuleContext(ObjectContext.class,0);
		}
		public PrimitiveContext primitive() {
			return getRuleContext(PrimitiveContext.class,0);
		}
		public TerminalNode EOF() { return getToken(SimpleLanguageParser.EOF, 0); }
		public SimplelanguageContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simplelanguage; }
	}

	public final SimplelanguageContext simplelanguage() throws RecognitionException {
		SimplelanguageContext _localctx = new SimplelanguageContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_simplelanguage);
		try {
			setState(19);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				enterOuterAlt(_localctx, 1);
				{
				setState(12);
				((SimplelanguageContext)_localctx).object = object();
				factory.registerRootNode(((SimplelanguageContext)_localctx).object.result);
				}
				break;
			case STRING_LITERAL:
			case NUMERIC_LITERAL:
			case BOOL_LITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(15);
				((SimplelanguageContext)_localctx).primitive = primitive();
				factory.registerRootNode(((SimplelanguageContext)_localctx).primitive.result);
				setState(17);
				match(EOF);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ObjectContext extends ParserRuleContext {
		public SLExpressionNode result;
		public ObjectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_object; }
	}

	public final ObjectContext object() throws RecognitionException {
		ObjectContext _localctx = new ObjectContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_object);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(21);
			match(T__0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PrimitiveContext extends ParserRuleContext {
		public SLExpressionNode result;
		public J_numberContext j_number;
		public J_stringContext j_string;
		public J_booleanContext j_boolean;
		public J_numberContext j_number() {
			return getRuleContext(J_numberContext.class,0);
		}
		public J_stringContext j_string() {
			return getRuleContext(J_stringContext.class,0);
		}
		public J_booleanContext j_boolean() {
			return getRuleContext(J_booleanContext.class,0);
		}
		public PrimitiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primitive; }
	}

	public final PrimitiveContext primitive() throws RecognitionException {
		PrimitiveContext _localctx = new PrimitiveContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_primitive);
		try {
			setState(32);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NUMERIC_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(23);
				((PrimitiveContext)_localctx).j_number = j_number();
				((PrimitiveContext)_localctx).result =  ((PrimitiveContext)_localctx).j_number.result;
				}
				break;
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(26);
				((PrimitiveContext)_localctx).j_string = j_string();
				((PrimitiveContext)_localctx).result =  ((PrimitiveContext)_localctx).j_string.result;
				}
				break;
			case BOOL_LITERAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(29);
				((PrimitiveContext)_localctx).j_boolean = j_boolean();
				((PrimitiveContext)_localctx).result =  ((PrimitiveContext)_localctx).j_boolean.result;
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class J_numberContext extends ParserRuleContext {
		public SLExpressionNode result;
		public Token NUMERIC_LITERAL;
		public List<TerminalNode> NUMERIC_LITERAL() { return getTokens(SimpleLanguageParser.NUMERIC_LITERAL); }
		public TerminalNode NUMERIC_LITERAL(int i) {
			return getToken(SimpleLanguageParser.NUMERIC_LITERAL, i);
		}
		public J_numberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_j_number; }
	}

	public final J_numberContext j_number() throws RecognitionException {
		J_numberContext _localctx = new J_numberContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_j_number);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(34);
			((J_numberContext)_localctx).NUMERIC_LITERAL = match(NUMERIC_LITERAL);
			Token whole = ((J_numberContext)_localctx).NUMERIC_LITERAL; Token dec = null;
			setState(39);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__1) {
				{
				setState(36);
				match(T__1);
				setState(37);
				((J_numberContext)_localctx).NUMERIC_LITERAL = match(NUMERIC_LITERAL);
				dec = ((J_numberContext)_localctx).NUMERIC_LITERAL;
				}
			}

			((J_numberContext)_localctx).result =  factory.createDecimal(whole, dec);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class J_stringContext extends ParserRuleContext {
		public SLExpressionNode result;
		public Token STRING_LITERAL;
		public TerminalNode STRING_LITERAL() { return getToken(SimpleLanguageParser.STRING_LITERAL, 0); }
		public J_stringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_j_string; }
	}

	public final J_stringContext j_string() throws RecognitionException {
		J_stringContext _localctx = new J_stringContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_j_string);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(43);
			((J_stringContext)_localctx).STRING_LITERAL = match(STRING_LITERAL);
			((J_stringContext)_localctx).result =  factory.createStringLiteral(((J_stringContext)_localctx).STRING_LITERAL, true);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class J_booleanContext extends ParserRuleContext {
		public SLExpressionNode result;
		public Token BOOL_LITERAL;
		public TerminalNode BOOL_LITERAL() { return getToken(SimpleLanguageParser.BOOL_LITERAL, 0); }
		public J_booleanContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_j_boolean; }
	}

	public final J_booleanContext j_boolean() throws RecognitionException {
		J_booleanContext _localctx = new J_booleanContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_j_boolean);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(46);
			((J_booleanContext)_localctx).BOOL_LITERAL = match(BOOL_LITERAL);
			((J_booleanContext)_localctx).result =  factory.createBoolean(((J_booleanContext)_localctx).BOOL_LITERAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\13\64\4\2\t\2\4\3"+
		"\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\3\2\3\2\3\2\3\2\3\2\3\2\3\2\5\2\26"+
		"\n\2\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4#\n\4\3\5\3\5\3\5"+
		"\3\5\3\5\5\5*\n\5\3\5\3\5\3\6\3\6\3\6\3\7\3\7\3\7\3\7\2\2\b\2\4\6\b\n"+
		"\f\2\2\2\61\2\25\3\2\2\2\4\27\3\2\2\2\6\"\3\2\2\2\b$\3\2\2\2\n-\3\2\2"+
		"\2\f\60\3\2\2\2\16\17\5\4\3\2\17\20\b\2\1\2\20\26\3\2\2\2\21\22\5\6\4"+
		"\2\22\23\b\2\1\2\23\24\7\2\2\3\24\26\3\2\2\2\25\16\3\2\2\2\25\21\3\2\2"+
		"\2\26\3\3\2\2\2\27\30\7\3\2\2\30\5\3\2\2\2\31\32\5\b\5\2\32\33\b\4\1\2"+
		"\33#\3\2\2\2\34\35\5\n\6\2\35\36\b\4\1\2\36#\3\2\2\2\37 \5\f\7\2 !\b\4"+
		"\1\2!#\3\2\2\2\"\31\3\2\2\2\"\34\3\2\2\2\"\37\3\2\2\2#\7\3\2\2\2$%\7\n"+
		"\2\2%)\b\5\1\2&\'\7\4\2\2\'(\7\n\2\2(*\b\5\1\2)&\3\2\2\2)*\3\2\2\2*+\3"+
		"\2\2\2+,\b\5\1\2,\t\3\2\2\2-.\7\t\2\2./\b\6\1\2/\13\3\2\2\2\60\61\7\13"+
		"\2\2\61\62\b\7\1\2\62\r\3\2\2\2\5\25\")";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}