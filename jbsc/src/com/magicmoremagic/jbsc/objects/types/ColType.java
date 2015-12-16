package com.magicmoremagic.jbsc.objects.types;

import java.io.PrintWriter;
import java.util.*;

import com.magicmoremagic.jbsc.objects.Function;
import com.magicmoremagic.jbsc.objects.FunctionType;
import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.objects.base.EntityContainer;
import com.magicmoremagic.jbsc.objects.containers.Spec;
import com.magicmoremagic.jbsc.util.CodeGenHelper;
import com.magicmoremagic.jbsc.visitors.IEntityVisitor;

public class ColType extends FieldType {

	private static final List<Integer> columnIndex = Collections.unmodifiableList(Arrays.asList(0));
	
	private String affinity;
	private String constraints;
	private Map<FunctionType, ColTypeFunction> functions;
	
	public ColType(String name) {
		this();
		setName(name);
	}
	
	public ColType() {
		functions = new EnumMap<>(FunctionType.class);
		functions.put(FunctionType.ASSIGN, new AssignFunction());
		functions.put(FunctionType.PARSE, new ParseFunction());
	}

	@Override
	protected void trySetName(String newName) {
		String oldName = getName();
		try {
			for (ColTypeFunction func : functions.values()) {
				func.setName(func.calculateName(newName));
			}
			super.trySetName(newName);
		} catch (Exception e) {
			for (ColTypeFunction func : functions.values()) {
				func.setName(func.calculateName(oldName));
			}
			throw e;
		}
	}
	
	@Override
	protected void trySetParent(EntityContainer newParent) {
		EntityContainer oldParent = getParent();
		try {
			for (Function func : functions.values()) {
				newParent.addChild(func);
			}
			super.trySetParent(newParent);
		} catch (Exception e) {
			for (Function func : functions.values()) {
				oldParent.addChild(func);
			}
			throw e;
		}
	}
	
	@Override
	public String getUnqualifiedCodeName() {
		return CodeGenHelper.toPascalCase(name);
	}
	
	public String getAffinity() {
		return affinity;
	}

	public ColType setAffinity(String affinity) {
		this.affinity = affinity;
		return this;
	}
	
	public String getConstraints() {
		return constraints;
	}

	public ColType setConstraints(String constraints) {
		this.constraints = constraints;
		return this;
	}

	@Override
	public Function getFunction(FunctionType type) {
		return functions.get(type);
	}

	@Override
	public Collection<Integer> getColumnIndices() {
		return columnIndex;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columnIndex == 0 ? "" : null;
	}
	
	@Override
	public ColType getColumnType(int columnIndex) {
		return columnIndex == 0 ? this : null;
	}
	
	@Override
	protected int onVisitorVisit(IEntityVisitor visitor) {
		int result = super.onVisitorVisit(visitor);
		if ((result & (IEntityVisitor.CANCEL_THIS | IEntityVisitor.STOP)) == 0) {
			result |= visitor.visit(this);
		}
		return result;
	}
	
	@Override
	protected int onVisitorLeave(IEntityVisitor visitor) {
		int result = super.onVisitorLeave(visitor);
		if ((result & (IEntityVisitor.CANCEL_THIS | IEntityVisitor.STOP)) == 0) {
			result |= visitor.leave(this);
		}
		return result;
	}

	private abstract class ColTypeFunction extends Function {
		
		@Override
		protected void initRequiredIncludes() {
			super.initRequiredIncludes();
			requiredIncludes.add("\"be/bed/stmt.hpp\"");
			requiredIncludes.add("\"be/bed/bed.hpp\"");
		}
		
		abstract String calculateName(String colTypeName);
		
		@Override
		public boolean isImplementationInline() {
			return true;
		}
		
		@Override
		public Spec getSpec() {
			return spec;
		}
		
		@Override
		public Entity setSpec(Spec spec) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Collection<Function> getDependencies() {
			Collection<Function> deps = Collections.emptySet();
			return deps;
		}
		
	}
	
