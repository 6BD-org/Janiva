// Generated from language/src/main/java/com/oracle/truffle/sl/parser/SimpleLanguage.g4 by ANTLR 4.9.2
package com.oracle.truffle.sl.parser;

// DO NOT MODIFY - generated from SimpleLanguage.g4 using "mx create-sl-parser"

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SimpleLanguageLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, WS=3, COMMENT=4, LINE_COMMENT=5, IDENTIFIER=6, STRING_LITERAL=7, 
		NUMERIC_LITERAL=8, BOOL_LITERAL=9;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "WS", "COMMENT", "LINE_COMMENT", "LETTER", "NON_ZERO_DIGIT", 
			"DIGIT", "HEX_DIGIT", "OCT_DIGIT", "BINARY_DIGIT", "TAB", "STRING_CHAR", 
			"IDENTIFIER", "STRING_LITERAL", "NUMERIC_LITERAL", "BOOL_LITERAL"
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


	public SimpleLanguageLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "SimpleLanguage.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\13\u0082\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\3\2\3\2\3\2\3\3\3\3\3\4\6\4,\n\4\r\4\16\4-\3\4\3\4\3\5\3\5\3\5\3"+
		"\5\7\5\66\n\5\f\5\16\59\13\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\7\6D"+
		"\n\6\f\6\16\6G\13\6\3\6\3\6\3\7\5\7L\n\7\3\b\3\b\3\t\3\t\3\n\5\nS\n\n"+
		"\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\17\7\17`\n\17\f\17\16"+
		"\17c\13\17\3\20\3\20\7\20g\n\20\f\20\16\20j\13\20\3\20\3\20\3\21\3\21"+
		"\3\21\7\21q\n\21\f\21\16\21t\13\21\5\21v\n\21\3\22\3\22\3\22\3\22\3\22"+
		"\3\22\3\22\3\22\3\22\5\22\u0081\n\22\3\67\2\23\3\3\5\4\7\5\t\6\13\7\r"+
		"\2\17\2\21\2\23\2\25\2\27\2\31\2\33\2\35\b\37\t!\n#\13\3\2\n\5\2\13\f"+
		"\16\17\"\"\4\2\f\f\17\17\6\2&&C\\aac|\3\2\63;\3\2\62;\5\2\62;CHch\3\2"+
		"\629\5\2\f\f\17\17$$\2\u0082\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3"+
		"\2\2\2\2\13\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\3"+
		"%\3\2\2\2\5(\3\2\2\2\7+\3\2\2\2\t\61\3\2\2\2\13?\3\2\2\2\rK\3\2\2\2\17"+
		"M\3\2\2\2\21O\3\2\2\2\23R\3\2\2\2\25T\3\2\2\2\27V\3\2\2\2\31X\3\2\2\2"+
		"\33Z\3\2\2\2\35\\\3\2\2\2\37d\3\2\2\2!u\3\2\2\2#\u0080\3\2\2\2%&\7}\2"+
		"\2&\'\7\177\2\2\'\4\3\2\2\2()\7\60\2\2)\6\3\2\2\2*,\t\2\2\2+*\3\2\2\2"+
		",-\3\2\2\2-+\3\2\2\2-.\3\2\2\2./\3\2\2\2/\60\b\4\2\2\60\b\3\2\2\2\61\62"+
		"\7\61\2\2\62\63\7,\2\2\63\67\3\2\2\2\64\66\13\2\2\2\65\64\3\2\2\2\669"+
		"\3\2\2\2\678\3\2\2\2\67\65\3\2\2\28:\3\2\2\29\67\3\2\2\2:;\7,\2\2;<\7"+
		"\61\2\2<=\3\2\2\2=>\b\5\2\2>\n\3\2\2\2?@\7\61\2\2@A\7\61\2\2AE\3\2\2\2"+
		"BD\n\3\2\2CB\3\2\2\2DG\3\2\2\2EC\3\2\2\2EF\3\2\2\2FH\3\2\2\2GE\3\2\2\2"+
		"HI\b\6\2\2I\f\3\2\2\2JL\t\4\2\2KJ\3\2\2\2L\16\3\2\2\2MN\t\5\2\2N\20\3"+
		"\2\2\2OP\t\6\2\2P\22\3\2\2\2QS\t\7\2\2RQ\3\2\2\2S\24\3\2\2\2TU\t\b\2\2"+
		"U\26\3\2\2\2VW\4\62\63\2W\30\3\2\2\2XY\7\13\2\2Y\32\3\2\2\2Z[\n\t\2\2"+
		"[\34\3\2\2\2\\a\5\r\7\2]`\5\r\7\2^`\5\21\t\2_]\3\2\2\2_^\3\2\2\2`c\3\2"+
		"\2\2a_\3\2\2\2ab\3\2\2\2b\36\3\2\2\2ca\3\2\2\2dh\7$\2\2eg\5\33\16\2fe"+
		"\3\2\2\2gj\3\2\2\2hf\3\2\2\2hi\3\2\2\2ik\3\2\2\2jh\3\2\2\2kl\7$\2\2l "+
		"\3\2\2\2mv\7\62\2\2nr\5\17\b\2oq\5\21\t\2po\3\2\2\2qt\3\2\2\2rp\3\2\2"+
		"\2rs\3\2\2\2sv\3\2\2\2tr\3\2\2\2um\3\2\2\2un\3\2\2\2v\"\3\2\2\2wx\7v\2"+
		"\2xy\7t\2\2yz\7w\2\2z\u0081\7g\2\2{|\7h\2\2|}\7c\2\2}~\7n\2\2~\177\7u"+
		"\2\2\177\u0081\7g\2\2\u0080w\3\2\2\2\u0080{\3\2\2\2\u0081$\3\2\2\2\16"+
		"\2-\67EKR_ahru\u0080\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}