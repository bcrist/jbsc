package com.magicmoremagic.jbsc.objects.queries;

public interface QueryExpression {

	FieldList getParameters();
	String getSQL();
	
}
