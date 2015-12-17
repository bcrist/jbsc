package com.magicmoremagic.jbsc.visitors.base;

import java.util.Set;

import com.magicmoremagic.jbsc.objects.Code;
import com.magicmoremagic.jbsc.objects.Function;
import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.objects.base.EntityContainer;
import com.magicmoremagic.jbsc.objects.containers.*;
import com.magicmoremagic.jbsc.objects.types.*;

public abstract class AbstractShouldPrintVisitor implements IEntityVisitor {

	protected boolean shouldPrint;
	protected Set<Entity> entitiesToPrint;

	public AbstractShouldPrintVisitor(Set<Entity> entitiesToPrint) {
		this.entitiesToPrint = entitiesToPrint;
	}

	public boolean shouldPrint() {
		return shouldPrint;
	}

	private int getResult() {
		return shouldPrint ? STOP : CONTINUE;
	}

	@Override
	public int init(Entity entity) {
		return CONTINUE;
	}

	@Override
	public int visit(Entity entity) {
		if (shouldPrint)
			return STOP;

		if (entitiesToPrint != null && !entitiesToPrint.contains(entity))
			return CANCEL_THIS | CANCEL_CHILDREN;

		return CONTINUE;
	}

	@Override
	public int leave(Entity entity) {
		return getResult();
	}

	@Override
	public int visit(EntityContainer container) {
		return getResult();
	}

	@Override
	public int leave(EntityContainer container) {
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
	public int visit(FieldType fieldType) {
		return getResult();
	}

	@Override
	public int leave(FieldType fieldType) {
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
	public int visit(Table table) {
		return getResult();
	}

	@Override
	public int leave(Table table) {
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
