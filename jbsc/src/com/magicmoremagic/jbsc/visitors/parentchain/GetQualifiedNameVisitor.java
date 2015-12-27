package com.magicmoremagic.jbsc.visitors.parentchain;

import com.magicmoremagic.jbsc.objects.base.AbstractEntity;
import com.magicmoremagic.jbsc.objects.base.IEntity;
import com.magicmoremagic.jbsc.objects.containers.Spec;
import com.magicmoremagic.jbsc.util.CodeGenConfig;
import com.magicmoremagic.jbsc.visitors.base.AbstractStringBuilderVisitor;

public class GetQualifiedNameVisitor extends AbstractStringBuilderVisitor {
	
	private Class<?> firstClass;
	
	@Override
	public int init(IEntity entity) {
		firstClass = entity.getClass();
		return CONTINUE;
	}
	
	@Override
	public int leaveAbstractEntity(AbstractEntity entity) {
		if (entity instanceof Spec && firstClass != Spec.class)
			return CANCEL_THIS;
		
		if (sb.length() > 0)
			sb.append(CodeGenConfig.QUALIFIED_NAME_SEPARATOR);
			
		sb.append(entity.getName());
		
		return CANCEL_THIS;
	}

}
