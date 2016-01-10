package com.magicmoremagic.jbsc.objects.queries;

import com.magicmoremagic.jbsc.objects.Function;
import com.magicmoremagic.jbsc.objects.base.AbstractContainer;
import com.magicmoremagic.jbsc.visitors.base.IEntityVisitor;

public abstract class Query extends AbstractContainer {
	
	protected FieldList parameters;
	protected FieldList results;
	protected Function function;
	
	public Query() {
		parameters = new FieldList();
		results = new FieldList();
	}
	
	public FieldList getParameters() {
		return parameters;
	}
	
	public FieldList getResults() {
		return parameters;
	}
	
	public Function getFunction() {
		return function;
	}
	
	public abstract String getSQL();
	
	protected abstract Function createFunction(FieldList parameterSet);
	
	@Override
	protected int onVisitorVisit(IEntityVisitor visitor) {
		int result = super.onVisitorVisit(visitor);
		if ((result & (IEntityVisitor.CANCEL_THIS | IEntityVisitor.STOP)) == 0) {
			result |= visitor.visitQuery(this);
		}
		return result;
	}
	
	@Override
	protected int onVisitorLeave(IEntityVisitor visitor) {
		int result = super.onVisitorLeave(visitor);
		if ((result & (IEntityVisitor.CANCEL_THIS | IEntityVisitor.STOP)) == 0) {
			result |= visitor.leaveQuery(this);
		}
		return result;
	}

}
