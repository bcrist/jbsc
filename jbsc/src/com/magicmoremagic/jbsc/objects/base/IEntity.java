package com.magicmoremagic.jbsc.objects.base;

import java.util.Collection;

import com.magicmoremagic.jbsc.OutputFileType;
import com.magicmoremagic.jbsc.objects.Flag;
import com.magicmoremagic.jbsc.objects.containers.Namespace;
import com.magicmoremagic.jbsc.objects.containers.Spec;
import com.magicmoremagic.jbsc.visitors.base.IEntityVisitor;

public interface IEntity {
	
	public IEntity getParent();
	public Collection<? extends IEntity> getChildren();
	public IEntity getChildByName(String name);
	
	public IEntity addChild(IEntity child);
	public boolean removeChild(IEntity child);
	public IEntity removeChildByName(String name);

	public Spec getRoot();
	public Namespace getNamespace();
	
	public String getName();
	public IEntity setName(String newName);
	public String getFullyQualifiedName();
	
	public String getCName();
	public String getQualifiedCName();
	public String getQualifiedCName(Namespace fromNamespace);
	
	public IEntity lookupEntity(String name);
	public IEntity lookupEntity(String name, boolean includeAncestors);
	public String lookupCName(String name, String defaultCName);
	
	public EntityFlags flags();
	public boolean hasFlag(Flag flag);
	public boolean hasFlag(Flag flag, boolean includeAncestors);
	
	public EntityIncludes requiredIncludes(OutputFileType type);
	
	public int visit(IEntityVisitor visitor);
	public int visitParents(IEntityVisitor visitor);
	
}
