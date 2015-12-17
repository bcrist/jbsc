package com.magicmoremagic.jbsc.util;

import java.io.PrintWriter;
import java.util.Random;

public class CodeGenHelper {

	private static final Random rnd = new Random();
	private static final char[] randomChars = initRandomChars();
	
	private static char[] initRandomChars() {
		char[] val = new char[52];
		for (int i = 0; i < 26; ++i) {
			val[i] = (char)('A' + i);
			val[26 + i] = (char)('a' + i);
		}
		return val;
	}
	
	
	public static String concat(String... strings) {
		StringBuilder sb = new StringBuilder();
		for (String s : strings) {
			sb.append(s);
		}
		return sb.toString();
	}
	
	public static String concatLines(String... lines) {
		StringBuilder sb = new StringBuilder();
		for (String s : lines) {
			sb.append(s);
			sb.append(CodeGenConfig.NL);
		}
		return sb.toString();
	}
	
	public static String toPascalCase(String s) {
		if (s == null || s.isEmpty())
			return s;
		
		StringBuilder sb = new StringBuilder(s.length());
		int length = s.length();
		boolean capitalize = true;
		for (int i = 0; i < length; ++i) {
			char c = s.charAt(i);
			
			if (c == '_' || c == ' ') {
				capitalize = true;
			} else {
				if (capitalize) {
					capitalize = false;
					sb.append(Character.toUpperCase(c));
				} else {
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}
	
	public static String toUnderscoredLowerCase(String s) {
		if (s == null || s.isEmpty())
			return s;
		
		StringBuilder sb = new StringBuilder(s.length());
		int length = s.length();
		boolean first = true;
		for (int i = 0; i < length; ++i) {
			char c = s.charAt(i);
			if (first) {
				if (c != '_') {
					first = false;
				}
				sb.append(Character.toLowerCase(c));
			} else {
				char lc = Character.toLowerCase(c);
				
				if (c == '_') {
					first = true;
				}
				
				if (lc != c) {
					sb.append('_');
				}
				
				sb.append(Character.toLowerCase(c));
			}
		}
		return s.toString();
	}
	
	public static void printCode(String code, PrintWriter writer) {
		String[] lines = code.split("\\r?\\n", -1);
		boolean first = true;
		for (String line : lines) {
			if (first) {
				first = false;
			} else {
				writer.println();
			}
			line = line.replace("\t", CodeGenConfig.INDENT);
			writer.print(line);
		}
	}
	
	public static String getRandomName(int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; ++i) {
			sb.append(randomChars[rnd.nextInt(randomChars.length)]);
		}
		return sb.toString();
	}
	
}
