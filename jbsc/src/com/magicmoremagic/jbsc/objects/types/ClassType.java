package com.magicmoremagic.jbsc.objects.types;

import java.io.PrintWriter;
import java.util.*;

import com.magicmoremagic.jbsc.OutputFileType;
import com.magicmoremagic.jbsc.objects.*;
import com.magicmoremagic.jbsc.objects.base.EntityFunctions;
import com.magicmoremagic.jbsc.objects.base.EntityIncludes;
import com.magicmoremagic.jbsc.objects.containers.Namespace;
import com.magicmoremagic.jbsc.objects.queries.FieldList;
import com.magicmoremagic.jbsc.util.CodeGenHelper;
import com.magicmoremagic.jbsc.visitors.base.IEntityVisitor;

public class ClassType extends FieldType {

	private String className;
	private FieldList fields;
	private EntityFunctions functions;
	private EntityFunctions.Mediator funcMediator;

	public ClassType(String name) {
		this();
		setName(name);
	}
	
	public ClassType() {
		fields = new FieldList();
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
	public String getCName() {
		return className;
	}
	
	public String getClassName() {
		return className;
	}

	public ClassType setClassName(String className) {
		this.className = className;
		return this;
	}
	
	public boolean isBuiltin() {
		return hasFlag(Flag.BUILTIN, false);
	}
	
	@Override
	public FieldList fields() {
		return fields;
	}
	
	@Override
	public EntityFunctions functions() {
		return functions;
	}
	
	public ClassType setFunctionNamespace(Namespace namespace) {
		Namespace oldNamespace = functions.getNamespace();
		try {
			funcMediator.setNamespace(namespace);
		} catch (Exception e) {
			funcMediator.setNamespace(oldNamespace);
			throw e;
		}
		return this;
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
	
	private abstract class ClassTypeFunction extends Function {
		
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
		public String getDefaultCode() {
			StringBuilder sb = new StringBuilder();
			sb.append("bool fail = false;\n");
			
			for (FieldRef ref : fields.get()) {
				if (ref.isTransient() || ref.isMeta())
					continue;
				
				sb.append("if (!");
				sb.append(ref.getType().functions().get(getFunctionType()).getQualifiedCName(getNamespace()));
				sb.append("(bed, stmt, ");
				if (getFunctionType() == FunctionType.PARSE) {
					sb.append("column + ");
				} else {
					sb.append("parameter + ");
				}
				sb.append(ref.getFirstSqlIndex());
				sb.append(", value");
				if (ref.getName() != null && !ref.getName().isEmpty()) {
					sb.append('.');
					sb.append(ref.getName());
				}
				sb.append(")) fail = true;\n");
			}
			
			sb.append("if (fail) return false;");
			
			return sb.toString();
		}
		
		@Override
		public Collection<Function> getDependencies() {
			if (getCode() != null || fields.get().isEmpty()) {
				Collection<Function> deps = Collections.emptySet();
				return deps;
			}
			
			Collection<Function> deps = new HashSet<>();
			for (FieldRef ref : fields.get()) {
				if (ref.isTransient() || ref.isMeta())
					continue;
				
				Function fieldFunc = ref.getType().functions().get(getFunctionType());
				deps.add(fieldFunc);
				deps.addAll(fieldFunc.getDependencies());
			}
			
			return deps;
		}
		
	}
	
	private class AssignFunction extends ClassTypeFunction {

		@Override
		public FunctionType getFunctionType() {
			return FunctionType.ASSIGN;
		}
		
		@Override
		protected String calculateDefaultName() {
			return "assignType" + CodeGenHelper.toPascalCase(ClassType.this.getName());
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
			writer.print("inline bool ");
			writer.print(getCName());
			writer.print('(');
			writer.print(lookupCName("be.bed.Bed", "::be::bed::Bed"));
			writer.print("& bed, ");
			writer.print(lookupCName("be.bed.CachedStmt", "::be::bed::CachedStmt"));
			writer.print("& stmt, int parameter, ");
			
			if (!isAssignByValue()) {
				writer.print("const ");
			}
			
			if (isBuiltin()) {
				writer.print(className);	
			} else {
				writer.print(ClassType.this.getQualifiedCName(getNamespace()));
			}
			
			if (!isAssignByValue()) {
				writer.print("&");
			}
			writer.print(" value)");
		}
		
	}
	
	private class ParseFunction extends ClassTypeFunction {

		@Override
		public FunctionType getFunctionType() {
			return FunctionType.PARSE;
		}

		@Override
		protected String calculateDefaultName() {
			return "parseType" + CodeGenHelper.toPascalCase(ClassType.this.getName());
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
			writer.print("inline bool ");
			writer.print(getCName());
			writer.print('(');
			
			writer.print(lookupCName("be.bed.Bed", "::be::bed::Bed"));
			writer.print("& bed, ");
			writer.print(lookupCName("be.bed.CachedStmt", "::be::bed::CachedStmt"));
			writer.print("& stmt, int column, ");
			if (isBuiltin()) {
				writer.print(className);	
			} else {
				writer.print(ClassType.this.getQualifiedCName(getNamespace()));
			}
			writer.print("& value)");
		}
		
	}

}
