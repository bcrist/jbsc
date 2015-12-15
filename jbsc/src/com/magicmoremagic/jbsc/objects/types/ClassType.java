package com.magicmoremagic.jbsc.objects.types;

import java.io.PrintWriter;
import java.util.*;

import com.magicmoremagic.jbsc.objects.*;
import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.objects.base.EntityContainer;
import com.magicmoremagic.jbsc.objects.containers.Spec;
import com.magicmoremagic.jbsc.util.CodeGenHelper;
import com.magicmoremagic.jbsc.visitors.IEntityVisitor;

public class ClassType extends FieldType {

	private String className;
	private List<FieldRef> fields;
	private Map<FunctionType, ClassTypeFunction> functions;
	
	public ClassType() {
		fields = new ArrayList<>();
		
		functions = new EnumMap<>(FunctionType.class);
		functions.put(FunctionType.ASSIGN, new AssignFunction());
		functions.put(FunctionType.PARSE, new ParseFunction());
	}
	
	public ClassType(String name) {
		this();
		setName(name);
	}
	
	@Override
	protected void trySetName(String newName) {
		String oldName = getName();
		try {
			for (ClassTypeFunction func : functions.values()) {
				func.setName(func.calculateName(newName));
			}
			super.trySetName(newName);
		} catch (Exception e) {
			for (ClassTypeFunction func : functions.values()) {
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
	
	public String getClassName() {
		return className;
	}

	public ClassType setClassName(String className) {
		this.className = className;
		return this;
	}
	
	public boolean isAssignByValue() {
		return hasFlag(Flag.ASSIGN_BY_VALUE, false);
	}
	
	public List<FieldRef> getFields() {
		return fields;
	}
	
	public ClassType setFields(List<FieldRef> fields) {
		this.fields = fields;
		return this;
	}
	
	public ClassType addField(FieldRef ref) {
		// TODO verify unique names
		if (ref.getFirstColumn() < 0) {
			ref.setFirstColumn(getNextUnusedColumnIndex());
		}
		fields.add(ref);
		return this;
	}
	
	public ClassType addField(FieldType type, String name) {
		// TODO verify unique names
		fields.add(new FieldRef(type, name, getNextUnusedColumnIndex()));
		return this;
	}
	
	@Override
	public Function getFunction(FunctionType type) {
		return functions.get(type);
	}

	@Override
	public Collection<Integer> getColumnIndices() {
		List<Integer> usedIndices = new ArrayList<Integer>();
		for (FieldRef ref : fields) {
			for (Integer index : ref.getType().getColumnIndices()) {
				usedIndices.add(index + ref.getFirstColumn());
			}
		}
		return usedIndices;
	}
	
	@Override
	public String getColumnName(int columnIndex) {
		for (FieldRef ref : fields) {
			if (ref.getFirstColumn() > columnIndex || ref.isTransient())
				continue;
			
			int index = columnIndex - ref.getFirstColumn();
			String childName = ref.getType().getColumnName(index);
			if (childName != null) {
				String parentName = ref.getName();
				if (parentName == null)
					parentName = "";
				
				StringBuilder sb = new StringBuilder(parentName.length() + childName.length() + 1);
				sb.append(parentName);
				
				if (!parentName.isEmpty() && !childName.isEmpty())
					sb.append('_');
				
				sb.append(childName);
				
				return sb.toString();
			}
		}
		
		return null;
	}
	
	@Override
	public ColType getColumnType(int columnIndex) {
		for (FieldRef ref : fields) {
			if (ref.getFirstColumn() > columnIndex || ref.isTransient())
				continue;
			
			int index = columnIndex - ref.getFirstColumn();
			ColType type = ref.getType().getColumnType(index);
			
			if (type != null)
				return type;
		}
		
		return null;
	}
	
	private int getNextUnusedColumnIndex() {
		Collection<Integer> usedIndices = getColumnIndices();
		for (int i = 0; i < 100; ++i) {
			if (!usedIndices.contains(i))
				return i;
		}
		throw new IllegalStateException("Too many columns in type " + name);
	}
	
	@Override
	public int acceptVisitor(IEntityVisitor visitor) {
		int result = super.acceptVisitor(visitor);
		if ((result & (IEntityVisitor.CANCEL_THIS | IEntityVisitor.STOP)) == 0) {
			result |= visitor.visit(this);
		}
		return result;
	}
	
	private abstract class ClassTypeFunction extends Function {
		
		@Override
		protected void initRequiredIncludes() {
			super.initRequiredIncludes();
			requiredIncludes.add("\"be/bed/stmt.hpp\"");
			requiredIncludes.add("\"be/bed/bed.hpp\"");
		}
		
		abstract String calculateName(String classTypeName);
		
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
		public String getDefaultCode() {
			StringBuilder sb = new StringBuilder();
			sb.append("bool fail = false;\n");
			
			for (FieldRef ref : fields) {
				if (ref.isTransient())
					continue;
				
				sb.append("if (!");
				sb.append(ref.getType().getFunction(getFunctionType()).getName());
				sb.append("(bed, stmt, ");
				if (getFunctionType() == FunctionType.PARSE) {
					sb.append("column + ");
				} else {
					sb.append("parameter + ");
				}
				sb.append(ref.getFirstColumn());
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
			if (getCode() != null || fields.isEmpty()) {
				Collection<Function> deps = Collections.emptySet();
				return deps;
			}
			
			Collection<Function> deps = new HashSet<>();
			for (FieldRef ref : fields) {
				if (ref.isTransient())
					continue;
				
				Function fieldFunc = ref.getType().getFunction(getFunctionType());
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
		String calculateName(String classTypeName) {
			return "assignType" + CodeGenHelper.toPascalCase(classTypeName);
		}

		@Override
		public Function printDeclaration(PrintWriter writer) {
			writer.print("inline bool ");
			writer.print(getName());
			writer.print("(::be::bed::Bed& bed, ::be::bed::CachedStmt& stmt, int parameter, ");
			if (!isAssignByValue()) {
				writer.print("const ");
			}
			writer.print(className);
			if (!isAssignByValue()) {
				writer.print("&");
			}
			writer.println(" value);");
			return this;
		}

		@Override
		public Function printImplementation(PrintWriter writer) {
			writer.print("inline bool ");
			writer.print(getName());
			writer.print("(::be::bed::Bed& bed, ::be::bed::CachedStmt& stmt, int parameter, ");
			if (!isAssignByValue()) {
				writer.print("const ");
			}
			writer.print(className);
			if (!isAssignByValue()) {
				writer.print("&");
			}
			writer.println(" value)");
			
			printFunctionBodyWithReturnTrue(writer);
			return this;
		}
		
	}
	
	private class ParseFunction extends ClassTypeFunction {

		@Override
		public FunctionType getFunctionType() {
			return FunctionType.PARSE;
		}

		@Override
		String calculateName(String classTypeName) {
			return "parseType" + CodeGenHelper.toPascalCase(classTypeName);
		}

		@Override
		public Function printDeclaration(PrintWriter writer) {
			writer.print("inline bool ");
			writer.print(getName());
			writer.print("(::be::bed::Bed& bed, ::be::bed::CachedStmt& stmt, int column, ");
			writer.print(className);
			writer.println("& value);");
			return this;
		}

		@Override
		public Function printImplementation(PrintWriter writer) {
			writer.print("inline bool ");
			writer.print(getName());
			writer.print("(::be::bed::Bed& bed, ::be::bed::CachedStmt& stmt, int column, ");
			writer.print(className);
			writer.println("& value)");
			
			printFunctionBodyWithReturnTrue(writer);
			return this;
		}
		
	}

}
