package com.magicmoremagic.jbsc.objects.containers;

import java.util.*;

import com.magicmoremagic.jbsc.objects.base.EntityContainer;
import com.magicmoremagic.jbsc.objects.types.*;
import com.magicmoremagic.jbsc.util.CodeGenHelper;
import com.magicmoremagic.jbsc.visitors.IEntityVisitor;

public class Table extends EntityContainer {

	private List<FieldRef> fields;
	
	public Table() {
		fields = new ArrayList<>();
	}
	
	public Table(String name) {
		this();
		setName(name);
	}
	
	@Override
	public String getUnqualifiedCodeName() {
		return CodeGenHelper.toPascalCase(name);
	}
	
	public List<FieldRef> getFields() {
		return fields;
	}
	
	public Table setFields(List<FieldRef> fields) {
		this.fields = fields;
		return this;
	}
	
	public Table addField(FieldRef ref) {
		// TODO verify unique names
		if (ref.getFirstColumn() < 0) {
			ref.setFirstColumn(getNextUnusedColumnIndex());
		}
		fields.add(ref);
		return this;
	}
	
	public Table addField(FieldType type, String name) {
		// TODO verify unique names
		fields.add(new FieldRef(type, name, getNextUnusedColumnIndex()));
		return this;
	}
	
	public Collection<Integer> getColumnIndices() {
		List<Integer> usedIndices = new ArrayList<Integer>();
		for (FieldRef ref : fields) {
			for (Integer index : ref.getType().getColumnIndices()) {
				usedIndices.add(index + ref.getFirstColumn());
			}
		}
		return usedIndices;
	}
	
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
		throw new IllegalStateException("Too many columns in table " + name);
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

}
