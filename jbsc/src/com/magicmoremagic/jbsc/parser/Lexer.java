package com.magicmoremagic.jbsc.parser;

import java.io.*;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import com.magicmoremagic.jbsc.*;

public class Lexer {
	
	private IErrorHandler errorHandler;
	private String sourceName;
	private LineNumberReader in;
	private TokenNode lastRead;
	private LexerStateData data;
	
	private static class LexerStateData {
		Lexer lexer;
		
		State current;
		State onSuccess;
		State onFailure;
		CharacterTarget target;
		
		int beginLine, endLine;
		
		StringBuilder tokenValue = new StringBuilder();
		int escapeChar;
		
		StringBuilder mStringDelimiter = new StringBuilder();
		Set<Integer> mStringIgnoredChars = new HashSet<>();
		int mStringCloseCursor;
		
		StringBuilderCharacterTarget tokenValueTarget = new StringBuilderCharacterTarget(tokenValue);
//		StringBuilderCharacterTarget mStringDelimiterTarget = new StringBuilderCharacterTarget(mStringDelimiter);
		SetCharacterTarget mStringIgnoredCharsTarget = new SetCharacterTarget(mStringIgnoredChars);
	}
	
	private static interface State {
		
		static final State DONE = null;
		
		static final InitialState INITIAL = new InitialState().name("INITIAL");
		
		static final PotentialCommentState POTENTIAL_COMMENT = new PotentialCommentState().name("COMMENT.POTENTIAL");
		
		static final EscapeState ESCAPE = new EscapeState().name("ESCAPE");
		static final OptionalNewlineState OPTIONAL_NEWLINE = new OptionalNewlineState().name("OPTIONAL_NEWLINE");
		
		static final QStringState QSTRING = new QStringState().name("QSTRING");
		static final AMStringState AMSTRING = new AMStringState().name("AMSTRING");
		static final AStringState ASTRING = new AStringState().name("ASTRING");
		static final MStringState MSTRING = new MStringState().name("MSTRING.DELIMITER");
	
		static final IDState ID = new IDState().name("ID");
		
		State process(LexerStateData data, int ch);
		State init(LexerStateData data);
	}
	
	public Lexer(Path filePath) throws IOException {
		this(new FileReader(filePath.toFile()), filePath.getFileName().toString(), null);
	}
	
	public Lexer(Path filePath, IErrorHandler errorHandler) {
		this(createFileReader(filePath, errorHandler), filePath.getFileName().toString(), errorHandler);
	}
	
	private static Reader createFileReader(Path filePath, IErrorHandler errorHandler) {
		try {
			return new FileReader(filePath.toFile());
		} catch (FileNotFoundException e) {
			if (errorHandler != null)
				errorHandler.handleError(ErrorCategory.FILE, ErrorType.FILE_NOT_FOUND, "Could not find file: " + filePath.toString(), e);
			
			return new StringReader("");
		}
	}
	
	public Lexer(Reader inputReader, String sourceName) {
		this(inputReader, sourceName, null);
	}
	
	public Lexer(Reader inputReader, String sourceName, IErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
		this.sourceName = sourceName;
		in = new LineNumberReader(inputReader);
		in.setLineNumber(1);
		data = new LexerStateData();
		data.lexer = this;
		lastRead = new TokenNode(); // allows mark() to work before the first token has been consumed
	}
	
	public void setErrorHandler(IErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}
	
	public IErrorHandler getErrorHandler() {
		return errorHandler;
	}
	
	public boolean hasMore() {
		return peek() != null;
	}
	
	public Token peek() {
		if (lastRead.next == null) {
			lex();
		}
		if (lastRead.next != null) {
			return lastRead.next.token;
		}
		
		return null;
	}
	
	public Token consume() {
		if (lastRead.next == null) {
			lex();
		}
		if (lastRead.next != null) {
			lastRead = lastRead.next;
			return lastRead.token;
		}
		
		return null;
	}
	
	public Mark mark() {
		return new Mark(lastRead);
	}
	
	private void lex() {
		try {
			data.beginLine = in.getLineNumber();
			data.tokenValue.setLength(0);
			for (data.current = State.INITIAL; data.current != State.DONE;) {
				data.endLine = in.getLineNumber();
				int ch = in.read();
				data.current = data.current.process(data, ch);
			}
		}
		catch (IOException e) {
			if (errorHandler != null)
				errorHandler.handleError(ErrorCategory.FILE, ErrorType.IO_EXCEPTION, sourceName + ": " + e.getMessage(), e);
		}
	}
	
