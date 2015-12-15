package com.magicmoremagic.jbsc.util;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeGenHelper {

	
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
	
	
	private static final Pattern SYSTEM_INCLUDE_PATTERN = Pattern.compile("<([^<>\"]+)>");
	private static final Pattern LOCAL_INCLUDE_PATTERN = Pattern.compile("\"([^<>\"]+)\"");
	public static void printIncludes(Collection<String> includes, PrintWriter writer) {
		List<String> sortedIncludes = new ArrayList<String>(includes);
		Collections.sort(sortedIncludes);
		
		for (String include : sortedIncludes) {
			writer.print("#include ");
			Matcher matcher = SYSTEM_INCLUDE_PATTERN.matcher(include);
			if (matcher.matches()) {
				writer.print(include);
			} else {
				matcher = LOCAL_INCLUDE_PATTERN.matcher(include);
				if (matcher.matches()) {
					writer.print(include);
				} else {
					writer.print('"');
					writer.print(include.replace("\"", ""));
					writer.print('"');
				}
			}
			writer.println();
		}
	}
	
}
