package com.magicmoremagic.jbsc.parser;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.*;

import com.magicmoremagic.jbsc.*;
import com.magicmoremagic.jbsc.objects.*;
import com.magicmoremagic.jbsc.objects.base.*;
import com.magicmoremagic.jbsc.objects.base.AbstractEntity.ExtractNamespaceResult;
import com.magicmoremagic.jbsc.objects.containers.*;
import com.magicmoremagic.jbsc.objects.queries.FieldList;
import com.magicmoremagic.jbsc.objects.types.*;
import com.magicmoremagic.jbsc.parser.Lexer.Mark;

public class Parser {

	private IErrorHandler errorHandler;
	private Path specPath;
	private Lexer lexer;
	
	public Parser(Path filePath) throws IOException {
		this.specPath = filePath;
		this.lexer = new Lexer(filePath);
	}
	
	public Parser(Path filePath, IErrorHandler errorHandler) {
		this.specPath = filePath;
		this.lexer = new Lexer(filePath, errorHandler);
		this.errorHandler = errorHandler;
	}
	
	public Parser(Reader inputReader, Path specPath, IErrorHandler errorHandler) {
		this.specPath = specPath;
		this.lexer = new Lexer(inputReader, specPath.toString(), errorHandler);
		this.errorHandler = errorHandler;
	}
	
	public Lexer getLexer() {
		return lexer;
	}
	
	public void setLexer(Lexer lexer) {
		this.lexer = lexer;
	}
	
	public Spec parse() {
		if (lexer == null) {
			fatal(ErrorType.ASSERTION_ERROR, null, null, "No Lexer available!", null);
		}
		
		Spec spec = new Spec();
		spec.setInputPath(specPath);
		if (specPath != JBSC.INTERNAL_PATH) {
			spec.addIncludedSpec(JBSC.app.getInternalSpec());
			JBSC.app.addSpec(specPath, spec);
		}
		pSpec(spec);
		return spec;
	}
	
	private void pSpec(Spec spec) {
		// spec := spec-decl spec | ;	
		while (lexer.hasMore()) {
			if (!pSpecDecl(spec)) {
				parseError(lexer.consume(), "Expected spec-decl!");
			}
		}
	}
	
	private boolean pSpecDecl(Spec spec) {
		// spec-decl := spec-name | spec-include | namespace | scoped-decl ;
		if (pSpecName(spec)) return true;
		if (pSpecInclude(spec)) return true;
		if (pNamespace(spec)) return true;
		if (pScopedDecl(spec)) return true;
		
		return false;
	}
	
	private  boolean pSpecName(Spec spec) {
		// spec-name := 'spec' text<specname> ';' ;
		if (optionalID("spec")) {
			Mark mark = lexer.mark();
			String specName = expectText();
			if (specName != null && expectEnd()) {
				try {
					spec.setName(specName);
				} catch (Exception e) {
					warning(mark.peek(), spec, "Could not set spec name!", e);
				}	
			}
			return true;
		}
		return false;
	}

	private boolean pSpecInclude(Spec spec) {
		// spec-include := 'include' ( spec-include-list | spec-include-name ) ;
		if (optionalID("include")) {
			if (pSpecIncludeList(spec)) return true;
			if (!pSpecIncludeName(spec)) {
				parseError(lexer.peek(), spec, "Expected filename string!");
			}
			return true;
		}
		return false;
	}
	
	private boolean pSpecIncludeList(Spec spec) {
		// spec-include-list := '{' spec-include-names '}' [';'] ;
		if (optionalOpen()) {
			if (pSpecIncludeNames(spec) && expectClose() && optionalEnd()) return true;
			return true;
		}
		return false;
	}
	
	private boolean pSpecIncludeNames(Spec spec) {
		// spec-include-names := spec-include-name spec-include-names | ;
		while (pSpecIncludeName(spec));
		return true;
	}
	
	private boolean pSpecIncludeName(Spec spec) {
		// spec-include-name := text ';' ;
		Mark mark = lexer.mark();
		String include = optionalText();
		if (include != null) {
			if (expectEnd()) {
				Spec includeSpec = JBSC.app.getSpec(include);
				if (includeSpec != null && includeSpec != spec && !includeSpec.hasIncludedSpec(spec)) {
					if (includeSpec.getErrorCount() > 0) {
						warning(mark.peek(), spec, "There were parsing errors in the spec '" + includeSpec.getName() + "'.");
					}
					spec.addIncludedSpec(includeSpec);
				} else {
					warning(mark.peek(), spec, "Could not parse spec '" + include + "'.");
				}
			}
			return true;
		}
		return false;
	}
	
	private boolean pNamespace(AbstractContainer parent) {
		// namespace := 'namespace' id<name> ( ';' | '{' namespace-decls '}' [';'] );
		if (optionalID("namespace")) {
			Mark mark = lexer.mark();
			String name = expectID();
			if (name != null) {
				Namespace namespace = new Namespace(name);
				try {
					parent.addChild(namespace);
				} catch (Exception e) {
					warning(mark.peek(), parent, "Could not add Namespace '" + namespace.getName() + "'.", e);
				}
				if (!optionalEnd() && expectOpen() && pNamespaceDecls(namespace) && expectClose()) {
					optionalEnd();
				}
			}
			return true;
		}
		return false;
	}
	