	private void makeToken(TokenType type, TokenSubtype subtype, String value, int beginLine, int endLine) {
		TokenNode node = lastRead;
		while (node.next != null) {
			node = node.next;
		}
		node.next = new TokenNode(new Token(type, subtype, value, sourceName, beginLine, endLine));
	}
	
	private void error(ErrorType type, String message, Exception ex) {
		if (errorHandler != null)
			errorHandler.handleError(ErrorCategory.LEXER, type, message, ex);
	}
	
	@SuppressWarnings("unused")
	private static abstract class AbstractState implements State {
		
		private String name;
		
		@SuppressWarnings("unchecked")
		public <T> T name(String name) {
			this.name = name;
			return (T)this;
		}
		
		@Override
		public String toString() {
			if (name != null) {
				return name;
			}
			
			return this.getClass().getName();
		}
		
		@Override
		public State init(LexerStateData data) { return this; }
		
		protected State makeToken(LexerStateData data, TokenType type, int ch) {
			data.tokenValue.append((char)ch);
			makeToken(data, type);
			return State.DONE;
		}
		
		protected State makeToken(LexerStateData data, TokenType type) {
			data.lexer.makeToken(type, TokenSubtype.DEFAULT, data.tokenValue.toString(), data.beginLine, data.endLine);
			return State.DONE;
		}
		
		protected State makeToken(LexerStateData data, TokenType type, TokenSubtype subtype) {
			data.lexer.makeToken(type, subtype, data.tokenValue.toString(), data.beginLine, data.endLine);
			return State.DONE;
		}
		
		protected State error(LexerStateData data, ErrorType type, Exception ex) {
			return error(data, type, ex.getMessage(), ex);
		}
		
		protected State error(LexerStateData data, ErrorType type, String message) {
			return error(data, type, message, null);
		}
		
		protected State error(LexerStateData data, ErrorType type, String message, Exception ex) {
			StringBuilder sb = new StringBuilder();
			sb.append(data.lexer.sourceName);
			sb.append(':');
			sb.append(data.beginLine);
			if (data.endLine != data.beginLine) {
				sb.append('-');
				sb.append(data.endLine);
			}
			
			if (data.current != null) {
				sb.append('(');
				sb.append(data.current.toString());
				sb.append(')');
			}
			
			sb.append(": ");
			sb.append(message);
			
			data.lexer.error(type, sb.toString(), ex);
			return State.DONE;
		}
		
		protected State addToTarget(LexerStateData data, char ch) {
			if (data.target != null)
				data.target.append(ch);
			
			return data.onSuccess;
		}
		
		protected State addToTarget(LexerStateData data, CharSequence csq) {
			if (data.target != null)
				data.target.append(csq);
			
			return data.onSuccess;
		}
		
		protected State addToTarget(LexerStateData data, CharSequence csq, int begin, int end) {
			if (data.target != null)
				data.target.append(csq, begin, end);
			
			return data.onSuccess;
		}
	}
	
	private static class InitialState extends AbstractState {
		@Override
		public State process(LexerStateData data, int ch) {
			if (ch < 0)
				return State.DONE;
			
			if (ch <= ' ') {
				data.beginLine = data.lexer.in.getLineNumber();
				return this;
			}
			
			switch (ch) {
				case '{':	return makeToken(data, TokenType.OPEN, ch);
				case '}':	return makeToken(data, TokenType.CLOSE, ch);
				case ';':	return makeToken(data, TokenType.END, ch);
				case '*':	return makeToken(data, TokenType.WILDCARD, ch);
				
				case '"':	return State.QSTRING.init(data);
				case '<':	return State.AMSTRING.init(data);				
				case '/':	return State.POTENTIAL_COMMENT.init(data, this, null);
				default:	return State.ID.init(data).process(data, ch);
			}
		}
	}
	
	private static class PotentialCommentState extends AbstractState {
		private static final State LINE_COMMENT = new AbstractState() {
			@Override
			public State process(LexerStateData data, int ch) {
				if (ch < 0)
					return data.onSuccess.process(data, ch);
				
				if (data.lexer.in.getLineNumber() == data.beginLine)
					return this;
				
				return data.onSuccess.process(data, ch);
			}
		}.name("COMMENT.LINE");
		
		private static final State MULTILINE_COMMENT = new AbstractState() {
			@Override
			public State process(LexerStateData data, int ch) {
				if (ch < 0) {
					error(data, ErrorType.UNEXPECTED_EOF, "Multi-line comment not closed!");
					return data.onFailure.process(data, ch);
				}
				
				if (ch == '*')
					return POTENTIAL_MULTILINE_COMMENT_END;
				
				return this;
			}
		}.name("COMMENT.MULTILINE");
		
