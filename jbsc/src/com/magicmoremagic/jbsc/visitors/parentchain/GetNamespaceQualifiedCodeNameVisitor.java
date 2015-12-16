package com.magicmoremagic.jbsc.visitors.parentchain;

import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.objects.base.EntityContainer;
import com.magicmoremagic.jbsc.objects.containers.Namespace;
import com.magicmoremagic.jbsc.visitors.base.AbstractStringBuilderVisitor;

public class GetNamespaceQualifiedCodeNameVisitor extends AbstractStringBuilderVisitor {

	private static final String DELIM = "::";
	
	private Namespace fromNamespace;
	
	private Entity originalEntity;
	private Namespace commonAncestor;
	private boolean useGlobalScopeOperator;
	private boolean prependDelim;
	
	public GetNamespaceQualifiedCodeNameVisitor(Namespace fromNamespace) {
		this.fromNamespace = fromNamespace;
	}
	
	@Override
	public int init(Entity entity) {
		originalEntity = entity;
		useGlobalScopeOperator = !(entity instanceof Namespace);
		commonAncestor = EntityContainer.findCommonAncestor(entity, fromNamespace, Namespace.class);
		prependDelim = false;
		return CONTINUE;
	}
	
	@Override
	public int visit(Entity entity) {
		if (entity == commonAncestor) {
			return CANCEL_THIS | CANCEL_PARENTS;
		}
		
		if (useGlobalScopeOperator && entity.getParent() == null) {
			sb.append(DELIM);
		}
		
		return CONTINUE;
	}
	
	@Override
	public int leave(Entity entity) {
		if (entity == originalEntity || entity instanceof Namespace) {
			if (prependDelim)
				sb.append(DELIM);
			else
				prependDelim = true;
			
			sb.append(entity.getUnqualifiedCodeName());
		}
		return CANCEL_THIS;
	}
	
}