	private boolean pNamespaceDecls(Namespace namespace) {
		// namespace-decls := namespace-decl namespace-decls | ;
		while (pNamespaceDecl(namespace));
		return true;
	}
	
	private boolean pNamespaceDecl(Namespace namespace) {
		// namespace-decl := namespace | scoped-decl ;
		
		if (pNamespace(namespace)) return true;
		if (pScopedDecl(namespace)) return true;
		
		return false;
	}
	
	@SuppressWarnings("serial")	private static final Map<String, Set<Flag>> validScopeFlags = new HashMap<String, Set<Flag>>() {{
		put("no-skip-parse", EnumSet.of(Flag.NO_SKIP_PARSE));
		put("no-skip-assign", EnumSet.of(Flag.NO_SKIP_ASSIGN));
		EnumSet<Flag> both = EnumSet.of(Flag.NO_SKIP_PARSE, Flag.NO_SKIP_ASSIGN);
		put("no-skip-assign-parse", both);
		put("no-skip-parse-assign", both);
	}};
	
	
	private boolean pScopedDecl(AbstractContainer parent) {
		// scoped-decl := code | col-type | class-type | table | flag | ';' ;
		// TODO fieldset
		
		if (pCode(parent)) return true;
		if (pColType(parent)) return true;
		if (pClassType(parent)) return true;
		if (pAggregateType(parent)) return true;
		if (pTable(parent)) return true;
		if (pEntityFlag(parent, validScopeFlags)) return true;
		if (optionalEnd()) return true;
		
		return false;
	}
	
	private boolean pCode(AbstractContainer parent) {
		// code := [code-qualifier] unqualified-code ;
		// code-qualifier := 'inline' | 'header' | 'sqlid' | 'source' ;
		
		if (pUnqualifiedCode(parent, OutputFileType.SOURCE)) return true;
		
		Mark mark = lexer.mark();
		String token = optionalID();
		if (token != null) {
			OutputFileType type = OutputFileType.SOURCE;
			if (token.equals("inline"))
				type = OutputFileType.INLINE_SOURCE;
			else if (token.equals("header"))
				type = OutputFileType.HEADER;
			else if (token.equals("sqlid"))
				type = OutputFileType.SQL_HEADER;
			else if (token.equals("source"))
				type = OutputFileType.SOURCE;
			else {
				mark.restore();
				return false;
			}
			
			if (pUnqualifiedCode(parent, type)) return true;
			
			mark.restore();
			return false;
		}
		return false;
	}
	
	private boolean pUnqualifiedCode(AbstractContainer parent, OutputFileType type) {
		// unqualified-code := 'code' text<code> ';' ;
		if (optionalID("code")) {
			Mark mark = lexer.mark();
			String codeString = expectText();
			if (codeString != null && expectEnd()) {
				Code code = new Code();
				code.setType(type);
				code.setCode(codeString);
				try {
					parent.addChild(code);
				} catch (Exception e) {
					warning(mark.peek(), parent, "Could not add Code.", e);
				}
			}
			return true;
		}
		return false;
	}
	
	private boolean pColType(AbstractContainer parent) {
		// col-type := 'coltype' text '{' col-type-decls '}' [';'] ;
		if (optionalID("coltype")) {
			Mark mark = lexer.mark();
			String name = expectID();
			if (name != null) {
				ColType colType = new ColType(name);
				try {
					parent.addChild(colType);
				} catch (Exception e) {
					warning(mark.peek(), parent, "Could not add ColType '" + colType.getName() + "'.", e);
				}
				if (expectOpen() && pColTypeDecls(colType) && expectClose()) {
					optionalEnd();
					
					// make sure colType has an affinity set
					if (colType.getAffinity() == null) {
						warning(mark.peek(), colType, "No affinity set!");
					}
				}
			}
			return true;
		}
		return false;
	}
	
	private boolean pColTypeDecls(ColType colType) {
		// col-type-decls := col-type-decl col-type-decls | ;
		while (pColTypeDecl(colType));
		return true;
	}
	
	@SuppressWarnings("serial")	private static final Map<String, Set<Flag>> validColTypeFlags = new HashMap<String, Set<Flag>>() {{
		put("assign-by-value", EnumSet.of(Flag.ASSIGN_BY_VALUE));
		put("no-skip-parse", EnumSet.of(Flag.NO_SKIP_PARSE));
		put("no-skip-assign", EnumSet.of(Flag.NO_SKIP_ASSIGN));
		EnumSet<Flag> both = EnumSet.of(Flag.NO_SKIP_PARSE, Flag.NO_SKIP_ASSIGN);
		put("no-skip-assign-parse", both);
		put("no-skip-parse-assign", both);
	}};
	
	
	private boolean pColTypeDecl(ColType colType) {
		// col-type-decl := affinity | constraints | flag | field-type-decl ;
		
		if (pAffinity(colType)) return true;
		if (pConstraints(colType)) return true;
		if (pEntityFlag(colType, validColTypeFlags)) return true;
		if (pFieldTypeDecl(colType, Phase.PARSE)) return true;
		
		return false;
	}

