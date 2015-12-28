package com.magicmoremagic.jbsc.objects.queries;

import java.util.*;

import com.magicmoremagic.jbsc.objects.types.*;

public class FieldList {
	
	private List<FieldRef> fields;
	private List<FieldRef> unmodFields;
	
	public FieldList() {
		fields = new ArrayList<>();
		unmodFields = Collections.unmodifiableList(fields);
	}

	public List<FieldRef> get() {
		return unmodFields;
	}
	
	public FieldList set(List<FieldRef> fields) {
		fields.clear();
		for (FieldRef field : fields) {
			add(field);
		}
		return this;
	}
	
	public FieldList add(FieldRef ref) {
		// TODO verify unique names
		if (ref.getFirstSqlIndex() < 0) {
			ref.setFirstSqlIndex(getNextUnusedSqlIndex());
		}
		fields.add(ref);
		return this;
	}
	
	public FieldList add(FieldType type, String name) {
		return add(new FieldRef(type, name, getNextUnusedSqlIndex()));
	}
	
	public Collection<Integer> getSqlIndices() {
		List<Integer> usedIndices = new ArrayList<Integer>();
		for (FieldRef ref : fields) {
			if (ref.isTransient()) continue;
			for (Integer index : ref.getType().fields().getSqlIndices()) {
				usedIndices.add(index + ref.getFirstSqlIndex());
			}
		}
		return usedIndices;
	}
	
	public String getColName(int sqlIndex) {
		for (FieldRef ref : fields) {
			if (ref.getFirstSqlIndex() > sqlIndex || ref.isTransient())
				continue;
			
			int index = sqlIndex - ref.getFirstSqlIndex();
			String childName = ref.getType().fields().getColName(index);
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
	
	public ColType getColType(int sqlIndex) {
		for (FieldRef ref : fields) {
			if (ref.getFirstSqlIndex() > sqlIndex || ref.isTransient())
				continue;
			
			int index = sqlIndex - ref.getFirstSqlIndex();
			ColType type = ref.getType().fields().getColType(index);
			
			if (type != null)
				return type;
		}
		
		return null;
	}
	
	private int getNextUnusedSqlIndex() {
		Collection<Integer> usedIndices = getSqlIndices();
		for (int i = 0; i < 100; ++i) {
			if (!usedIndices.contains(i))
				return i;
		}
		throw new IllegalStateException("Too many sql indices in FieldList!");
	}
}