		private static final State POTENTIAL_MULTILINE_COMMENT_END = new AbstractState() {
			@Override
			public State process(LexerStateData data, int ch) {
				if (ch < 0) {
					error(data, ErrorType.UNEXPECTED_EOF, "Multi-line comment not closed!");
					return data.onFailure.process(data, ch);
				}
				
				if (ch == '/')
					return data.onSuccess;
				
				if (ch == '*')
					return this;
				
				return MULTILINE_COMMENT;
			}
		}.name("COMMENT.MULTILINE.POTENTIAL_END");
		
		
		public State init(LexerStateData data, State currentState, CharacterTarget currentTarget) {
			data.onFailure = currentState;
			data.onSuccess = currentState;
			data.target = currentTarget;
			return super.init(data);
		}
		
		public State init(LexerStateData data, State currentState, State afterComment, CharacterTarget currentTarget) {
			data.onFailure = currentState;
			data.onSuccess = afterComment;
			data.target = currentTarget;
			return super.init(data);
		}
		
		@Override
		public State process(LexerStateData data, int ch) {
			if (ch == '/')
				return LINE_COMMENT;
			
			if (ch == '*')
				return MULTILINE_COMMENT;
			
			if (data.target != null)
				data.target.append('/');

			return data.onFailure.process(data, ch);
		}
	}
	
	private static class EscapeState extends AbstractState {
		private static class HexEscapeState extends AbstractState {
			private int count;
			public HexEscapeState(int count) {
				this.count = count;
				name("ESCAPE.HEX[" + count + "]");
			}
			
			@Override
			public State init(LexerStateData data) {
				data.escapeChar = 0;
				return super.init(data);
			}
			
			@Override
			public State process(LexerStateData data, int ch) {
				data.escapeChar = data.escapeChar << 4;
				
				if (ch >= '0' && ch <= '9') {
					data.escapeChar |= (ch - '0');
				} else if (ch >= 'a' && ch <= 'f') {
					data.escapeChar |= 10 + (ch - 'a');
				} else if (ch >= 'A' && ch <= 'F') {
					data.escapeChar |= 10 + (ch - 'A');
				} else {
					error(data, ErrorType.SHORT_HEX_ESCAPE, "Expected another " + (count - 1) + " hex escape character(s)!");
					addToTarget(data, (char)data.escapeChar);
					return data.onFailure.process(data, ch);
				}
				
				if (count > 1) {
					return HEX_ESCAPE[count - 1];
				} else {
					return addToTarget(data, (char)data.escapeChar);
				}
			}
		}
		
		static final HexEscapeState[] HEX_ESCAPE = { null,
				new HexEscapeState(1),	new HexEscapeState(2),
				new HexEscapeState(3),	new HexEscapeState(4)
			};
		
		public State init(LexerStateData data, State currentState, CharacterTarget currentTarget) {
			data.onFailure = currentState;
			data.onSuccess = currentState;
			data.target = currentTarget;
			return super.init(data);
		}
		
		@Override
		public State process(LexerStateData data, int ch) {
			if (ch < 0) {
				error(data, ErrorType.UNEXPECTED_EOF, "Expected escape sequence!");
				return data.onFailure;
			}
			
			switch (ch) {
				case 'n':	return addToTarget(data, '\n');
				case 'r':	return addToTarget(data, '\r');
				case 't':	return addToTarget(data, '\t');
				case 'b':	return addToTarget(data, '\b');
				case 'f':	return addToTarget(data, '\f');
				case '0':	return addToTarget(data, '\0');
				case 'v':	return addToTarget(data, '\u000b');
	
				case 'x':
				case 'X':	return HEX_ESCAPE[2].init(data);
				case 'u':	return HEX_ESCAPE[4].init(data);
					
				default:	return addToTarget(data, (char)ch);
			}
		}
	}

	private static class OptionalNewlineState extends AbstractState {
		private static final State LF_NO_TARGET = new AbstractState() {
			@Override
			public State process(LexerStateData data, int ch) {
				if (ch == '\n')
					return data.onSuccess;
				
				return data.onFailure.process(data, ch);
			}
		}.name("OPTIONAL_NEWLINE.LF_AFTER_CR");
		
		public State init(LexerStateData data, State currentState, CharacterTarget currentTarget) {
			data.onFailure = currentState;
			data.onSuccess = currentState;
			data.target = currentTarget;
			return super.init(data);
		}
		
