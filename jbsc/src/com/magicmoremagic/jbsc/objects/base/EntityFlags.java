package com.magicmoremagic.jbsc.objects.base;

import java.util.*;

import com.magicmoremagic.jbsc.objects.Flag;

public class EntityFlags {

	protected EntityFlags prototype;
	protected boolean canRemovePrototypeFlags;
	
	protected Set<Flag> flags;
	protected Set<Flag> unmodFlags;

	public static final EntityFlags NULL = new EntityFlags() {
		
		@Override
		public EntityFlags add(Flag flag) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public EntityFlags set(Collection<Flag> flags) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public EntityFlags remove(Flag include) {
			throw new UnsupportedOperationException();
		}
	};

	public EntityFlags() {
		this(null, true);
	}
	
	public EntityFlags(EntityFlags prototype, boolean canRemovePrototypeFlags) {
		this.prototype = prototype;
		this.canRemovePrototypeFlags = canRemovePrototypeFlags;
		flags = EnumSet.noneOf(Flag.class);
		unmodFlags = Collections.unmodifiableSet(flags);
		init();
	}
	
	protected void init() {
		if (prototype != null) {
			flags.addAll(prototype.get());
		}
	}
	
	public Set<Flag> get() {
		return unmodFlags;
	}

	public EntityFlags set(Collection<Flag> flags) {
		this.flags.clear();
		init();
		this.flags.addAll(flags);
		return this;
	}

	public EntityFlags add(Flag flag) {
		flags.add(flag);
		return this;
	}

	public boolean contains(Flag flag) {
		return flags.contains(flag);
	}
	
	public EntityFlags remove(Flag include) {
		if (canRemovePrototypeFlags ||
				prototype == null ||
				!prototype.contains(include)) {
			flags.remove(include);
		}
		return this;
	}
	
}
