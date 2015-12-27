package com.magicmoremagic.jbsc.visitors.base;

import com.magicmoremagic.jbsc.objects.Code;
import com.magicmoremagic.jbsc.objects.Function;
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
	public int visit(Spec spec) {
		return CONTINUE;
	}

	@Override
	public int leave(Spec spec) {
		return CONTINUE;
	}

	@Override
	public int visit(Namespace namespace) {
		return CONTINUE;
	}

	@Override
	public int leave(Namespace namespace) {
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
	public int visit(ColType colType) {
		return CONTINUE;
	}

	@Override
	public int leave(ColType colType) {
		return CONTINUE;
	}

	@Override
	public int visit(ClassType classType) {
		return CONTINUE;
	}

	@Override
	public int leave(ClassType classType) {
		return CONTINUE;
	}

	@Override
	public int visit(Table table) {
		return CONTINUE;
	}

	@Override
	public int leave(Table table) {
		return CONTINUE;
	}

	@Override
	public int visit(Query query) {
		return CONTINUE;
	}

	@Override
	public int leave(Query query) {
		return CONTINUE;
	}
	
	@Override
	public int visit(Function function) {
		return CONTINUE;
	}

	@Override
	public int leave(Function function) {
		return CONTINUE;
	}
	
	@Override
	public int visit(Code code) {
		return CONTINUE;
	}

	@Override
	public int leave(Code code) {
		return CONTINUE;
	}

}
