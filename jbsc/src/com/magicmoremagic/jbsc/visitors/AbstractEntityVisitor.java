package com.magicmoremagic.jbsc.visitors;

import com.magicmoremagic.jbsc.objects.Function;
import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.objects.base.EntityContainer;
import com.magicmoremagic.jbsc.objects.containers.Namespace;
import com.magicmoremagic.jbsc.objects.containers.Spec;
import com.magicmoremagic.jbsc.objects.types.*;

public abstract class AbstractEntityVisitor implements IEntityVisitor {

	@Override
	public int visit(Entity entity) {
		return CONTINUE;
	}

	@Override
	public int visit(FieldType fieldType) {
		return CONTINUE;
	}

	@Override
	public int visit(ColType function) {
		return CONTINUE;
	}

	@Override
	public int visit(ClassType function) {
		return CONTINUE;
	}

	@Override
	public int visit(Function function) {
		return CONTINUE;
	}

	@Override
	public int visit(EntityContainer container) {
		return CONTINUE;
	}

	@Override
	public int visit(Spec spec) {
		return CONTINUE;
	}

	@Override
	public int visit(Namespace namespace) {
		return CONTINUE;
	}

	@Override
	public int leave(EntityContainer container) {
		return CONTINUE;
	}

	@Override
	public int leave(Spec spec) {
		return CONTINUE;
	}

	@Override
	public int leave(Namespace namespace) {
		return CONTINUE;
	}

}
