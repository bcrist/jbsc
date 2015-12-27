package com.magicmoremagic.jbsc.visitors.base;

import com.magicmoremagic.jbsc.objects.Code;
import com.magicmoremagic.jbsc.objects.Function;
import com.magicmoremagic.jbsc.objects.base.AbstractContainer;
import com.magicmoremagic.jbsc.objects.base.AbstractEntity;
import com.magicmoremagic.jbsc.objects.base.IEntity;
import com.magicmoremagic.jbsc.objects.containers.Namespace;
import com.magicmoremagic.jbsc.objects.containers.Spec;
import com.magicmoremagic.jbsc.objects.containers.Table;
import com.magicmoremagic.jbsc.objects.queries.Query;
import com.magicmoremagic.jbsc.objects.types.ClassType;
import com.magicmoremagic.jbsc.objects.types.ColType;
import com.magicmoremagic.jbsc.objects.types.FieldType;

public interface IEntityVisitor {
	
	public static final int CONTINUE = 0;
	public static final int CANCEL_THIS = 1 << 0;
	public static final int CANCEL_CHILDREN = 1 << 1;
	public static final int CANCEL_SIBLINGS = 1 << 2;
	public static final int CANCEL_PARENTS = CANCEL_CHILDREN; // only used for parent chain visitation
	public static final int STOP = 1 << 3;
	
	int init(IEntity entity);
	
	int visitAbstractEntity(AbstractEntity entity);
	int leaveAbstractEntity(AbstractEntity entity);
	
	int visitAbstractContainer(AbstractContainer container);
	int leaveAbstractContainer(AbstractContainer container);
	
	int visit(Spec spec);
	int leave(Spec spec);
	
	int visit(Namespace namespace);
	int leave(Namespace namespace);
	
	int visitFieldType(FieldType fieldType);
	int leaveFieldType(FieldType fieldType);
	
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

	int visit(Query query);
	int leave(Query query);
	
}
