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
	public int visit(Spec spec) {
		return getResult();
	}

	@Override
	public int leave(Spec spec) {
		return getResult();
	}

	@Override
	public int visit(Namespace namespace) {
		return getResult();
	}

	@Override
	public int leave(Namespace namespace) {
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
	public int visit(ColType colType) {
		return getResult();
	}

	@Override
	public int leave(ColType colType) {
		return getResult();
	}

	@Override
	public int visit(ClassType classType) {
		return getResult();
	}

	@Override
	public int leave(ClassType classType) {
		return getResult();
	}
	
	@Override
	public int visit(AggregateType aggregateType) {
		return getResult();
	}
	
	@Override
	public int leave(AggregateType aggregateType) {
		return getResult();
	}

	@Override
	public int visit(Table table) {
		return getResult();
	}

	@Override
	public int leave(Table table) {
		return getResult();
	}
	
	@Override
	public int visit(Query query) {
		return getResult();
	}
	
	@Override
	public int leave(Query query) {
		return getResult();
	}

	@Override
	public int visit(Function function) {
		return getResult();
	}

	@Override
	public int leave(Function function) {
		return getResult();
	}

	@Override
	public int visit(Code code) {
		return getResult();
	}

	@Override
	public int leave(Code code) {
		return getResult();
	}

}
