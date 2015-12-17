package com.magicmoremagic.jbsc.objects.base;

import static com.magicmoremagic.jbsc.visitors.base.IEntityVisitor.*;

import java.util.*;

import com.magicmoremagic.jbsc.objects.Flag;
import com.magicmoremagic.jbsc.objects.containers.Namespace;
import com.magicmoremagic.jbsc.objects.containers.Spec;
import com.magicmoremagic.jbsc.util.CodeGenConfig;
import com.magicmoremagic.jbsc.visitors.base.IEntityVisitor;
import com.magicmoremagic.jbsc.visitors.parentchain.GetNamespaceQualifiedCodeNameVisitor;
import com.magicmoremagic.jbsc.visitors.parentchain.GetQualifiedNameVisitor;

public abstract class Entity {

	protected Spec spec;
	protected EntityContainer parent;
	protected String name;
	private String qualifiedName;

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
		if (spec == null) {
			if (parent != null) {
				spec = parent.getSpec();
			}
		}
		return spec;
	}

	final public EntityContainer getParent() {
		return parent;
	}

	final void setParent(EntityContainer parent) {
		qualifiedName = null;
		spec = null;
		trySetParent(parent);
	}

	protected void trySetParent(EntityContainer newParent) {
		qualifiedName = null;
		spec = null;
		this.parent = newParent;
	}

	public Namespace getNamespace() {
		for (Entity parent = this; parent != null; parent = parent.getParent()) {
			if (parent instanceof Namespace)
				return (Namespace) parent;
		}
		return null;
	}

	final public String getName() {
		return name;
	}

	final public String getQualifiedName() {
		if (qualifiedName == null) {
			GetQualifiedNameVisitor visitor = new GetQualifiedNameVisitor();
			visitParentChain(visitor);
			qualifiedName = visitor.toString();
		}
		return qualifiedName;
	}

	public String getUnqualifiedCodeName() {
		return getName();
	}

	public String getQualifiedCodeName(Namespace fromNamespace) {
		GetNamespaceQualifiedCodeNameVisitor visitor = new GetNamespaceQualifiedCodeNameVisitor(fromNamespace);
		visitParentChain(visitor);
		return visitor.toString();
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

	protected void initRequiredIncludes() {
	}

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

	protected void initImplementationIncludes() {
	}

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

	public int visit(IEntityVisitor visitor) {
		int result = visitor.init(this);
		if ((result & STOP) != 0)
			return STOP;
		return continueVisit(visitor);
	}

	protected int continueVisit(IEntityVisitor visitor) {
		int result = onVisitorVisit(visitor);
		if ((result & (STOP | CANCEL_THIS)) == 0)
			result |= onVisitorLeave(visitor);

		return result;
	}

	public int visitParentChain(IEntityVisitor visitor) {
		int result = visitor.init(this);
		if ((result & STOP) != 0)
			return STOP;
		return continueVisitParentChain(visitor);
	}

	protected int continueVisitParentChain(IEntityVisitor visitor) {
		int result = onVisitorVisit(visitor);

		if ((result & STOP) != 0)
			return STOP;

		if ((result & CANCEL_PARENTS) == 0 && getParent() != null)
			result |= getParent().continueVisitParentChain(visitor);

		if ((result & STOP) != 0)
			return STOP;

		if ((result & CANCEL_THIS) == 0)
			result |= onVisitorLeave(visitor);

		return CONTINUE;
	}

	protected int onVisitorVisit(IEntityVisitor visitor) {
		return visitor.visit(this);
	}

	protected int onVisitorLeave(IEntityVisitor visitor) {
		return visitor.leave(this);
	}

}
