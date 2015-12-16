package com.magicmoremagic.jbsc.objects.types;

import java.util.Collection;

import com.magicmoremagic.jbsc.objects.Function;
import com.magicmoremagic.jbsc.objects.FunctionType;
import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.visitors.IEntityVisitor;

public abstract class FieldType extends Entity {

	public abstract Function getFunction(FunctionType type);
	
	public abstract Collection<Integer> getColumnIndices();
	public abstract String getColumnName(int columnIndex);
	public abstract ColType getColumnType(int columnIndex);
	
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