		public State init(LexerStateData data, State sameLine, State newLine, CharacterTarget currentTarget) {
			data.onFailure = sameLine;
			data.onSuccess = newLine;
			data.target = currentTarget;
			return super.init(data);
		}
		
		@Override
		public State process(LexerStateData data, int ch) {
			
			if (ch == '\r') {
				addToTarget(data, '\n');
				data.onFailure = data.onSuccess;
				return LF_NO_TARGET;
			}
			
			if (ch == '\n') {
				return addToTarget(data, '\n');
			}
			
			return data.onFailure.process(data, ch);
		}
	}
	
	private static class QStringState extends AbstractState {
		@Override
		public State process(LexerStateData data, int ch) {
			if (ch < 0) {
				error(data, ErrorType.UNEXPECTED_EOF, "Quoted string not closed!");
				return State.DONE;
			}
			
			if (ch == '"')
				return makeToken(data, TokenType.TEXT, TokenSubtype.QSTRING);
			
			if (ch == '\\')
				return State.ESCAPE.init(data, this, data.tokenValueTarget);
			
			data.tokenValue.append((char)ch);
			return this;
		}
	}
	
	private static class AMStringState extends AbstractState {
		@Override
		public State process(LexerStateData data, int ch) {
			if (ch == '[')
				return State.MSTRING.init(data);
			
			return State.ASTRING.process(data, ch);
		}
	}
	
	private static class AStringState extends AbstractState {
		@Override
		public State process(LexerStateData data, int ch) {
			if (ch < 0) {
				error(data, ErrorType.UNEXPECTED_EOF, "Angle-quoted string not closed!");
				return State.DONE;
			}
			
			if (ch == '>')
				return makeToken(data, TokenType.TEXT, TokenSubtype.ASTRING);
			
			data.tokenValue.append((char)ch);
			return this;
		}
	}
	
	private static class MStringState extends AbstractState {
		private static final State IGNORE = new AbstractState() {
			@Override
			public State process(LexerStateData data, int ch) {
				if (ch < 0)
					return State.DONE;
				
				if (ch == '[')
					return INIT;
				
				if (ch == '\\')
					return State.ESCAPE.init(data, this, data.mStringIgnoredCharsTarget);
				
				data.mStringIgnoredChars.add(ch);
				return this;
			}
		}.name("MSTRING.IGNORE");
		
		private static final State INIT = new AbstractState() {
			@Override
			public State process(LexerStateData data, int ch) {
				return State.OPTIONAL_NEWLINE.init(data, LINE_INIT, null).process(data, ch);
			}
		}.name("MSTRING.FIRST_LINE");
		
		private static final State LINE_INIT = new AbstractState() {
			@Override
			public State process(LexerStateData data, int ch) {
				if (data.mStringIgnoredChars.contains(ch))
					return this;
				
				return MAIN.process(data, ch);
			}
		}.name("MSTRING.LINE_PADDING");
		
		private static final State MAIN = new AbstractState() {
			@Override
			public State process(LexerStateData data, int ch) {
				if (ch < 0) {
					error(data, ErrorType.UNEXPECTED_EOF, "Multi-line string not closed!");
					return State.DONE;
				}
				
				if (ch == ']')
					return POSSIBLE_CLOSE.init(data);
				
				return State.OPTIONAL_NEWLINE.init(data, MAIN_HELPER, LINE_INIT, data.tokenValueTarget).process(data, ch);
			}		
		}.name("MSTRING");
		
		private static final State MAIN_HELPER = new AbstractState() {
			@Override
			public State process(LexerStateData data, int ch) {
				data.tokenValue.append((char)ch);
				return MAIN;
			}		
		}.name("MSTRING");
		
		private static final State POSSIBLE_CLOSE = new AbstractState() {
			@Override
			public State init(LexerStateData data) {
				data.mStringCloseCursor = 0;
				return super.init(data);
			};
			
			@Override
			public State process(LexerStateData data, int ch) {
				int required = Integer.MAX_VALUE;
				int cursor = data.mStringCloseCursor;
				int delimLength = data.mStringDelimiter.length();
				
				if (cursor < delimLength) {
					required = data.mStringDelimiter.charAt(cursor);
				} else switch (cursor - delimLength) {
					case 0:	required = ']'; break;
					case 1: required = '>'; break;
				}
				
				if (ch == required) {
					if (cursor == delimLength + 1) {
						return makeToken(data, TokenType.TEXT, TokenSubtype.MSTRING);
					}
					++data.mStringCloseCursor;
					return this;
				} else {
					data.tokenValue.append(']');
					data.tokenValue.append(data.mStringDelimiter, 0, Math.min(delimLength, cursor));
					
					if (cursor == delimLength + 1) {
						return init(data).process(data, ch);
					} else { // cursor <= delimLength
						return MAIN.process(data, ch);	
					}
				}
			}
		}.name("MSTRING.POSSIBLE_CLOSE");
		
