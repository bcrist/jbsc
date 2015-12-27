package com.magicmoremagic.jbsc.objects.types;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import com.magicmoremagic.jbsc.OutputFileType;
import com.magicmoremagic.jbsc.objects.Function;
import com.magicmoremagic.jbsc.objects.FunctionType;
import com.magicmoremagic.jbsc.objects.base.AbstractEntity;
import com.magicmoremagic.jbsc.objects.base.EntityFunctions;
import com.magicmoremagic.jbsc.objects.base.EntityIncludes;
import com.magicmoremagic.jbsc.objects.queries.FieldList;
import com.magicmoremagic.jbsc.util.CodeGenHelper;
import com.magicmoremagic.jbsc.visitors.base.IEntityVisitor;

public class ColType extends FieldType {
	
	private static final Collection<Integer> SQL_INDICES = Collections.unmodifiableCollection(Arrays.asList(0));
	
	private FieldList fields = new FieldList() {
		
		@Override
		public FieldList set(java.util.List<FieldRef> fields) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public FieldList add(FieldRef ref) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public FieldList add(FieldType type, String name) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public String getColName(int sqlIndex) {
			return sqlIndex == 0 ? "" : null;
		}
		
		@Override
		public ColType getColType(int sqlIndex) {
			return sqlIndex == 0 ? ColType.this : null;
		}
		
		@Override
		public Collection<Integer> getSqlIndices() {
			return SQL_INDICES;
		}
		
		{
			FieldRef ref = new FieldRef(ColType.this, "", 0);
			ref.setMeta(true);
			super.add(ref);
		}
	};
	
	private String affinity;
	private String constraints;
	private EntityFunctions functions;
	private EntityFunctions.Mediator funcMediator;
	
	public ColType(String name) {
		this();
		setName(name);
	}
	
	public ColType() {
		funcMediator = new EntityFunctions.Mediator();
		functions = new EntityFunctions(funcMediator);
		funcMediator.put(FunctionType.ASSIGN, new AssignFunction());
		funcMediator.put(FunctionType.PARSE, new ParseFunction());
	}

	@Override
	protected void onNameChanged(String oldName) {
		super.onNameChanged(oldName);
		funcMediator.calculateNames();
	}
	
	@Override
	protected void onParentChanged(AbstractEntity oldParent) {
		super.onParentChanged(oldParent);
		funcMediator.setNamespace(getNamespace());
	}
	
	@Override
	public String getCName() {
		return CodeGenHelper.toPascalCase(getName());
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
	public FieldList fields() {
		return fields;
	}
	
	@Override
	public EntityFunctions functions() {
		return functions;
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
		public EntityIncludes requiredIncludes(OutputFileType type) {
			switch (type) {
			case HEADER:
				return EntityIncludes.BED_STMT_INCLUDES;
			default:
				return super.requiredIncludes(type);
			}
		}
		
		@Override
		public boolean isImplementationInline() {
			return true;
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
		protected String calculateDefaultName() {
			return "assignCol" + ColType.this.getCName();
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
			writer.print(getCName());
			writer.print('(');
			
			writer.print(lookupCName("be.bed.Bed", "::be::bed::Bed"));
			writer.print("& bed, ");
			writer.print(lookupCName("be.bed.CachedStmt", "::be::bed::CachedStmt"));			
			writer.print("& stmt, int parameter, ");
			
			if (!isAssignByValue()) {
				writer.print("const ");
			}
			
			writer.print("T");	
			
			if (!isAssignByValue()) {
				writer.print("&");
			}
			writer.print(" value)");
		}
		
	}
	
	private class ParseFunction extends ColTypeFunction {

		@Override
		public FunctionType getFunctionType() {
			return FunctionType.PARSE;
		}

		@Override
		protected String calculateDefaultName() {
			return "parseCol" + ColType.this.getCName();
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
			writer.print(getCName());
			writer.print('(');
			writer.print(lookupCName("be.bed.Bed", "::be::bed::Bed"));
			writer.print("& bed, ");
			writer.print(lookupCName("be.bed.CachedStmt", "::be::bed::CachedStmt"));
			writer.print("& stmt, int column, T& value)");
		}
		
	}
	
}
