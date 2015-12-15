
package com.magicmoremagic.jbsc.objects.types;

public class FieldRef {
	
	private String name;
	private int firstColumn;
	private FieldType type;
	private boolean isTransient;
	
	public FieldRef(FieldType type, String name, int firstColumn) {
		this.name = name;
		this.firstColumn = firstColumn;
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

	public int getFirstColumn() {
		return firstColumn;
	}

	public FieldRef setFirstColumn(int firstColumn) {
		this.firstColumn = firstColumn;
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
	
}
