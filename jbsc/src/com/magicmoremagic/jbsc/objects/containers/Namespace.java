package com.magicmoremagic.jbsc.objects.containers;

import com.magicmoremagic.jbsc.objects.base.AbstractContainer;
import com.magicmoremagic.jbsc.objects.base.EntityFlags;
import com.magicmoremagic.jbsc.visitors.base.IEntityVisitor;

public class Namespace extends AbstractContainer {
	
	public Namespace() {
		super(new EntityFlags());
	}
	
	public Namespace(String name) {
		this();
		setName(name);
	}
	
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
