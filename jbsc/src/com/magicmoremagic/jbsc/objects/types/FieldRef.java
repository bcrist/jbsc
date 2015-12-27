
package com.magicmoremagic.jbsc.objects.types;

public class FieldRef {
	
	private String name;
	private int firstSqlIndex;
	private FieldType type;
	private boolean isTransient;	// exists only in code, allows multiple C++ objects to refer to the same field
	private boolean isMeta;			// exists only in schema, allows using classTypes for metadata of other fields
	
	public FieldRef(FieldType type, String name, int firstSqlIndex) {
		this.name = name;
		this.firstSqlIndex = firstSqlIndex;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}

	public FieldRef setName(String name) {
		// TODO verify unique names
		this.name = name;
		return this;
	}

	public int getFirstSqlIndex() {
		return firstSqlIndex;
	}

	public FieldRef setFirstSqlIndex(int firstSqlIndex) {
		this.firstSqlIndex = firstSqlIndex;
		return this;
	}

	public FieldType getType() {
		return type;
	}

	public FieldRef setType(FieldType type) {
		this.type = type;
		return this;
	}
	
	public boolean isTransient() {
		return isTransient;
	}
	
	public FieldRef setTransient(boolean isTransient) {
		this.isTransient = isTransient;
		return this;
	}
	
	public boolean isMeta() {
		return isMeta;
	}
	
	public FieldRef setMeta(boolean isMeta) {
		this.isMeta = isMeta;
		return this;
	}
	
}
