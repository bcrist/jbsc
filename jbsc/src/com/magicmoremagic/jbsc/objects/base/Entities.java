package com.magicmoremagic.jbsc.objects.base;

import java.util.ArrayList;
import java.util.List;

public abstract class Entities {
	
	// find's the closest common ancestor of a specific type shared by both entities
	@SuppressWarnings("unchecked")
	public static <T> T findCommonAncestor(IEntity a, IEntity b, Class<T> ancestorType) {
		List<IEntity> ancestors = new ArrayList<>();
		for (IEntity aa = a; aa != null; aa = aa.getParent()) {
			if (ancestorType.isInstance(aa)) {
				ancestors.add(aa);
			}
		}
		
		for (IEntity bb = b; bb != null; bb = bb.getParent()) {
			String bbqn = bb.getFullyQualifiedName();
			for (IEntity aa : ancestors) {
				if (bbqn.equals(aa.getFullyQualifiedName()) && aa.getClass().equals(bb.getClass()))
					return (T)aa;
			}
		}
		
		return null;
	}

}
