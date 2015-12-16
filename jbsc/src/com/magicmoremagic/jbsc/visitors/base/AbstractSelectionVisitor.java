package com.magicmoremagic.jbsc.visitors.base;

import java.util.*;

public abstract class AbstractSelectionVisitor<T> extends AbstractEntityVisitor {

	protected Set<T> selections = new HashSet<>();
	private Set<T> unmodSelections = Collections.unmodifiableSet(selections);
	
	public Set<T> getSelections() {
		return unmodSelections;
	}
	
	public void clearSelections() {
		selections.clear();
	}
	
	public boolean isSelected(T entity) {
		return selections.contains(entity);
	}
	
	public boolean isSelectionEmpty() {
		return selections.isEmpty();
	}

}