	private boolean pAffinity(ColType colType) {
		// affinity := 'affinity' text ';' ;
		if (optionalID("affinity")) {
			Mark mark = lexer.mark();
			String affinity = expectText();
			if (affinity != null && expectEnd()) {
				String oldAffinity = colType.getAffinity();
				if (oldAffinity != null && !oldAffinity.equals(affinity)) {
					warning(mark.peek(), colType, "Affinity was previously specified as '" + oldAffinity + "'; overwritten by '" + affinity + "'.");
				}
				colType.setAffinity(affinity);
			}
			return true;
		}
		return false;
	}
	
	private boolean pConstraints(ColType colType) {
		// constraints := 'constraints' text ';' ;
		if (optionalID("constraints")) {
			Mark mark = lexer.mark();
			String constraints = expectText();
			if (constraints != null && expectEnd()) {
				String oldConstraints = colType.getConstraints();
				if (oldConstraints != null && !oldConstraints.equals(constraints)) {
					warning(mark.peek(), colType, "Constraints were previously specified as '" + oldConstraints + "'; overwritten by '" + constraints + "'.");
				}
				colType.setConstraints(constraints);
			}
			return true;
		}
		return false;
	}
	
	private boolean pClassType(AbstractContainer parent) {
		// class-type := 'type' id<name> '{' class-type-decls '}' [';'] ;
		if (optionalID("type")) {
			Mark mark = lexer.mark();
			String name = expectID();
			if (name != null) {
				ClassType classType = new ClassType(name);
				Mark preParse = lexer.mark();
				if (expectOpen() && pClassTypeDecls(classType, Phase.PREPARSE) && expectClose()) {
					// make sure classType has an class name set
					if (classType.getClassName() == null) {
						warning(mark.peek(), classType, "No class name set!");
					} else {
						AbstractContainer classParent = parent;
						try {
							if (classType.isBuiltin()) {
								if (parent.getRoot() != null)
									classParent = parent.getRoot();
							} else {
								ExtractNamespaceResult nsInfo = parent.extractNamespace(classType.getClassName());
								if (nsInfo.namespace != null) {
									classParent = nsInfo.namespace;
									classType.setClassName(nsInfo.name);
								}
							}
						
							classType.setFunctionNamespace(parent.getNamespace());
							classParent.addChild(classType);
						} catch (Exception e) {
							warning(mark.peek(), parent, "Could not add ClassType '" + classType.getName() + "' to '" + classParent.getFullyQualifiedName() + "'.", e);
							return true;
						}
						
						preParse.restore();
						if (optionalOpen() && pClassTypeDecls(classType, Phase.PARSE) && optionalClose()) {
							optionalEnd();
						}
					}
				}
			}
			return true;
		}
		
		return false;
	}
	
	private boolean pClassTypeDecls(ClassType classType, Phase phase) {
		// class-type-decls := class-type-decl class-type-decls | ;
		while (pClassTypeDecl(classType, phase));
		return true;
	}
		
	@SuppressWarnings("serial")	private static final Map<String, Set<Flag>> validClassTypeFlags = new HashMap<String, Set<Flag>>() {{
		put("assign-by-value", EnumSet.of(Flag.ASSIGN_BY_VALUE));
		put("builtin", EnumSet.of(Flag.BUILTIN));
		put("no-skip-parse", EnumSet.of(Flag.NO_SKIP_PARSE));
		put("no-skip-assign", EnumSet.of(Flag.NO_SKIP_ASSIGN));
		EnumSet<Flag> both = EnumSet.of(Flag.NO_SKIP_PARSE, Flag.NO_SKIP_ASSIGN);
		put("no-skip-assign-parse", both);
		put("no-skip-parse-assign", both);
	}};
	
	
	private boolean pClassTypeDecl(ClassType classType, Phase phase) {
		// class-type-decl := class-name | class-type-fields | flag | field-type-decl ;		
		
		if (pClassName(classType, phase)) return true;
		if (pFieldTypeFields(classType, phase)) return true;
		if (pEntityFlag(classType, validClassTypeFlags)) return true;
		if (pFieldTypeDecl(classType, phase)) return true;
		
		return false;
	}

	private boolean pClassName(ClassType classType, Phase phase) {
		// class-name := 'class' text<classname> ';' ;
		// only parsed on Phase.PREPARSE
		if (optionalID("class")) {
			Mark mark = lexer.mark();
			String className = expectText();
			if (className != null && expectEnd() && phase == Phase.PREPARSE) {
				String oldClassName = classType.getClassName();
				if (oldClassName != null && !oldClassName.equals(className)) {
					warning(mark.peek(), classType, "Class name was previously specified as '" + oldClassName + "'; overwritten by '" + className + "'.");
				}
				classType.setClassName(className);
			}
			return true;
		}
		return false;
	}
	
