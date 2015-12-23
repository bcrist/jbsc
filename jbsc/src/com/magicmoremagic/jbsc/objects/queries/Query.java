package com.magicmoremagic.jbsc.objects.queries;

import java.util.*;

import com.magicmoremagic.jbsc.objects.Function;
import com.magicmoremagic.jbsc.objects.base.EntityContainer;
import com.magicmoremagic.jbsc.visitors.base.IEntityVisitor;

public abstract class Query extends EntityContainer {
	
	private Map<QueryParameters, Function> functions;
	private Collection<QueryParameters> unmodParameterSets;
	
	public Query() {
		functions = new HashMap<>();
		unmodParameterSets = Collections.unmodifiableCollection(functions.keySet());
	}
	
	
	public Collection<QueryParameters> getParameterSets() {
		return unmodParameterSets;
	}
	
	public Function getFunction(QueryParameters parameterSet) {
		return functions.get(parameterSet);
	}
	
	public Query addParameterSet(QueryParameters parameterSet) {
		functions.put(parameterSet,  getFunctionForNewParameterSet(parameterSet));
		return this;
	}
	
	public abstract String getSQL();
	
	
	
	
	

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
