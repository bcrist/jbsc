package com.magicmoremagic.jbsc.visitors.base;

public abstract class AbstractStringBuilderVisitor extends AbstractEntityVisitor {

	protected StringBuilder sb = new StringBuilder();
	
	public void reset() {
		sb.setLength(0);
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}
	
}