	private boolean pAggregateType(AbstractContainer parent) {
		// aggregate-type := 'aggregate' id<name> '{' aggregate-type-decls '}' [';'] ;
		if (optionalID("aggregate")) {
			Mark mark = lexer.mark();
			String name = expectID();
			if (name != null) {
				AggregateType type = new AggregateType(name);
				try {
					parent.addChild(type);
				} catch (Exception e) {
					warning(mark.peek(), parent, "Could not add AggregateType '" + type.getName() + "' to '" + parent.getFullyQualifiedName() + "'.", e);
				}
				if (expectOpen() && pAggregateTypeDecls(type) && expectClose()) {
					optionalEnd();
				}
			}
			return true;
		}
		return false;
	}
	
	private boolean pAggregateTypeDecls(AggregateType type) {
		// aggregate-type-decls := aggregate-type-decl aggregate-type-decls | ;
		while (pAggregateTypeDecl(type));
		return true;
	}
	
	@SuppressWarnings("serial")	private static final Map<String, Set<Flag>> validAggregateTypeFlags = new HashMap<String, Set<Flag>>() {{
		put("assign-by-value", EnumSet.of(Flag.ASSIGN_BY_VALUE));
		put("no-skip-parse", EnumSet.of(Flag.NO_SKIP_PARSE));
		put("no-skip-assign", EnumSet.of(Flag.NO_SKIP_ASSIGN));
		EnumSet<Flag> both = EnumSet.of(Flag.NO_SKIP_PARSE, Flag.NO_SKIP_ASSIGN);
		put("no-skip-assign-parse", both);
		put("no-skip-parse-assign", both);
	}};
	
	
	private boolean pAggregateTypeDecl(AggregateType type) {
		// aggregate-type-decl := field-type-fields | flag | field-type-decl ;
		
		if (pFieldTypeFields(type, Phase.PARSE)) return true;
		if (pEntityFlag(type, validAggregateTypeFlags)) return true;
		if (pFieldTypeDecl(type, Phase.PARSE)) return true;
		
		return false;
	}
	
	
	private boolean pFieldTypeFields(FieldType type, Phase phase) {
		// field-type-fields := ('fields' | 'field') ( field-type-fields-list | field-type-field-decl ) ;
		if (optionalID("fields") || optionalID("field")) {
			if (pFieldTypeFieldsList(type, phase)) return true;
			if (!pFieldTypeFieldDecl(type, phase)) {
				parseError(lexer.peek(), type, "Expected field-decl!");
			}
			return true;
		}
		return false;
	}
	
	private boolean pFieldTypeFieldsList(FieldType type, Phase phase) {
		// field-type-fields-list := '{' field-type-field-decls '}' [';'] ;
		if (optionalOpen()) {
			if (pFieldTypeFieldDecls(type, phase) && expectClose() && optionalEnd()) return true;
			return true;
		}
		return false;
	}
	
	private boolean pFieldTypeFieldDecls(FieldType type, Phase phase) {
		// field-type-field-decls := field-type-field-decl field-type-field-decls | ;
		while (pFieldTypeFieldDecl(type, phase));
		return true;
	}
	
	private boolean pFieldTypeFieldDecl(FieldType type, Phase phase) {
		// field-type-field-decl := ['transient' | 'meta'] unqualified-field-type-field-decl ;
		if (optionalID("transient")) {
			if (!pUnqualifiedFieldTypeFieldDecl(type, phase, true, false)) {
				parseError(lexer.peek(), type, "Expected field-decl!");
			}
			return true;
		}
		if (optionalID("meta")) {
			if (!pUnqualifiedFieldTypeFieldDecl(type, phase, false, true)) {
				parseError(lexer.peek(), type, "Expected field-decl!");
			}
			return true;
		}
		if (pUnqualifiedFieldTypeFieldDecl(type, phase, false, false)) return true;
		
		return false;
	}
	
	private boolean pUnqualifiedFieldTypeFieldDecl(FieldType parent, Phase phase, boolean transientField, boolean metaField) {
		// unqualified-field-type-field-decl := id<type> [id<name>] ';' ;
		Mark mark = lexer.mark();
		String typeName = optionalID();
		if (typeName != null) {
			String fieldName = optionalID();
			if (fieldName == null) fieldName = "";
			if (expectEnd() && phase == Phase.PARSE) {
				if (parent != null) {
					IEntity type = parent.lookupEntity(typeName);
					if (type instanceof FieldType) {
						FieldType fieldType = (FieldType)type;
						try {
							FieldRef ref = new FieldRef(fieldType, fieldName, -1);
							ref.setTransient(transientField);
							ref.setMeta(metaField);
							parent.fields().add(ref);
						} catch (Exception e) {
							warning(mark.peek(), parent, "Could not add FieldRef!", e);
						}
						return true;
					}
				}
				warning(mark.peek(), parent, typeName + " does not define a FieldType!");
			}
			return true;
		}
		
		return false;
	}
	
