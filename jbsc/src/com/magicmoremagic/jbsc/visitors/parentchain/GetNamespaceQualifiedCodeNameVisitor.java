package com.magicmoremagic.jbsc.visitors.parentchain;

import com.magicmoremagic.jbsc.objects.base.AbstractEntity;
import com.magicmoremagic.jbsc.objects.base.Entities;
import com.magicmoremagic.jbsc.objects.base.IEntity;
import com.magicmoremagic.jbsc.objects.containers.Namespace;
import com.magicmoremagic.jbsc.objects.types.ClassType;
import com.magicmoremagic.jbsc.visitors.base.AbstractStringBuilderVisitor;

public class GetNamespaceQualifiedCodeNameVisitor extends AbstractStringBuilderVisitor {

	private static final String DELIM = "::";
	
	private Namespace fromNamespace;
	
	private IEntity originalEntity;
	private Namespace commonAncestor;
	private boolean useGlobalScopeOperator;
	private boolean prependDelim;
	
	public GetNamespaceQualifiedCodeNameVisitor(Namespace fromNamespace) {
		this.fromNamespace = fromNamespace;
	}
	
	@Override
	public int init(IEntity entity) {
		originalEntity = entity;
		if (entity instanceof ClassType) {
			ClassType type = (ClassType)entity;
			if (type.isBuiltin()) {
				sb.append(entity.getCName());
				return STOP;
			}
		}
		
		useGlobalScopeOperator = !(entity instanceof Namespace);
		commonAncestor = Entities.findCommonAncestor(entity, fromNamespace, Namespace.class);
		prependDelim = false;
		return CONTINUE;
	}
	
	@Override
	public int visitAbstractEntity(AbstractEntity entity) {
		if (entity == commonAncestor) {
			return CANCEL_THIS | CANCEL_PARENTS;
		}
		
		if (useGlobalScopeOperator && entity.getParent() == null) {
			sb.append(DELIM);
		}
		
		return CONTINUE;
	}
	
	@Override
	public int leaveAbstractEntity(AbstractEntity entity) {
		if (entity == originalEntity || entity instanceof Namespace) {
			if (prependDelim)
				sb.append(DELIM);
			else
				prependDelim = true;
			
			sb.append(entity.getCName());
		}
		return CANCEL_THIS;
	}
	
}
