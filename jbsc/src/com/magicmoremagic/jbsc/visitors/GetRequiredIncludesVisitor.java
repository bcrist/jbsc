package com.magicmoremagic.jbsc.visitors;

import java.util.Set;

import com.magicmoremagic.jbsc.OutputFileType;
import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.objects.containers.Spec;
import com.magicmoremagic.jbsc.visitors.base.AbstractSelectionVisitor;

public class GetRequiredIncludesVisitor extends AbstractSelectionVisitor<String> {
	
	private Set<Entity> entitiesToInclude;
	
	public GetRequiredIncludesVisitor(Set<Entity> entitiesToInclude) {
		this.entitiesToInclude = entitiesToInclude;
	}
	
	@Override
	public int visit(Entity entity) {
		if (entitiesToInclude != null && !entitiesToInclude.contains(entity))
			return CANCEL_THIS | CANCEL_CHILDREN;
		
		selections.addAll(entity.getRequiredIncludes());
		return CONTINUE;
	}
	
	@Override
	public int visit(Spec spec) {
		for (Spec s : spec.getIncludedSpecs()) {
			if (s.getName() == null) continue;
			selections.add('"' + s.getOutputFileName(OutputFileType.HEADER) + '"');
		}
		return CONTINUE;
	}
	
}