	private class AssignFunction extends ColTypeFunction {

		@Override
		public FunctionType getFunctionType() {
			return FunctionType.ASSIGN;
		}

		@Override
		String calculateName(String colTypeName) {
			return "assignCol" + CodeGenHelper.toPascalCase(colTypeName);
		}
		
		@Override
		public String getDefaultCode() {
			return "stmt->bind(parameter, value);";
		}

		@Override
		public Function printDeclaration(PrintWriter writer) {
			printSignature(writer);
			writer.println(";");
			return this;
		}

		@Override
		public Function printImplementation(PrintWriter writer) {
			printSignature(writer);
			writer.println();
			printFunctionBodyWithReturnTrue(writer);
			return this;
		}
		
		private void printSignature(PrintWriter writer) {
			writer.println("template <typename T>");
			writer.print("bool ");
			writer.print(getUnqualifiedCodeName());
			writer.print('(');
			
			String qualifiedBedName;
			try {
				qualifiedBedName = getParent().lookupName("be.bed.Bed").getCodeName(getNamespace());
			} catch (NullPointerException e) {
				qualifiedBedName = "::be::bed::Bed";
			}
			
			writer.print(qualifiedBedName);
			writer.print("& bed, ");
			
			String qualifiedCachedStmtName;
			try {
				qualifiedCachedStmtName = getParent().lookupName("be.bed.CachedStmt").getCodeName(getNamespace());
			} catch (NullPointerException e) {
				qualifiedCachedStmtName = "::be::bed::CachedStmt";
			}

			writer.print(qualifiedCachedStmtName);
			writer.print("& stmt, int parameter, const T& value)");
		}
		
	}
	
	private class ParseFunction extends ColTypeFunction {

		@Override
		public FunctionType getFunctionType() {
			return FunctionType.PARSE;
		}

		@Override
		String calculateName(String colTypeName) {
			return "parseCol" + CodeGenHelper.toPascalCase(colTypeName);
		}
		
		@Override
		public String getDefaultCode() {
			String lcAffinity = affinity == null ? "" : affinity.toLowerCase(Locale.US);
			if (lcAffinity.contains("int")) {
				return "value = static_cast<T>(stmt->getInt(column));";
			} else if (lcAffinity.contains("char") || lcAffinity.contains("text") || lcAffinity.contains("clob")) {
				return "value = static_cast<T>(stmt->getTextString(column));";
			} else if (lcAffinity.contains("blob") || lcAffinity.isEmpty()) {
				return "value = static_cast<T>(stmt->getBlobCopy(column));";
			} else {
				return "value = static_cast<T>(stmt->getDouble(column));";
			}
		}

		@Override
		public Function printDeclaration(PrintWriter writer) {
			printSignature(writer);
			writer.println(";");
			return this;
		}

		@Override
		public Function printImplementation(PrintWriter writer) {
			printSignature(writer);
			writer.println();
			printFunctionBodyWithReturnTrue(writer);
			return this;
		}
	
		private void printSignature(PrintWriter writer) {
			writer.println("template <typename T>");
			writer.print("bool ");
			writer.print(getUnqualifiedCodeName());
			writer.print('(');
			
			String qualifiedBedName;
			try {
				qualifiedBedName = getParent().lookupName("be.bed.Bed").getCodeName(getNamespace());
			} catch (NullPointerException e) {
				qualifiedBedName = "::be::bed::Bed";
			}
			
			writer.print(qualifiedBedName);
			writer.print("& bed, ");
			
			String qualifiedCachedStmtName;
			try {
				qualifiedCachedStmtName = getParent().lookupName("be.bed.CachedStmt").getCodeName(getNamespace());
			} catch (NullPointerException e) {
				qualifiedCachedStmtName = "::be::bed::CachedStmt";
			}

			writer.print(qualifiedCachedStmtName);
			writer.print("& stmt, int column, const T& value)");
		}
		
	}
	
}
