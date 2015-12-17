package com.magicmoremagic.jbsc.visitors.base;

import com.magicmoremagic.jbsc.objects.Code;
import com.magicmoremagic.jbsc.objects.Function;
import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.objects.base.EntityContainer;
import com.magicmoremagic.jbsc.objects.containers.*;
import com.magicmoremagic.jbsc.objects.types.*;

public interface IEntityVisitor {
	
	public static final int CONTINUE = 0;
	public static final int CANCEL_THIS = 1 << 0;
	public static final int CANCEL_CHILDREN = 1 << 1;
	public static final int CANCEL_SIBLINGS = 1 << 2;
	public static final int CANCEL_PARENTS = CANCEL_CHILDREN; // only used for parent chain visitation
	public static final int STOP = 1 << 3;
	
	int init(Entity entity);
	
	int visit(Entity entity);
	int leave(Entity entity);
	
	int visit(EntityContainer container);
	int leave(EntityContainer container);
	
	int visit(Spec spec);
	int leave(Spec spec);
	
	int visit(Namespace namespace);
	int leave(Namespace namespace);
	
	int visit(FieldType fieldType);
	int leave(FieldType fieldType);
	
	int visit(ColType colType);
	int leave(ColType colType);
	
	int visit(ClassType classType);
	int leave(ClassType classType);
	
	int visit(Table table);
	int leave(Table table);
	
	int visit(Function function);
	int leave(Function function);
	
	int visit(Code code);
	int leave(Code code);

}