	private boolean pFieldTypeDecl(FieldType fieldType, Phase phase) {
		// field-type-decl := required-include | parse | assign | ';' ;
		
		if (pRequiredInclude(fieldType, phase)) return true;
		if (pFieldTypeFunctionCode(fieldType, phase, FunctionType.PARSE)) return true;
		if (pFieldTypeFunctionCode(fieldType, phase, FunctionType.ASSIGN)) return true;
		if (optionalEnd()) return true;
		
		return false;
	}
	
	private boolean pFieldTypeFunctionCode(FieldType fieldType, Phase phase, FunctionType function) {
		// field-type-function-code := function-name text ';' ;
		String id = function.name().toLowerCase();
		if (optionalID(id)) {
			Mark mark = lexer.mark();
			String code = expectText();
			if (code != null && expectEnd() && phase == Phase.PARSE) {
				Function func = fieldType.functions().get(function);
				String oldCode = func.getCode();
				if (oldCode != null && !oldCode.equals(code)) {
					warning(mark.peek(), fieldType, "Code was previously specified as '" + oldCode + "'; overwritten by '" + code + "'.");
				}
				func.setCode(code);
			}
			return true;
		}
		return false;
	}
	
	private boolean pTable(AbstractEntity parent) {
		// table := 'table' id<name> '{' table-decls '}' [';'] ;
		if (optionalID("table")) {
			Mark mark = lexer.mark();
			String name = expectID();
			if (name != null) {
				Table table = new Table(name);
				try {
					parent.addChild(table);
				} catch (Exception e) {
					warning(mark.peek(), parent, "Could not add Table '" + table.getName() + "'", e);
				}
				if (expectOpen() && pTableDecls(table) && expectClose()) {
					optionalEnd();
				}
			}
			return true;
		}
		return false;
	}
	
	private boolean pTableDecls(Table table) {
		// table-decls := table-decl table-decls | ;
		while (pTableDecl(table));
		return true;
	}
	
	@SuppressWarnings("serial")
	private static final Map<String, Set<Flag>> validTableFlags = new HashMap<String, Set<Flag>>() {{
//		put("assign-by-value", EnumSet.of(Flag.ASSIGN_BY_VALUE));
//		put("no-skip-parse", EnumSet.of(Flag.NO_SKIP_PARSE));
//		put("no-skip-assign", EnumSet.of(Flag.NO_SKIP_ASSIGN));
//		EnumSet<Flag> both = EnumSet.of(Flag.NO_SKIP_PARSE, Flag.NO_SKIP_ASSIGN);
//		put("no-skip-assign-parse", both);
//		put("no-skip-parse-assign", both);
	}};

	private boolean pTableDecl(Table table) {
		// table-decl := field-type-fields | index | fieldset | query | table | flag | ';' ;
		
		if (pFieldTypeFields(table, Phase.PARSE)) return true;
//		if (pTableIndex(table)) return true;	// TODO
		if (pFieldset(table)) return true;
//		if (pQuery(table)) return true;			// TODO
		if (pTable(table)) return true;
		if (pEntityFlag(table, validTableFlags)) return true;
		if (optionalEnd()) return true;
		
		return false;
	}
	
	
	private boolean pFieldset(Table parent) {
		// fieldset := 'fieldset' id<name> '{' fieldset-decls '}' [';'] ;
		if (optionalID("fieldset")) {
			Mark mark = lexer.mark();
			String name = expectID();
			if (name != null) {
				AggregateType fieldset = new AggregateType(name);
				try {
					parent.addChild(fieldset);
				} catch (Exception e) {
					warning(mark.peek(), parent, "Could not add Fieldset '" + fieldset.getName() + "'", e);
				}
				if (expectOpen() && pFieldsetDecls(fieldset, parent) && expectClose()) {
					optionalEnd();
				}
			}
			return true;
		}
		return false;
	}
	
	private boolean pFieldsetDecls(AggregateType fieldset, Table parent) {
		// fieldset-decls := fieldset-decl fieldset-decls | ;
		while (pFieldsetDecl(fieldset, parent));
		return true;
	}
	
	@SuppressWarnings("serial")
	private static final Map<String, Set<Flag>> validFieldsetFlags = new HashMap<String, Set<Flag>>() {{
		put("assign-by-value", EnumSet.of(Flag.ASSIGN_BY_VALUE));
		put("no-skip-parse", EnumSet.of(Flag.NO_SKIP_PARSE));
		put("no-skip-assign", EnumSet.of(Flag.NO_SKIP_ASSIGN));
		EnumSet<Flag> both = EnumSet.of(Flag.NO_SKIP_PARSE, Flag.NO_SKIP_ASSIGN);
		put("no-skip-assign-parse", both);
		put("no-skip-parse-assign", both);
	}};
	
	
	private boolean pFieldsetDecl(AggregateType fieldset, Table parent) {
		// fieldset-decl := field-expr-fields | flag | field-type-decl ;
		
		if (pFieldExprFields(fieldset, fieldset.fields(), parent.fields())) return true;
		if (pEntityFlag(fieldset, validFieldsetFlags)) return true;
		if (pFieldTypeDecl(fieldset, Phase.PARSE)) return true;
		
		return false;
	}
	
	
	private boolean pFieldExprFields(FieldType destType, FieldList dest, FieldList src) {
		// field-expr-fields := ( 'field' | 'fields' ) field-expr ;
		
		if (optionalID("field") || optionalID("fields")) {
			if (!pFieldExpr(destType, dest, src)) {
				parseError(lexer.peek(), destType, "expected field-expr!");
			}
			return true;
		}
		return false;
	}

