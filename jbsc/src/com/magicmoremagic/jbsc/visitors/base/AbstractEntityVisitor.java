package com.magicmoremagic.jbsc.visitors.base;

import com.magicmoremagic.jbsc.objects.*;
import com.magicmoremagic.jbsc.objects.base.*;
import com.magicmoremagic.jbsc.objects.containers.*;
import com.magicmoremagic.jbsc.objects.queries.Query;
import com.magicmoremagic.jbsc.objects.types.*;

public abstract class AbstractEntityVisitor implements IEntityVisitor {

	@Override
	public int init(IEntity entity) {
		return CONTINUE;
	}
	
	@Override
	public int visitAbstractEntity(AbstractEntity entity) {
		return CONTINUE;
	}

	@Override
	public int leaveAbstractEntity(AbstractEntity entity) {
		return CONTINUE;
	}

	@Override
	public int visitAbstractContainer(AbstractContainer container) {
		return CONTINUE;
	}

	@Override
	public int leaveAbstractContainer(AbstractContainer container) {
		return CONTINUE;
	}

	@Override
	public int visitSpec(Spec spec) {
		return CONTINUE;
	}

	@Override
	public int leaveSpec(Spec spec) {
		return CONTINUE;
	}

	@Override
	public int visitNamespace(Namespace namespace) {
		return CONTINUE;
	}

	@Override
	public int leaveNamespace(Namespace namespace) {
		return CONTINUE;
	}

	@Override
	public int visitFieldType(FieldType fieldType) {
		return CONTINUE;
	}

	@Override
	public int leaveFieldType(FieldType fieldType) {
		return CONTINUE;
	}

	@Override
	public int visitColType(ColType colType) {
		return CONTINUE;
	}

	@Override
	public int leaveColType(ColType colType) {
		return CONTINUE;
	}

	@Override
	public int visitClassType(ClassType classType) {
		return CONTINUE;
	}

	@Override
	public int leaveClassType(ClassType classType) {
		return CONTINUE;
	}
	
	@Override
	public int visitAggregateType(AggregateType aggregateType) {
		return CONTINUE;
	}
	
	@Override
	public int leaveAggregateType(AggregateType aggregateType) {
		return CONTINUE;
	}

	@Override
	public int visitTable(Table table) {
		return CONTINUE;
	}

	@Override
	public int leaveTable(Table table) {
		return CONTINUE;
	}
	
	@Override
	public int visitTableIndex(TableIndex table) {
		return CONTINUE;
	}

	@Override
	public int leaveTableIndex(TableIndex table) {
		return CONTINUE;
	}

	@Override
	public int visitQuery(Query query) {
		return CONTINUE;
	}

	@Override
	public int leaveQuery(Query query) {
		return CONTINUE;
	}
	
	@Override
	public int visitFunction(Function function) {
		return CONTINUE;
	}

	@Override
	public int leaveFunction(Function function) {
		return CONTINUE;
	}
	
	@Override
	public int visitCode(Code code) {
		return CONTINUE;
	}

	@Override
	public int leaveCode(Code code) {
		return CONTINUE;
	}

}
