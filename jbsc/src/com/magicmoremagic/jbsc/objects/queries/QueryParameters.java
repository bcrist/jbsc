package com.magicmoremagic.jbsc.objects.queries;

import java.util.*;

import com.magicmoremagic.jbsc.objects.types.*;

public class QueryParameters {
	
	private List<FieldRef> fields;
	
	public QueryParameters() {
		fields = new ArrayList<>();
	}
	

	public List<FieldRef> getFields() {
		return fields;
	}
	
	public QueryParameters setFields(List<FieldRef> fields) {
		this.fields = fields;
		return this;
	}
	
	public QueryParameters addField(FieldRef ref) {
		// TODO verify unique names
		if (ref.getFirstColumn() < 0) {
			ref.setFirstColumn(getNextUnusedParameterIndex());
		}
		fields.add(ref);
		return this;
	}
	
	public QueryParameters addField(FieldType type, String name) {
		// TODO verify unique names
		fields.add(new FieldRef(type, name, getNextUnusedParameterIndex()));
		return this;
	}
	
	public Collection<Integer> getParameterIndices() {
		List<Integer> usedIndices = new ArrayList<Integer>();
		for (FieldRef ref : fields) {
			if (ref.isTransient()) continue;
			for (Integer index : ref.getType().getColumnIndices()) {
				usedIndices.add(index + ref.getFirstColumn());
			}
		}
		return usedIndices;
	}
	
	public String getParameterName(int parameterIndex) {
		for (FieldRef ref : fields) {
			if (ref.getFirstColumn() > parameterIndex || ref.isTransient())
				continue;
			
			int index = parameterIndex - ref.getFirstColumn();
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
	
	public ColType getParameterType(int parameterIndex) {
		for (FieldRef ref : fields) {
			if (ref.getFirstColumn() > parameterIndex || ref.isTransient())
				continue;
			
			int index = parameterIndex - ref.getFirstColumn();
			ColType type = ref.getType().getColumnType(index);
			
			if (type != null)
				return type;
		}
		
		return null;
	}
	
	private int getNextUnusedParameterIndex() {
		Collection<Integer> usedIndices = getParameterIndices();
		for (int i = 1; i < 100; ++i) {
			if (!usedIndices.contains(i))
				return i;
		}
		throw new IllegalStateException("Too many parameters in query!");
	}
}
