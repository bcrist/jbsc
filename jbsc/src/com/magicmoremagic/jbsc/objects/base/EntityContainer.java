package com.magicmoremagic.jbsc.objects.base;

import java.util.*;

import com.magicmoremagic.jbsc.util.CodeGenConfig;
import com.magicmoremagic.jbsc.visitors.IEntityVisitor;

public class EntityContainer extends Entity {

	protected Map<String, Entity> children;
	protected Collection<Entity> unmodChildren;
	
	public EntityContainer() {
		children = new LinkedHashMap<>();
		unmodChildren = Collections.unmodifiableCollection(children.values());
	}
	
	public Entity lookupName(String name) {
		int index = name.indexOf(CodeGenConfig.QUALIFIED_NAME_SEPARATOR);
		if (index != -1) {
			// do recursive search in this container.
			String rootName = name.substring(0, index);
			
			Entity entity = children.get(rootName);
			if (entity instanceof EntityContainer) {
				String rest = name.substring(index + CodeGenConfig.QUALIFIED_NAME_SEPARATOR.length());
				return ((EntityContainer)entity).lookupName(rest);
			}
		} else {
			// look for entire name in this container.
			Entity entity = children.get(name);
			if (entity != null) {
				return entity;
			}
		}
		
		// no matches, look in parent
		EntityContainer parent = getParent();
		if (parent != null) {
			return parent.lookupName(name);
		}
		
		// no matches
		return null;
	}
	
	public Collection<Entity> getChildren() {
		return unmodChildren;
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> getChildrenByType(Class<T> clazz) {
		List<T> list = new ArrayList<T>();
		for (Entity entity : children.values()) {
			if (clazz.isInstance(entity)) {
				list.add((T)entity);
			}
		}
		return list;
	}
	
	public Entity getChildByName(String name) {
		return children.get(name);
	}
	
	public EntityContainer addChild(Entity child) {
		Entity myChild = children.get(child.getName());
		if (myChild != null) {
			if (myChild == child)
				return this;
			
			throw new IllegalArgumentException("An entity with that name already exists in this namespace!");
		}
		
		EntityContainer oldParent = child.getParent();
		if (oldParent != null) {
			oldParent.removeChild(child);
		}
		children.put(child.getName(), child);
		child.setParent(this);
		return this;
	}
	
	public EntityContainer removeChild(Entity child) {
		if (children.remove(child.getName()) != null)
			child.setParent(null);
		
		return this;
	}
	
	
	final void doChildNameChange(String oldChildName, String newChildName, Entity child) {
		Entity myChild = children.get(oldChildName);
		if (myChild != null) {
			if (myChild == child) {
				Entity myNewChild = children.get(child.getName());
				if (myNewChild != null) {
					if (myNewChild == child)
						return;
					
					throw new IllegalArgumentException("An entity with that name already exists in this namespace!");
				}
				children.remove(oldChildName);
				children.put(newChildName, child);
				child.name = newChildName;
			}
		}
	}
	
	@Override
	public int acceptVisitor(IEntityVisitor visitor) {
		int result = super.acceptVisitor(visitor);
		
		if ((result & IEntityVisitor.STOP) != 0) return IEntityVisitor.STOP;
		
		if ((result & IEntityVisitor.CANCEL_THIS) == 0)
			result |= acceptVisitorEnter(visitor);
		
		if ((result & IEntityVisitor.STOP) != 0) return IEntityVisitor.STOP;
			
		if ((result & IEntityVisitor.CANCEL_CHILDREN) == 0)
			result |= acceptVisitorChildren(visitor);
		
		if ((result & IEntityVisitor.STOP) != 0) return IEntityVisitor.STOP;
			
		if ((result & IEntityVisitor.CANCEL_THIS) == 0)
			result |= acceptVisitorLeave(visitor);
				
		return result;
	}
	
	protected int acceptVisitorEnter(IEntityVisitor visitor) {
		return visitor.visit(this);
	}
	
	protected int acceptVisitorChildren(IEntityVisitor visitor) {
		for (Entity child : children.values()) {
			int result = child.acceptVisitor(visitor);
			
			if ((result & IEntityVisitor.STOP) != 0) return IEntityVisitor.STOP;
			if ((result & IEntityVisitor.CANCEL_SIBLINGS) != 0) break;
		}
		return 0;
	}
	
	protected int acceptVisitorLeave(IEntityVisitor visitor) {
		return visitor.leave(this);
	}
	
}
