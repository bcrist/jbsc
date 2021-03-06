package com.magicmoremagic.jbsc.util;

public abstract class CodeGenConfig {

	public static final String NL = "\n";
	public static final String INDENT = "   ";
	public static final String QUALIFIED_NAME_SEPARATOR = ".";
	
	public static final String HEADER_EXT = ".hpp";
	public static final String SQL_HEADER_EXT = "_sql.hpp";
	public static final String INLINE_SOURCE_EXT = ".inl";
	public static final String SOURCE_EXT = ".cpp";
	
	
	public static final String FUNCTION_DECL_PREFIX = "";
	
	public static final String FUNCTION_IMPL_PREFIX = "///////////////////////////////////////////////////////////////////////////////\n";
	
	public static final String OUTPUT_HEADER = CodeGenHelper.concatLines(
			"// Copyright (c) 2013 - 2016 Benjamin Crist",
			"// See LICENSE file for details",
			"",
			"///////////////////////////////////////////////////////////////////////////////",
			"/// \\file   $(Filename)",
			"/// \\author Benjamin Crist",
			"///",
			"/// \\date   $(Date)",
			"/// \\brief  THIS FILE HAS BEEN AUTOGENERATED FROM $(SpecFile)",
			"///",
			"///         DO NOT EDIT!", "");
	
}
