package com.magicmoremagic.jbsc.visitors;

import java.util.Set;

import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.visitors.base.AbstractSelectionVisitor;

public class GetImplementationIncludesVisitor extends AbstractSelectionVisitor<String> {

	private Set<Entity> entitiesToInclude;
	
	public GetImplementationIncludesVisitor(Set<Entity> entitiesToInclude) {
		this.entitiesToInclude = entitiesToInclude;
	}
	
	@Override
	public int visit(Entity entity) {
		if (entitiesToInclude != null && !entitiesToInclude.contains(entity))
			return CANCEL_THIS | CANCEL_CHILDREN;
		
		selections.addAll(entity.getImplementationIncludes());
		return CONTINUE;
	}
	
}