	private boolean pFieldExpr(FieldType destType, FieldList dest, FieldList src) {
		// field-expr := field-expr-list | field-expr-decl ;
		
		if (pFieldExprList(destType, dest, src)) return true;
		if (pFieldExprDecl(destType, dest, src)) return true;
		
		return false;
	}

	private boolean pFieldExprList(FieldType destType, FieldList dest, FieldList src) {
		// field-expr-list := '{' field-expr-decls '}' [';'] ;
		
		if (optionalOpen()) {
			if (pFieldExprDecls(destType, dest, src)) {
				if (expectClose()) {
					optionalEnd();
				}
			} else {
				parseError(lexer.peek(), destType, "expected field-expr-decl!");
			}
			return true;
		}
		return false;
	}

	private boolean pFieldExprDecls(FieldType destType, FieldList dest, FieldList src) {
		// field-expr-decls := field-expr-decl field-expr-decls | ;
		while (pFieldExprDecl(destType, dest, src));
		return true;
	}

	private boolean pFieldExprDecl(FieldType destType, FieldList dest, FieldList src) {
		// field-expr-decl := wildcard-expr | field-name-decl | field-type-field-decl ;
		
		if (pWildcardExpr(dest, src)) return true;
		if (pFieldNameDecl(destType, dest, src)) return true;
		if (destType != null) {
			if (pFieldTypeFieldDecl(destType, Phase.PARSE)) return true;
		}
		
		return false;
	}

	private boolean pFieldNameDecl(FieldType destType, FieldList dest, FieldList src) {
		// field-name-decl := id<fieldname> ';' ;
		
		Mark mark = lexer.mark();
		String fieldName = optionalID();
		if (fieldName != null && optionalEnd()) {
			
			// add field from source to dest
			for (FieldRef ref : src.get()) {
				if (ref.getName().equals(fieldName)) {
					FieldRef newRef = new FieldRef(ref.getType(), ref.getName(), ref.getFirstSqlIndex());
					newRef.setMeta(ref.isMeta());
					newRef.setTransient(ref.isTransient());
					
					dest.add(newRef);
					return true;
				}
			}
			warning(mark.peek(), destType, "No field named '" + fieldName + "' was found!");
			
			return true;
		}
		
		mark.restore();
		return false;
	}

	private boolean pWildcardExpr(FieldList dest, FieldList src) {
		// wildcard-expr := '*' ( except-expr | ';' ) ;
		if (optional(TokenType.WILDCARD, true)) {
			
			FieldList exceptFields = pExceptExpr(src);
			if (exceptFields == null) {
				expectEnd();
			}
			
			for (FieldRef srcRef : src.get()) {
				boolean useRef = true;
				if (exceptFields != null) {
					for (FieldRef exceptRef : exceptFields.get()) {
						if (srcRef.getType() == exceptRef.getType() && srcRef.getName().equals(exceptRef.getName())) {
							useRef = false;
							break;
						}
					}
				}
				if (useRef) {
					FieldRef destRef = new FieldRef(srcRef.getType(), srcRef.getName(), srcRef.getFirstSqlIndex());
					destRef.setMeta(srcRef.isMeta());
					destRef.setTransient(srcRef.isTransient());
					dest.add(destRef);
				}
			}
			
			return true;
		}
		return false;
	}

	private FieldList pExceptExpr(FieldList src) {
		// except-expr := 'except' field-expr ;
		if (optionalID("except")) {
			FieldList dest = new FieldList();
			
			if (!pFieldExpr(null, dest, src)) {
				parseError(lexer.peek(), "expected field-expr!");
			}
			
			return dest;
		}
		return null;
	}	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private boolean pEntityFlag(AbstractEntity entity, Map<String, Set<Flag>> validFlags) {
		// entity-flag := 'flag' ( entity-flag-list | entity-flag-decl ) ;
		if (optionalID("flag") || optionalID("flags")) {
			if (pEntityFlagList(entity, validFlags)) return true;
			
			Set<Flag> flagsToSet = EnumSet.noneOf(Flag.class);
			if (pEntityFlagDecl(entity, flagsToSet, validFlags)) {
				setEntityFlags(entity, flagsToSet);
			} else {
				parseError(lexer.peek(), entity, "Expected flag-decl!");
			}
			return true;
		}
		return false;
	}
	
	private boolean pEntityFlagList(AbstractEntity entity, Map<String, Set<Flag>> validFlags) {
		// entity-flag-list := '{' entity-flag-decls '}' [';'] ;
		if (optionalOpen()) {
			Set<Flag> flagsToSet = EnumSet.noneOf(Flag.class);
			if (pEntityFlagDecls(entity, flagsToSet, validFlags) && expectClose() && optionalEnd()) {
				setEntityFlags(entity, flagsToSet);
			}
			return true;
		}
		return false;
	}
	
