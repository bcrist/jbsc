package com.magicmoremagic.jbsc.objects.base;

import static com.magicmoremagic.jbsc.visitors.base.IEntityVisitor.*;

import java.util.*;

import com.magicmoremagic.jbsc.objects.containers.Namespace;
import com.magicmoremagic.jbsc.util.CodeGenConfig;
import com.magicmoremagic.jbsc.visitors.base.IEntityVisitor;

public abstract class AbstractContainer extends AbstractEntity {

	protected Map<String, AbstractEntity> children;
	protected Collection<AbstractEntity> unmodChildren;
	
	public AbstractContainer() {
		this(null);
	}
	
	public AbstractContainer(EntityFlags flags) {
		super(flags);
		children = new LinkedHashMap<>();
		unmodChildren = Collections.unmodifiableCollection(children.values());
	}
	
	@Override
	public Collection<AbstractEntity> getChildren() {
		return unmodChildren;
	}
	
	@Override
	public AbstractEntity getChildByName(String name) {
		return children.get(name);
	}
	
	@Override
	public AbstractContainer addChild(IEntity child) {
		if (child.getName() == null)
			throw new NullPointerException();
		
		IEntity myChild = children.get(child.getName());
		if (myChild != null) {
			if (myChild == child)
				return this;
			
			throw new IllegalArgumentException("An entity with that name already exists in this namespace!");
		}
		
		if (child instanceof AbstractEntity) {
			AbstractEntity abstractChild = (AbstractEntity)child;
			IEntity oldParent = abstractChild.getParent();
			if (oldParent != null) {
				oldParent.removeChild(abstractChild);
			}
			children.put(abstractChild.getName(), abstractChild);
			try {
				abstractChild.setParent(this);
			} catch (Exception e) {
				children.remove(abstractChild.getName());
				throw e;
			}
		} else {
			throw new IllegalArgumentException("Unsupported child type!");
		}
		
		return this;
	}
	
	@Override
	protected void onAncestorChanged(AbstractEntity oldAncestor) {
		super.onAncestorChanged(oldAncestor);
		for (AbstractEntity child : unmodChildren) {
			child.onAncestorChanged(oldAncestor);
		}
	}
	
	@Override
	public boolean removeChild(IEntity child) {
		if (child == null) {
			return false;
		}
		
		return removeChildByName(child.getName()) != null;
	}
	
	@Override
	public  AbstractEntity removeChildByName(String name) {
		AbstractEntity removed = children.remove(name);
		
		if (removed != null)
			removed.setParent(null);
		
		return removed;
	}
	
	@Override
	protected void onAncestorNameChanged(AbstractEntity ancestor, String oldName) {
		super.onAncestorNameChanged(ancestor, oldName);
		for (AbstractEntity child : unmodChildren) {
			child.onAncestorNameChanged(ancestor, oldName);
		}
	}
	
	@Override
	protected void onChildNameChanged(AbstractEntity child, String oldName) {
		IEntity myChild = children.get(oldName);
		if (myChild != null) {
			if (myChild == child) {
				IEntity myNewChild = children.get(child.getName());
				if (myNewChild != null) {
					if (myNewChild == child)
						return;
					
					throw new IllegalArgumentException("An entity with that name already exists in this namespace!");
				}
				children.remove(oldName);
				children.put(child.getName(), child);
			}
		}
		super.onChildNameChanged(child, oldName);
	}
	
	@Override
	public AbstractEntity lookupEntity(String name, boolean includeAncestors) {
		int index = name.indexOf(CodeGenConfig.QUALIFIED_NAME_SEPARATOR);
		if (index != -1) {
			// do recursive search in this container.
			String rootName = name.substring(0, index);
			
			AbstractEntity entity = children.get(rootName);
			if (entity != null) {
				String rest = name.substring(index + CodeGenConfig.QUALIFIED_NAME_SEPARATOR.length());
				return entity.lookupEntity(rest, false);
			} else if (getRoot() != null) {
				StringBuilder sb = new StringBuilder(getFullyQualifiedName());
				sb.append(CodeGenConfig.QUALIFIED_NAME_SEPARATOR);
				sb.append(name);
				
				entity = getRoot().lookupIncludedEntity(sb.toString());
				if (entity != null) {
					return entity;
				}
			}
		} else {
			// look for entire name in this container.
			AbstractEntity entity = children.get(name);
			if (entity != null) {
				return entity;
			} else if (getRoot() != null) {
				StringBuilder sb = new StringBuilder(getFullyQualifiedName());
				sb.append(CodeGenConfig.QUALIFIED_NAME_SEPARATOR);
				sb.append(name);
				
				entity = getRoot().lookupIncludedEntity(sb.toString());
				if (entity != null) {
					return entity;
				}
			}
		}
		
		if (includeAncestors) {
			// no matches, look in parent
			AbstractEntity parent = getParent();
			if (parent != null) {
				return parent.lookupEntity(name, true);
			}
		}
		
		// no matches
		return null;
	}
	
	@Override
	protected void finishExtractingNamespace(ExtractNamespaceResult result) {
		int index = result.name.indexOf("::");
		if (index != -1) {
			String rootName = result.name.substring(0, index);
			
			IEntity entity = children.get(rootName);
			if (entity instanceof Namespace) {
				result.namespace = (Namespace)entity;
				result.name = result.name.substring(index + 2);
				result.namespace.finishExtractingNamespace(result);
			}
		}
	}
	
	@Override
	public int visit(IEntityVisitor visitor) {
		int result = visitor.init(this);
		if ((result & STOP) != 0) return STOP;
		return continueVisit(visitor);
	}
	
	@Override
	protected int continueVisit(IEntityVisitor visitor) {
		int result = onVisitorVisit(visitor);
		
		if ((result & STOP) != 0) return STOP;
			
		if ((result & CANCEL_CHILDREN) == 0)
			result |= visitChildren(visitor);
		
		if ((result & STOP) != 0) return STOP;
			
		if ((result & CANCEL_THIS) == 0)
			result |= onVisitorLeave(visitor);
				
		return result;
	}
	
	protected int visitChildren(IEntityVisitor visitor) {
		for (AbstractEntity child : children.values()) {
			int result = child.continueVisit(visitor);
			
			if ((result & STOP) != 0) return STOP;
			if ((result & CANCEL_SIBLINGS) != 0) break;
		}
		return CONTINUE;
	}
	
	@Override
	protected int onVisitorVisit(IEntityVisitor visitor) {
		int result = super.onVisitorVisit(visitor);
		if ((result & (IEntityVisitor.CANCEL_THIS | IEntityVisitor.STOP)) == 0) {
			result |= visitor.visitAbstractContainer(this);
		}
		return result;
	}
	
	@Override
	protected int onVisitorLeave(IEntityVisitor visitor) {
		int result = super.onVisitorLeave(visitor);
		if ((result & (IEntityVisitor.CANCEL_THIS | IEntityVisitor.STOP)) == 0) {
			result |= visitor.leaveAbstractContainer(this);
		}
		return result;
	}
	
}
