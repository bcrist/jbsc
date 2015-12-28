package com.magicmoremagic.jbsc.visitors;

import java.util.Set;

import com.magicmoremagic.jbsc.OutputFileType;
import com.magicmoremagic.jbsc.objects.base.AbstractEntity;
import com.magicmoremagic.jbsc.objects.containers.Spec;
import com.magicmoremagic.jbsc.visitors.base.AbstractSelectionVisitor;

public class GetRequiredIncludesVisitor extends AbstractSelectionVisitor<String> {
	
	private OutputFileType type;
	private Set<AbstractEntity> entitiesToInclude;
	
	public GetRequiredIncludesVisitor(OutputFileType type, Set<AbstractEntity> entitiesToInclude) {
		this.type = type;
		this.entitiesToInclude = entitiesToInclude;
	}
	
	@Override
	public int visitAbstractEntity(AbstractEntity entity) {
		if (entitiesToInclude != null && !entitiesToInclude.contains(entity))
			return CANCEL_THIS | CANCEL_CHILDREN;
		
		selections.addAll(entity.requiredIncludes(type).get());
		return CONTINUE;
	}
	
	@Override
	public int visitSpec(Spec spec) {
		if (type == OutputFileType.HEADER) {
			for (Spec s : spec.getIncludedSpecs()) {
				if (s.getName().equals("__internal__")) continue;
				selections.add('"' + s.getOutputFileName(OutputFileType.HEADER) + '"');
			}
		}
		return CONTINUE;
	}
	
}
