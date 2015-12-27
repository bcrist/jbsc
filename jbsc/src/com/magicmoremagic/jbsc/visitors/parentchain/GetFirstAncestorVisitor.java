package com.magicmoremagic.jbsc.visitors.parentchain;

import com.magicmoremagic.jbsc.objects.base.AbstractEntity;
import com.magicmoremagic.jbsc.visitors.base.AbstractEntityVisitor;

public class GetFirstAncestorVisitor<T> extends AbstractEntityVisitor {

	private Class<T> ancestorClass;
	private T ancestor;
	
	public GetFirstAncestorVisitor(Class<T> ancestorClass) {
		this.ancestorClass = ancestorClass;
	}
	
	public T getAncestor() {
		return ancestor;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public int visitAbstractEntity(AbstractEntity entity) {
		if (ancestorClass.isInstance(entity)) {
			ancestor = (T)entity;
			return STOP;
		}
		
		return CANCEL_THIS;
	}
	
	@Override
	public int leaveAbstractEntity(AbstractEntity entity) {
		return STOP;
	}
	
}