		@Override
		public State init(LexerStateData data) {
			data.mStringDelimiter.setLength(0);
			data.mStringIgnoredChars.clear();
			return super.init(data);
		}
		
		@Override
		public State process(LexerStateData data, int ch) {
			if (ch < 0)
				return State.DONE;
			
			if (ch == '[')
				return INIT;
			
			if (ch == '@')
				return IGNORE;
			
			if (ch == ']') {
				error(data, ErrorType.INVALID_LITERAL, "Invalid character found in MString delimiter: ']'");
				return this;
			}
			
			data.mStringDelimiter.append((char)ch);
			return this;
		}
	}
	
	private static class IDState extends AbstractState {
		private static final State AFTER_COMMENT = new AbstractState() {
			@Override
			public State process(LexerStateData data, int ch) {
				if (data.tokenValue.length() > 0) {
					makeToken(data, TokenType.ID);
				}
				data.beginLine = data.lexer.in.getLineNumber();
				data.tokenValue.setLength(0);
				return State.INITIAL.init(data).process(data, ch);
			}
		}.name("ID.AFTER_COMMENT");
		
		@Override
		public State process(LexerStateData data, int ch) {
			if (ch < 0) {
				if (data.tokenValue.length() > 0) {
					return makeToken(data, TokenType.ID);
				}
				return State.DONE;
			}
			
			boolean done = false;
			
			if (ch <= ' ')
				done = true;
			else switch (ch) {
				case '/':
					return State.POTENTIAL_COMMENT.init(data, this, AFTER_COMMENT, data.tokenValueTarget);
			
				case '{':
				case '}':
				case ';':
				case '*':
				case '"':
				case '<':
					done = true;
			}
			
			if (done) {
				if (data.tokenValue.length() > 0) {
					makeToken(data, TokenType.ID);
				}
				data.beginLine = data.lexer.in.getLineNumber();
				data.tokenValue.setLength(0);
				return State.INITIAL.init(data).process(data, ch);
			} else {
				data.tokenValue.append((char)ch);
				return this;
			}
		}
	}
	
	private class TokenNode {
		private Token token;
		private TokenNode next;
		
		public TokenNode() { }
		
		public TokenNode(Token token) {
			this.token = token;
		}
	}
	
	public class Mark {
		private TokenNode markedNode;
		
		private Mark(TokenNode node) {
			markedNode = node;
		}
		
		public void restore() {
			lastRead = markedNode;
		}
		
		public Token last() {
			return markedNode.token;
		}
		
		public Token peek() {
			if (markedNode.next == null) {
				lex();
			}
			if (markedNode.next != null) {
				return markedNode.next.token;
			}
			
			return null;
		}
	}
	
	private interface CharacterTarget {
		CharacterTarget append(CharSequence csq);
	    CharacterTarget append(CharSequence csq, int start, int end);
	    CharacterTarget append(char c);
	}
	
	private static class SetCharacterTarget implements CharacterTarget {
		private Set<Integer> set;
		
		public SetCharacterTarget(Set<Integer> set) {
			this.set = set;
		}

		@Override
		public CharacterTarget append(CharSequence csq) {
			return append(csq, 0, csq.length());
		}

		@Override
		public CharacterTarget append(CharSequence csq, int start, int end) {
			for (int i = start; i < end; ++i) {
				append(csq.charAt(i));
			}
			return this;
		}

		@Override
		public CharacterTarget append(char c) {
			set.add((int)c);
			return this;
		}

	}
	
	private static class StringBuilderCharacterTarget implements CharacterTarget {
		private StringBuilder sb;
		
		public StringBuilderCharacterTarget(StringBuilder sb) {
			this.sb = sb;
		}
		
		@Override
		public CharacterTarget append(CharSequence csq) {
			sb.append(csq);
			return this;
		}

		@Override
		public CharacterTarget append(CharSequence csq, int start, int end) {
			sb.append(csq, start, end);
			return this;
		}

		@Override
		public CharacterTarget append(char c) {
			sb.append(c);
			return this;
		}
	}
	
}
