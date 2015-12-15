package com.magicmoremagic.jbsc.objects.base;

import java.util.*;

import com.magicmoremagic.jbsc.objects.Flag;
import com.magicmoremagic.jbsc.objects.containers.Spec;
import com.magicmoremagic.jbsc.util.CodeGenConfig;
import com.magicmoremagic.jbsc.visitors.IEntityVisitor;

public class Entity {

	protected Spec spec;
	protected EntityContainer parent;
	protected String name;
	
	protected Set<String> requiredIncludes;
	protected Set<String> unmodRequiredIncludes;
	
	protected Set<String> implementationIncludes;
	protected Set<String> unmodImplementationIncludes;
	
	protected Set<Flag> flags;
	protected Set<Flag> unmodFlags;
	
	public Entity() {
		requiredIncludes = new HashSet<>();
		unmodRequiredIncludes = Collections.unmodifiableSet(requiredIncludes);
		initRequiredIncludes();
		
		implementationIncludes = new HashSet<>();
		unmodImplementationIncludes = Collections.unmodifiableSet(implementationIncludes);
		initImplementationIncludes();
		
		flags = EnumSet.noneOf(Flag.class);
		unmodFlags = Collections.unmodifiableSet(flags);
	}
	
	public Spec getSpec() {
		return spec;
	}
	
	public Entity setSpec(Spec spec) {
		this.spec = spec;
		return this;
	}
	
	final public EntityContainer getParent() {
		return parent;
	}
	
	final void setParent(EntityContainer parent) {
		trySetParent(parent);
	}
	
	protected void trySetParent(EntityContainer newParent) {
		this.parent = newParent;
	}
	
	final public String getName() {
		return name;
	}

	final public String getQualifiedName() {
		return getQualifiedName(CodeGenConfig.QUALIFIED_NAME_SEPARATOR, Entity.class);
	}
		
	final public String getQualifiedName(String separator, Class<? extends Entity> filterClass) {
		if (parent != null) {
			StringBuilder sb = new StringBuilder();
			parent.appendQualifiedName(sb, separator, filterClass);
			if (sb.length() > 0)
				sb.append(separator);
			sb.append(getName());
			return sb.toString();
		} else {
			return getName();
		}
	}
	
	final void appendQualifiedName(StringBuilder sb, String separator, Class<? extends Entity> filterClass) {
		boolean addMe = filterClass.isInstance(this);
		if (parent != null) {
			parent.appendQualifiedName(sb, separator, filterClass);
			if (sb.length() > 0 && addMe)
				sb.append(separator);
		}
		if (addMe)
			sb.append(getName());
	}
	
	
	public Entity setName(String name) {
		if (this.name == null && name != null || !this.name.equals(name)) {
			trySetName(name);
		}
		return this;
	}
	
	protected void trySetName(String newName) {
		if (name != null && name.contains(CodeGenConfig.QUALIFIED_NAME_SEPARATOR)) {
			throw new IllegalArgumentException("Invalid name");
		}
		
		String oldName = this.name;
		if (parent != null) {
			parent.doChildNameChange(oldName, newName, this);
		} else {
			name = newName;
		}
	}
	
	public Set<String> getRequiredIncludes() {
		return unmodRequiredIncludes;
	}
	
	public Entity setRequiredIncludes(Collection<String> requiredIncludes) {
		this.requiredIncludes.clear();
		initRequiredIncludes();
		this.requiredIncludes.addAll(requiredIncludes);
		return this;
	}
	
	public Entity addRequiredInclude(String include) {
		requiredIncludes.add(include);
		return this;
	}
	
	public Entity removeRequiredInclude(String include) {
		requiredIncludes.remove(include);
		return this;
	}
	
	protected void initRequiredIncludes() { }
	
	public Set<String> getImplementationIncludes() {
		return unmodImplementationIncludes;
	}
	
	public Entity setImplementationIncludes(Collection<String> implementationIncludes) {
		this.implementationIncludes.clear();
		initImplementationIncludes();
		this.implementationIncludes.addAll(implementationIncludes);
		return this;
	}
	
	public Entity addImplementationInclude(String include) {
		implementationIncludes.add(include);
		return this;
	}
	
	public Entity removeImplementationInclude(String include) {
		implementationIncludes.remove(include);
		return this;
	}
	
	protected void initImplementationIncludes() { }
	
	
	public Set<Flag> getFlags() {
		return unmodFlags;
	}
	
	public boolean hasFlag(Flag flag) {
		return hasFlag(flag, true);
	}
	
	public boolean hasFlag(Flag flag, boolean includeParent) {
		if (flags.contains(flag))
			return true;
		
		if (includeParent && parent != null) {
			return parent.hasFlag(flag);
		}
		
		return false;
	}
	
	public Entity setFlag(Flag flag, boolean value) {
		if (value) {
			flags.add(flag);
		} else {
			flags.remove(flag);
		}
		return this;
	}
	
	public Entity setFlags(Collection<Flag> flags) {
		flags.clear();
		flags.addAll(flags);
		return this;
	}
	
	public int acceptVisitor(IEntityVisitor visitor) {
		return visitor.visit(this);
	}
	
}