	private boolean pEntityFlagDecls(AbstractEntity entity, Set<Flag> flagsToSet, Map<String, Set<Flag>> validFlags) {
		// entity-flag-decls := entity-flag-decl entity-flag-decls | ;
		while (pEntityFlagDecl(entity, flagsToSet, validFlags));
		return true;
	}
	
	private boolean pEntityFlagDecl(AbstractEntity entity, Set<Flag> flagsToSet, Map<String, Set<Flag>> validFlags) {
		// entity-flag-decl := id<flagtype> ';' ;
		Mark mark = lexer.mark();
		String id = optionalID();
		if (id != null) {
			if (expectEnd()) {
				Set<Flag> flagsToAdd = validFlags.get(id);
				if (flagsToAdd == null) {
					warning(mark.peek(), entity, "The flag '" + id + "' is invalid.");
				} else {
					flagsToSet.addAll(flagsToAdd);
				}
			}
			return true;
		}
		return false;
	}
	
	private void setEntityFlags(IEntity entity, Set<Flag> flagsToSet) {
		for (Flag f : flagsToSet) {
			entity.flags().add(f);
		}
	}
	
	private boolean pRequiredInclude(IEntity entity, Phase phase) {
		// required-include := ['implementation'] 'include' ( required-include-list | required-include-name ) ;
		OutputFileType type = OutputFileType.HEADER;
		Mark mark = lexer.mark();
		if (optionalID("implementation"))
			type = OutputFileType.SOURCE;
		
		if (optionalID("include")) {
			if (pRequiredIncludeList(entity, type, phase)) return true;
			if (!pRequiredIncludeName(entity, type, phase)) {
				parseError(lexer.peek(), "Expected filename!");
			}
			return true;
		}
		
		mark.restore();
		return false;
	}
	
	private boolean pRequiredIncludeList(IEntity entity, OutputFileType type, Phase phase) {
		// required-include-list := '{' required-include-names '}' [';'] ;
		if (optionalOpen()) {
			if (pRequiredIncludeNames(entity, type, phase) && expectClose() && optionalEnd()) return true;
			return true;
		}
		return false;
	}
	
	private boolean pRequiredIncludeNames(IEntity entity, OutputFileType type, Phase phase) {
		// required-include-names := required-include-name required-include-names | ;
		while (pRequiredIncludeName(entity, type, phase));
		return true;
	}
	
	private boolean pRequiredIncludeName(IEntity entity, OutputFileType type, Phase phase) {
		// required-include-name := ( text.astring | text ) ';' ;
		if (optional(TokenType.TEXT, false)) {
			Token token = lexer.consume();
			if (expectEnd() && phase == Phase.PARSE) {
				String include = token.getValue();
				if (token.getSubtype() == TokenSubtype.ASTRING) {
					include = "<" + include + ">";
				}
				
				if (!include.isEmpty()) {
					char first = include.charAt(0);
					char last = include.charAt(include.length() - 1);
					if (first != '<' && first != '"' || last != '>' && last != '"') {
						include = "\"" + include + "\"";
					}
				}
				entity.requiredIncludes(type).add(include);
			}
			return true;
		}
		return false;
	}
	
	// consumes an ID token if it is available and has the provided value.
	private boolean optionalID(String id) {
		return optional(TokenType.ID, id, true);
	}
	
	// consumes an ID token if it is available, and returns its value.
	private String optionalID() {
		if (optional(TokenType.ID, false)) {
			return lexer.consume().getValue();
		}
		return null;
	}
	
	// consumes a text token if it is available, and returns its value.
	private String optionalText() {
		if (optional(TokenType.TEXT, false)) {
			return lexer.consume().getValue();
		}
		return null;
	}
	
	// consumes an open token if it is available.
	private boolean optionalOpen() {
		return optional(TokenType.OPEN, true);
	}
	
	// consumes a close token if it is available.
	private boolean optionalClose() {
		return optional(TokenType.CLOSE, true);
	}
	
	// consumes an end token if it is available.
	private boolean optionalEnd() {
		return optional(TokenType.END, true);
	}
	
	// checks if the available token is of the specified type and has a value equal to the provided one.
	// if consume is true, the token will be consumed if it matches.
	private boolean optional(TokenType type, String value, boolean consume) {
		Token t = lexer.peek();
		if (t != null && t.getType() == type && t.getValue().equals(value)) {
			if (consume)
				lexer.consume();
			
			return true;
		}
		return false;
	}
	
	// checks if the available token is of the specified type
	// if consume is true, the token will be consumed if it matches.
	private boolean optional(TokenType type, boolean consume) {
		Token t = lexer.peek();
		if (t != null && t.getType() == type) {
			if (consume)
				lexer.consume();
			
			return true;
		}
		return false;
	}
	
	// checks if the available token is of the specified type and subtype
	// if consume is true, the token will be consumed if it matches.
	@SuppressWarnings("unused")
	private boolean optional(TokenType type, TokenSubtype subtype, boolean consume) {
		Token t = lexer.peek();
		if (t != null && t.getType() == type && t.getSubtype() == subtype) {
			if (consume)
				lexer.consume();
			
			return true;
		}
		return false;
	}
	
