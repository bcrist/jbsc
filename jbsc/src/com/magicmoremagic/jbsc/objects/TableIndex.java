package com.magicmoremagic.jbsc.objects;

import com.magicmoremagic.jbsc.objects.base.AbstractEntity;
import com.magicmoremagic.jbsc.objects.queries.FieldList;
import com.magicmoremagic.jbsc.util.CodeGenHelper;
import com.magicmoremagic.jbsc.visitors.base.IEntityVisitor;

public class TableIndex extends AbstractEntity {

	private IndexType type;
	private FieldList fields;
	
	public TableIndex() {
		fields = new FieldList();
		type = IndexType.INDEX;
		setName(CodeGenHelper.getRandomName(32));
	}
	
	public IndexType getType() {
		return type;
	}
	
	public TableIndex setType(IndexType type) {
		this.type = type;
		return this;
	}
	

	public FieldList fields() {
		return fields;
	}

	@Override
	protected int onVisitorVisit(IEntityVisitor visitor) {
		int result = super.onVisitorVisit(visitor);
		if ((result & (IEntityVisitor.CANCEL_THIS | IEntityVisitor.STOP)) == 0) {
			result |= visitor.visitTableIndex(this);
		}
		return result;
	}
	
	@Override
	protected int onVisitorLeave(IEntityVisitor visitor) {
		int result = super.onVisitorLeave(visitor);
		if ((result & (IEntityVisitor.CANCEL_THIS | IEntityVisitor.STOP)) == 0) {
			result |= visitor.leaveTableIndex(this);
		}
		return result;
	}
	
}
