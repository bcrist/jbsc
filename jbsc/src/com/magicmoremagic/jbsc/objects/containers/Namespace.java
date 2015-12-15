package com.magicmoremagic.jbsc.objects.containers;

import com.magicmoremagic.jbsc.objects.base.EntityContainer;
import com.magicmoremagic.jbsc.visitors.IEntityVisitor;

public class Namespace extends EntityContainer {

	public Namespace() { }
	
	public Namespace(String name) {
		this();
		setName(name);
	}
	
	@Override
	protected int acceptVisitorEnter(IEntityVisitor visitor) {
		int result = super.acceptVisitorEnter(visitor);
		if ((result & (IEntityVisitor.CANCEL_THIS | IEntityVisitor.STOP)) == 0) {
			result |= visitor.visit(this);
		}
		return result;
	}
	
	@Override
	protected int acceptVisitorLeave(IEntityVisitor visitor) {
		int result = super.acceptVisitorLeave(visitor);
		if ((result & (IEntityVisitor.CANCEL_THIS | IEntityVisitor.STOP)) == 0) {
			result |= visitor.leave(this);
		}
		return result;
	}
	
}
