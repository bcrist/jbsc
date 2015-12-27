package com.magicmoremagic.jbsc.objects.types;

import com.magicmoremagic.jbsc.OutputFileType;
import com.magicmoremagic.jbsc.objects.Flag;
import com.magicmoremagic.jbsc.objects.base.AbstractEntity;
import com.magicmoremagic.jbsc.objects.base.EntityFlags;
import com.magicmoremagic.jbsc.objects.base.EntityFunctions;
import com.magicmoremagic.jbsc.objects.base.EntityIncludes;
import com.magicmoremagic.jbsc.objects.queries.FieldList;
import com.magicmoremagic.jbsc.visitors.base.IEntityVisitor;

public abstract class FieldType extends AbstractEntity {

	private EntityIncludes headerIncludes = new EntityIncludes();
	private EntityIncludes sourceIncludes = new EntityIncludes();
	
	public FieldType() {
		super(new EntityFlags());
	}
	
	public abstract FieldList fields();	
	public abstract EntityFunctions functions();
	
	public boolean isAssignByValue() {
		return hasFlag(Flag.ASSIGN_BY_VALUE, false);
	}
	
	@Override
	public EntityIncludes requiredIncludes(OutputFileType type) {
		switch (type) {
		case HEADER:
			return headerIncludes;
		case SOURCE:
			return sourceIncludes;
		default:
			return super.requiredIncludes(type);
		}
	}
	
	@Override
	protected int onVisitorVisit(IEntityVisitor visitor) {
		int result = super.onVisitorVisit(visitor);
		if ((result & (IEntityVisitor.CANCEL_THIS | IEntityVisitor.STOP)) == 0) {
			result |= visitor.visitFieldType(this);
		}
		return result;
	}
	
	@Override
	protected int onVisitorLeave(IEntityVisitor visitor) {
		int result = super.onVisitorLeave(visitor);
		if ((result & (IEntityVisitor.CANCEL_THIS | IEntityVisitor.STOP)) == 0) {
			result |= visitor.leaveFieldType(this);
		}
		return result;
	}
	
}