	// consumes an ID token if it is available and has the provided value.
	// If not available, triggers an error.
	@SuppressWarnings("unused")
	private boolean expectID(String id) {
		return expect(TokenType.ID, id, true);
	}
	
	// consumes an ID token if it is available and returns its value.
	// If not available, triggers an error and returns null.
	private String expectID() {
		if (expect(TokenType.ID, false)) {
			return lexer.consume().getValue();
		}
		return null;
	}
	
	// consumes a text token if it is available and returns its value.
	// If not available, triggers an error and returns null.
	private String expectText() {
		if (expect(TokenType.TEXT, false)) {
			return lexer.consume().getValue();
		}
		return null;
	}
	
	// consumes an open token if available, and triggers an error if not.
	private boolean expectOpen() {
		return expect(TokenType.OPEN, true);
	}
	
	// consumes an open token if available, and triggers an error if not.
	private boolean expectClose() {
		return expect(TokenType.CLOSE, true);
	}
	
	// consumes an end token if available, and triggers an error if not.
	private boolean expectEnd() {
		return expect(TokenType.END, true);
	}

	// like optional(TokenType, String, boolean) but if the expected token
	// is not available, a PARSE_ERROR will be triggered.
	private boolean expect(TokenType type, String value, boolean consume) {
		Token t = lexer.peek();
		if (t != null && t.getType() == type && t.getValue().equals(value)) {
			if (consume)
				lexer.consume();
			return true;
		} else {
			parseError(t, "Expected " + type.toString() + " '" + value + "'");
			return false;
		}
	}
	
	// like optional(TokenType, boolean) but if the expected token
	// is not available, a PARSE_ERROR will be triggered.
	private boolean expect(TokenType type, boolean consume) {
		Token t = lexer.peek();
		if (t != null && t.getType() == type) {
			if (consume)
				lexer.consume();
			return true;
		} else {
			parseError(t, "Expected " + type.toString());
			return false;
		}
	}
	
	// like optional(TokenType, TokenSubtype, boolean) but if the expected token
	// is not available, a PARSE_ERROR will be triggered.
	@SuppressWarnings("unused")
	private boolean expect(TokenType type, TokenSubtype subtype, boolean consume) {
		Token t = lexer.peek();
		if (t != null && t.getType() == type && t.getSubtype() == subtype) {
			if (consume)
				lexer.consume();
			return true;
		} else {
			parseError(t, type.toString() + "." + subtype.toString());
			return false;
		}
	}
	
	@SuppressWarnings("unused")
	private void warning(Token token, String message) {
		error(ErrorType.WARNING, token, null, message, null);
	}
		
	private void warning(Token token, AbstractEntity entity, String message) {
		error(ErrorType.WARNING, token, entity, message, null);
	}
	
	@SuppressWarnings("unused")
	private void warning(Token token, String message, Exception ex) {
		error(ErrorType.WARNING, token, null, message, ex);
	}
		
	private void warning(Token token, AbstractEntity entity, String message, Exception ex) {
		error(ErrorType.WARNING, token, entity, message, ex);
	}
	
	private void parseError(Token token, String message) {
		error(ErrorType.PARSE_ERROR, token, null, message, null);
	}
		
	private void parseError(Token token, AbstractEntity entity, String message) {
		error(ErrorType.PARSE_ERROR, token, entity, message, null);
	}
	
	private void fatal(ErrorType type, Token token, AbstractEntity entity, String message, RuntimeException ex) {
		error(type, token, entity, message, ex);
		if (ex != null) {
			throw ex;
		} else {
			throw new IllegalStateException(message);
		}
	}
	
	private void error(ErrorType type, Token token, AbstractEntity entity, String message, Exception ex) {
		if (errorHandler != null) {
			StringBuilder sb = new StringBuilder();
			
			sb.append(specPath.getFileName().toString());
			
			if (token != null) {
				sb.append(':');
				sb.append(token.getLine());
				if (token.getEndingLine() != token.getLine()) {
					sb.append('-');
					sb.append(token.getEndingLine());
				}
				sb.append(" (");
				sb.append(token.getType());
				if (token.getSubtype() != TokenSubtype.DEFAULT) {
					sb.append('.');
					sb.append(token.getSubtype());
				}
				if (!token.getValue().isEmpty()) {
					sb.append(" '");
					if (token.getValue().length() > 15) {
						sb.append(token.getValue().substring(0,  12));
						sb.append("'...");
					} else {
						sb.append(token.getValue());
						sb.append("'");
					}
				}
				sb.append(')');
			} else {
				sb.append(":EOF");
			}
			
			if (entity != null) {
				sb.append(": ");
				sb.append(entity.getClass().getSimpleName());
				sb.append(' ');
				sb.append(entity.getFullyQualifiedName());
			}
			
			if (message != null) {
				sb.append(": ");
				sb.append(message);
			}
			errorHandler.handleError(ErrorCategory.PARSER, type, sb.toString(), ex);
		}
	}
	
}
