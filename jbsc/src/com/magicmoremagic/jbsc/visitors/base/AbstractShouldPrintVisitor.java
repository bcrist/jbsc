package com.magicmoremagic.jbsc.visitors.base;

import java.util.Set;

import com.magicmoremagic.jbsc.objects.Code;
import com.magicmoremagic.jbsc.objects.Function;
import com.magicmoremagic.jbsc.objects.base.*;
import com.magicmoremagic.jbsc.objects.containers.*;
import com.magicmoremagic.jbsc.objects.queries.Query;
import com.magicmoremagic.jbsc.objects.types.*;

public abstract class AbstractShouldPrintVisitor implements IEntityVisitor {

	protected boolean shouldPrint;
	protected Set<AbstractEntity> entitiesToPrint;

	public AbstractShouldPrintVisitor(Set<AbstractEntity> entitiesToPrint) {
		this.entitiesToPrint = entitiesToPrint;
	}

	public boolean shouldPrint() {
		return shouldPrint;
	}

	private int getResult() {
		return shouldPrint ? STOP : CONTINUE;
	}

	@Override
	public int init(IEntity entity) {
		return CONTINUE;
	}

	@Override
	public int visitAbstractEntity(AbstractEntity entity) {
		if (shouldPrint)
			return STOP;

		if (entitiesToPrint != null && !entitiesToPrint.contains(entity))
			return CANCEL_THIS | CANCEL_CHILDREN;

		return CONTINUE;
	}

	@Override
	public int leaveAbstractEntity(AbstractEntity entity) {
		return getResult();
	}

	@Override
	public int visitAbstractContainer(AbstractContainer container) {
		return getResult();
	}

	@Override
	public int leaveAbstractContainer(AbstractContainer container) {
		return getResult();
	}

	@Override
	public int visitSpec(Spec spec) {
		return getResult();
	}

	@Override
	public int leaveSpec(Spec spec) {
		return getResult();
	}

	@Override
	public int visitNamespace(Namespace namespace) {
		return getResult();
	}

	@Override
	public int leaveNamespace(Namespace namespace) {
		return getResult();
	}

	@Override
	public int visitFieldType(FieldType fieldType) {
		return getResult();
	}

	@Override
	public int leaveFieldType(FieldType fieldType) {
		return getResult();
	}

	@Override
	public int visitColType(ColType colType) {
		return getResult();
	}

	@Override
	public int leaveColType(ColType colType) {
		return getResult();
	}

	@Override
	public int visitClassType(ClassType classType) {
		return getResult();
	}

	@Override
	public int leaveClassType(ClassType classType) {
		return getResult();
	}
	
	@Override
	public int visitAggregateType(AggregateType aggregateType) {
		return getResult();
	}
	
	@Override
	public int leaveAggregateType(AggregateType aggregateType) {
		return getResult();
	}

	@Override
	public int visitTable(Table table) {
		return getResult();
	}

	@Override
	public int leaveTable(Table table) {
		return getResult();
	}
	
	@Override
	public int visitQuery(Query query) {
		return getResult();
	}
	
	@Override
	public int leaveQuery(Query query) {
		return getResult();
	}

	@Override
	public int visitFunction(Function function) {
		return getResult();
	}

	@Override
	public int leaveFunction(Function function) {
		return getResult();
	}

	@Override
	public int visitCode(Code code) {
		return getResult();
	}

	@Override
	public int leaveCode(Code code) {
		return getResult();
	}

}
