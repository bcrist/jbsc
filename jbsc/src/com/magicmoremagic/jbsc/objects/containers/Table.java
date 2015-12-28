package com.magicmoremagic.jbsc.objects.containers;

import com.magicmoremagic.jbsc.objects.types.AggregateType;
import com.magicmoremagic.jbsc.visitors.base.IEntityVisitor;

public class Table extends AggregateType {

	public Table(String name) {
		super(name);
	}
	
	public Table() {
		super();
	}
	
	@Override
	protected int onVisitorVisit(IEntityVisitor visitor) {
		int result = super.onVisitorVisit(visitor);
		if ((result & (IEntityVisitor.CANCEL_THIS | IEntityVisitor.STOP)) == 0) {
			result |= visitor.visitTable(this);
		}
		return result;
	}
	
	@Override
	protected int onVisitorLeave(IEntityVisitor visitor) {
		int result = super.onVisitorLeave(visitor);
		if ((result & (IEntityVisitor.CANCEL_THIS | IEntityVisitor.STOP)) == 0) {
			result |= visitor.leaveTable(this);
		}
		return result;
	}
	
}
