package com.magicmoremagic.jbsc.visitors;

import java.util.Set;

import com.magicmoremagic.jbsc.objects.Function;
import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.objects.base.EntityContainer;
import com.magicmoremagic.jbsc.objects.containers.Namespace;
import com.magicmoremagic.jbsc.objects.containers.Spec;
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
	public int visit(Entity entity) {
		if (shouldPrint) return STOP;
		
		if (entitiesToPrint != null && !entitiesToPrint.contains(entity))
			return CANCEL_THIS | CANCEL_CHILDREN;
		
		return CONTINUE;
	}

	@Override
	public int visit(FieldType fieldType) {
		return getResult();
	}

	@Override
	public int visit(ColType function) {
		return getResult();
	}

	@Override
	public int visit(ClassType function) {
		return getResult();
	}

	@Override
	public int visit(Function function) {
		return getResult();
	}

	@Override
	public int visit(EntityContainer container) {
		return getResult();
	}

	@Override
	public int visit(Spec spec) {
		return getResult();
	}

	@Override
	public int visit(Namespace namespace) {
		return getResult();
	}

	@Override
	public int leave(EntityContainer container) {
		return getResult();
	}

	@Override
	public int leave(Spec spec) {
		return getResult();
	}

	@Override
	public int leave(Namespace namespace) {
		return getResult();
	}
	

}
