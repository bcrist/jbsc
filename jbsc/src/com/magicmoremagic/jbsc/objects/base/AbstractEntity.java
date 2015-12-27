package com.magicmoremagic.jbsc.objects.base;

import static com.magicmoremagic.jbsc.visitors.base.IEntityVisitor.*;

import java.util.Collection;
import java.util.Collections;

import com.magicmoremagic.jbsc.OutputFileType;
import com.magicmoremagic.jbsc.objects.Flag;
import com.magicmoremagic.jbsc.objects.containers.Namespace;
import com.magicmoremagic.jbsc.objects.containers.Spec;
import com.magicmoremagic.jbsc.util.CodeGenConfig;
import com.magicmoremagic.jbsc.util.CodeGenHelper;
import com.magicmoremagic.jbsc.visitors.base.IEntityVisitor;
import com.magicmoremagic.jbsc.visitors.parentchain.GetNamespaceQualifiedCodeNameVisitor;
import com.magicmoremagic.jbsc.visitors.parentchain.GetQualifiedNameVisitor;

public abstract class AbstractEntity implements IEntity {
	
	protected AbstractEntity parent;
	private String name;
	
	private EntityFlags flags;
	
	// cached
	private Spec spec;
	private Namespace namespace;
	private String qualifiedName;

	protected AbstractEntity() {
		this(EntityFlags.NULL);
	}
	
	public AbstractEntity(EntityFlags flags) {
		this.flags = flags;
		setName(calculateDefaultName());
	}
	
	@Override
	final public AbstractEntity getParent() {
		return parent;
	}

	protected void setParent(AbstractEntity newParent) {
		if (newParent != parent) {
			AbstractEntity oldParent = parent;
			parent = newParent;
			try {
				onParentChanged(oldParent);
			} catch (Exception e) {
				parent = oldParent;
				onParentChanged(newParent);
				throw e;
			}
		}
	}
	
	protected void onParentChanged(AbstractEntity oldParent) {
		onAncestorChanged(oldParent);
	}
	
	protected void onAncestorChanged(AbstractEntity oldAncestor) {
		qualifiedName = null;
		spec = null;
	}
	
	@Override
	public Collection<AbstractEntity> getChildren() {
		Collection<AbstractEntity> empty = Collections.emptyList();
		return empty;
	}
	
	@Override
	public IEntity getChildByName(String name) {
		return null;
	}
	
	@Override
	public IEntity addChild(IEntity child) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean removeChild(IEntity child) {
		return false;
	}
	
	@Override
	public IEntity removeChildByName(String name) {
		return null;
	}
	
	@Override
	public Spec getRoot() {
		if (spec == null) {
			spec = calculateRoot();
		}
		return spec;
	}
	
	protected Spec calculateRoot() {
		if (this instanceof Spec)
			return (Spec)this;
		
		if (parent != null) {
			return parent.getRoot();
		}
		
		return null;
	}
	
	@Override
	public Namespace getNamespace() {
		if (namespace == null) {
			namespace = calculateNamespace();
		}
		return namespace;
	}
	
	protected Namespace calculateNamespace() {
		if (this instanceof Namespace)
			return (Namespace)this;
		
		if (parent != null) {
			return parent.getNamespace();
		}
		
		return null;	
	}

	@Override
	public String getName() {
		return name;
	}
	
	protected String calculateDefaultName() {
		return CodeGenHelper.getRandomName(20);
	}
	
	@Override
	public IEntity setName(String name) {
		if (name == null)
			throw new NullPointerException();
		
		if (!name.equals(this.name)) {
			if (name.contains(CodeGenConfig.QUALIFIED_NAME_SEPARATOR))
				throw new IllegalArgumentException("Invalid name");
			
			String oldName = this.name;
			this.name = name;
			if (oldName != null) {
				try {
					onNameChanged(oldName);
				} catch (Exception e) {
					this.name = oldName;
					onNameChanged(name);
					throw e;
				}
			}
		}
		return this;
	}
	
	protected void onNameChanged(String oldName) {
		if (parent != null) {
			parent.onChildNameChanged(this, oldName);
		}
	}
	
	protected void onChildNameChanged(AbstractEntity child, String oldName) { }
	
	protected void onAncestorNameChanged(AbstractEntity ancestor, String oldName) {
		qualifiedName = null;
	}
	
	@Override
	public String getFullyQualifiedName() {
		if (qualifiedName == null) {
			qualifiedName = calculateFullyQualifiedName();
		}
		return qualifiedName;
	}
	
	public String calculateFullyQualifiedName() {
		GetQualifiedNameVisitor visitor = new GetQualifiedNameVisitor();
		visitParents(visitor);
		return visitor.toString();
	}
	
	@Override
	public String getCName() {
		return getName();
	}

	@Override
	public String getQualifiedCName() {
		return getQualifiedCName(null);
	}
	
	@Override
	public String getQualifiedCName(Namespace fromNamespace) {
		GetNamespaceQualifiedCodeNameVisitor visitor = new GetNamespaceQualifiedCodeNameVisitor(fromNamespace);
		visitParents(visitor);
		return visitor.toString();
	}
	
	@Override
	public IEntity lookupEntity(String name) {
		return lookupEntity(name, true);
	}
	
	@Override
	public AbstractEntity lookupEntity(String name, boolean includeAncestors) {
		if (includeAncestors && parent != null) {
			return parent.lookupEntity(name, true);
		}
		return null;
	}
	
	@Override
	public String lookupCName(String name, String backup) {
		String codeName;
		try {
			codeName = lookupEntity(name).getQualifiedCName(getNamespace());
		} catch (Exception e) {
			codeName = backup;
		}
		return codeName;
	}
	
	public static class ExtractNamespaceResult {
		public Namespace namespace;
		public String name;
	}
	
	public ExtractNamespaceResult extractNamespace(String cName) {
		ExtractNamespaceResult result = new ExtractNamespaceResult();
		result.name = cName;
		
		int index = result.name.indexOf("::");
		if (index == 0) {
			result.name = result.name.substring(2); // remove leading "::"
			if (getRoot() != null)
				getRoot().finishExtractingNamespace(result);
			
		} else if (index != -1) {
			String rootName = result.name.substring(0, index);
			IEntity entity = lookupEntity(rootName);
			if (entity instanceof Namespace && entity.getRoot() == getRoot()) {
				result.namespace = (Namespace)entity;
				result.name = result.name.substring(index + 2);
				result.namespace.finishExtractingNamespace(result);
			}
		}
		
		return result;
	}
	
	protected void finishExtractingNamespace(ExtractNamespaceResult result) { }

	@Override
	public EntityFlags flags() {
		return flags;
	}

	@Override
	public boolean hasFlag(Flag flag) {
		return hasFlag(flag, true);
	}

	@Override
	public boolean hasFlag(Flag flag, boolean includeAncestors) {
		if (flags.contains(flag))
			return true;

		if (includeAncestors && parent != null) {
			return parent.hasFlag(flag, true);
		}

		return false;
	}

	@Override
	public EntityIncludes requiredIncludes(OutputFileType type) {
		return EntityIncludes.NULL;
	}
	
	@Override
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

	@Override
	public int visitParents(IEntityVisitor visitor) {
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
		return visitor.visitAbstractEntity(this);
	}

	protected int onVisitorLeave(IEntityVisitor visitor) {
		return visitor.leaveAbstractEntity(this);
	}

}
