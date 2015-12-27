package com.magicmoremagic.jbsc.objects.queries;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.magicmoremagic.jbsc.objects.Function;
import com.magicmoremagic.jbsc.objects.base.AbstractContainer;
import com.magicmoremagic.jbsc.visitors.base.IEntityVisitor;

public abstract class Query extends AbstractContainer {
	
	private Map<FieldList, Function> functions;
	private Map<FieldList, Function> unmodFunctions;
	
	public Query() {
		functions = new HashMap<>();
		unmodFunctions = Collections.unmodifiableMap(functions);
	}
	
	
	public Collection<FieldList> getParameterSets() {
		return unmodFunctions.keySet();
	}
	
	public Function getFunction(FieldList parameterSet) {
		return functions.get(parameterSet);
	}
	
	public Query addParameterSet(FieldList parameterSet) {
		functions.put(parameterSet, createFunction(parameterSet));
		return this;
	}
	
	public abstract String getSQL();
	
	protected abstract Function createFunction(FieldList parameterSet);
	

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
