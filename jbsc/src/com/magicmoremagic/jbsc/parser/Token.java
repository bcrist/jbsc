package com.magicmoremagic.jbsc.parser;

public class Token {
	private TokenType type;
	private TokenSubtype subtype;
	private String value;
	private String sourceName;
	private int line;
	private int endLine;
	
	public Token(TokenType type, String value, String sourceFile, int line, int endLine) {
		this(type, TokenSubtype.DEFAULT, value, sourceFile, line, endLine);
	}
	
	public Token(TokenType type, TokenSubtype subtype, String value, String sourceName, int line, int endLine) {
		this.type = type;
		this.subtype = subtype;
		this.value = value;
		this.sourceName = sourceName;
		this.line = line;
		this.endLine = endLine;
	}
	
	public TokenType getType() {
		return type;
	}
	
	public TokenSubtype getSubtype() {
		return subtype;
	}
	
	public String getValue() {
		return value;
	}
	
	public String getSourceName() {
		return sourceName;
	}
	
	public int getLine() {
		return line;
	}
	
	public int getEndingLine() {
		return endLine;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type.name());
		if (subtype != TokenSubtype.DEFAULT) {
			sb.append('.');
			sb.append(subtype.name());
		}
		sb.append('(');
		sb.append(sourceName);
		sb.append(':');
		sb.append(line);
		if (endLine != line) {
			sb.append('-');
			sb.append(endLine);	
		}
		sb.append("): '");
		sb.append(value);
		sb.append("'");
		
		return sb.toString();
	}
	
}
