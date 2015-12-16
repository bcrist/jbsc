package com.magicmoremagic.jbsc.visitors;

import com.magicmoremagic.jbsc.objects.Function;
import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.objects.base.EntityContainer;
import com.magicmoremagic.jbsc.objects.containers.*;
import com.magicmoremagic.jbsc.objects.types.*;

public abstract class AbstractEntityVisitor implements IEntityVisitor {

	@Override
	public int visit(Entity entity) {
		return CONTINUE;
	}

	@Override
	public int leave(Entity entity) {
		return CONTINUE;
	}

	@Override
	public int visit(EntityContainer container) {
		return CONTINUE;
	}

	@Override
	public int leave(EntityContainer container) {
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
	public int visit(FieldType fieldType) {
		return CONTINUE;
	}

	@Override
	public int leave(FieldType fieldType) {
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
	public int visit(Function function) {
		return CONTINUE;
	}

	@Override
	public int leave(Function function) {
		return CONTINUE;
	}

}
