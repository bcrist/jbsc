package com.magicmoremagic.jbsc.visitors;

import com.magicmoremagic.jbsc.objects.Function;
import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.objects.base.EntityContainer;
import com.magicmoremagic.jbsc.objects.containers.Namespace;
import com.magicmoremagic.jbsc.objects.containers.Spec;
import com.magicmoremagic.jbsc.objects.types.*;

public interface IEntityVisitor {
	
	public static final int CONTINUE = 0;
	public static final int CANCEL_THIS = 1 << 0;
	public static final int CANCEL_CHILDREN = 1 << 1;
	public static final int CANCEL_SIBLINGS = 1 << 2;
	public static final int STOP = 1 << 3;
	
	int visit(Entity entity);
	int visit(FieldType fieldType);
	int visit(ColType function);
	int visit(ClassType function);
	int visit(Function function);
	
	int visit(EntityContainer container);
	int visit(Spec spec);
	int visit(Namespace namespace);
	
	int leave(EntityContainer container);
	int leave(Spec spec);
	int leave(Namespace namespace);

}
