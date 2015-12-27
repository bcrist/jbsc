package com.magicmoremagic.jbsc.objects.containers;

import com.magicmoremagic.jbsc.objects.base.AbstractContainer;
import com.magicmoremagic.jbsc.objects.base.EntityFlags;
import com.magicmoremagic.jbsc.objects.queries.FieldList;
import com.magicmoremagic.jbsc.util.CodeGenHelper;
import com.magicmoremagic.jbsc.visitors.base.IEntityVisitor;

public class Table extends AbstractContainer {

	private FieldList fields;
	
	public Table() {
		super(new EntityFlags());
		fields = new FieldList();
	}
	
	public Table(String name) {
		this();
		setName(name);
	}
	
	@Override
	public String getCName() {
		return CodeGenHelper.toPascalCase(getName());
	}
	
	public FieldList fields() {
		return fields;
	}
	
	@Override
	protected int onVisitorVisit(IEntityVisitor visitor) {
		int result = super.onVisitorVisit(visitor);
		if ((result & (IEntityVisitor.CANCEL_THIS | IEntityVisitor.STOP)) == 0) {
			result |= visitor.visit(this);
		}
		return result;
	}
	
	@Override
	protected int onVisitorLeave(IEntityVisitor visitor) {
		int result = super.onVisitorLeave(visitor);
		if ((result & (IEntityVisitor.CANCEL_THIS | IEntityVisitor.STOP)) == 0) {
			result |= visitor.leave(this);
		}
		return result;
	}

}
