package com.magicmoremagic.jbsc.visitors.base;

import com.magicmoremagic.jbsc.objects.*;
import com.magicmoremagic.jbsc.objects.base.*;
import com.magicmoremagic.jbsc.objects.containers.*;
import com.magicmoremagic.jbsc.objects.queries.Query;
import com.magicmoremagic.jbsc.objects.types.*;

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
	
	int visitSpec(Spec spec);
	int leaveSpec(Spec spec);
	
	int visitNamespace(Namespace namespace);
	int leaveNamespace(Namespace namespace);
	
	int visitFieldType(FieldType fieldType);
	int leaveFieldType(FieldType fieldType);
	
	int visitColType(ColType colType);
	int leaveColType(ColType colType);
	
	int visitClassType(ClassType classType);
	int leaveClassType(ClassType classType);
	
	int visitAggregateType(AggregateType aggregateType);
	int leaveAggregateType(AggregateType aggregateType);
	
	int visitTable(Table table);
	int leaveTable(Table table);
	
	int visitTableIndex(TableIndex index);
	int leaveTableIndex(TableIndex index);
	
	int visitFunction(Function function);
	int leaveFunction(Function function);
	
	int visitCode(Code code);
	int leaveCode(Code code);

	int visitQuery(Query query);
	int leaveQuery(Query query);
	
}
