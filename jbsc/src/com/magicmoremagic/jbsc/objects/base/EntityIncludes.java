package com.magicmoremagic.jbsc.objects.base;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EntityIncludes {

	public static final UnmodifiableEntityIncludes NULL = new UnmodifiableEntityIncludes();
	public static final UnmodifiableEntityIncludes BED_STMT_INCLUDES = new UnmodifiableEntityIncludes(
			"\"be/bed/stmt.hpp\"", "\"be/bed/bed.hpp\"");

	protected EntityIncludes prototype;
	protected boolean canRemovePrototypeIncludes;

	protected Set<String> includes;
	protected Set<String> unmodIncludes;

	public EntityIncludes() {
		this(null, true);
	}

	public EntityIncludes(EntityIncludes prototype,
			boolean canRemovePrototypeIncludes) {
		this.prototype = prototype;
		this.canRemovePrototypeIncludes = canRemovePrototypeIncludes;
		includes = new HashSet<>();
		unmodIncludes = Collections.unmodifiableSet(includes);
		init();
	}

	protected void init() {
		if (prototype != null) {
			includes.addAll(prototype.get());
		}
	}

	public Set<String> get() {
		return unmodIncludes;
	}

	public EntityIncludes set(Collection<String> includes) {
		this.includes.clear();
		init();
		this.includes.addAll(includes);
		return this;
	}

	public EntityIncludes add(String include) {
		includes.add(include);
		return this;
	}

	public boolean contains(String include) {
		return includes.contains(include);
	}

	public EntityIncludes remove(String include) {
		if (canRemovePrototypeIncludes || prototype == null
				|| !prototype.contains(include)) {
			includes.remove(include);
		}
		return this;
	}

	public static class UnmodifiableEntityIncludes extends EntityIncludes {

		public UnmodifiableEntityIncludes(String... includes) {
			if (includes == null)
				return;
			for (String include : includes) {
				super.add(include);
			}
		}

		@Override
		public EntityIncludes add(String include) {
			throw new UnsupportedOperationException();
		}

		@Override
		public EntityIncludes set(java.util.Collection<String> includes) {
			throw new UnsupportedOperationException();
		}

		@Override
		public EntityIncludes remove(String include) {
			throw new UnsupportedOperationException();
		}
	}

}
