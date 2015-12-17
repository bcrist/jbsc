package com.magicmoremagic.jbsc.objects;

import com.magicmoremagic.jbsc.OutputFileType;
import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.util.CodeGenHelper;
import com.magicmoremagic.jbsc.visitors.base.IEntityVisitor;

public class Code extends Entity {

	protected OutputFileType type;
	protected String code;
	
	public Code() {
		type = OutputFileType.SOURCE;
		setName(CodeGenHelper.getRandomName(32));
	}
	
	public OutputFileType getType() {
		return type;
	}
	
	public Code setType(OutputFileType type) {
		this.type = type;
		return this;
	}
	
	public Code setCode(String code) {
		this.code = code;
		return this;
	}
	
	public Code setCode(String... lines) {
		return setCode(CodeGenHelper.concatLines(lines));
	}
	
	public Code appendCode(String code) {
		if (this.code == null)
			this.code = code;
		else
			this.code += '\n' + code;
		
		return this;
	}
	
	public Code appendCode(String... lines) {
		return appendCode(CodeGenHelper.concatLines(lines));
	}
	
	public String getCode() {
		return code;
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
