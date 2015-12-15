package com.magicmoremagic.jbsc.objects;

import java.io.PrintWriter;
import java.util.Collection;

import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.util.CodeGenHelper;
import com.magicmoremagic.jbsc.util.IndentingPrintWriter;
import com.magicmoremagic.jbsc.visitors.IEntityVisitor;

public abstract class Function extends Entity {

	protected String code;
	
	public abstract FunctionType getFunctionType();
	
	public Function setCode(String code) {
		this.code = code;
		return this;
	}
	
	public Function setCode(String... lines) {
		return setCode(CodeGenHelper.concatLines(lines));
	}
	
	public Function appendCode(String code) {
		if (this.code == null)
			this.code = code;
		else
			this.code += '\n' + code;
		
		return this;
	}
	
	public Function appendCode(String... lines) {
		return appendCode(CodeGenHelper.concatLines(lines));
	}
	
	public String getCode() {
		return code;
	}
	
	public String getGeneratedCode() {
		String code = getCode();
		if (code == null) {
			code = getDefaultCode();
		}
		return code;
	}
	
	public abstract String getDefaultCode();
	
	public abstract boolean isImplementationInline();
	
	public abstract Function printDeclaration(PrintWriter writer);
	public abstract Function printImplementation(PrintWriter writer);
	
	public abstract Collection<? extends Function> getDependencies();
	
	protected void printFunctionBodyOpen(PrintWriter writer) {
		writer.print("{");
		if (writer instanceof IndentingPrintWriter) {
			((IndentingPrintWriter)writer).indent();
		}
		writer.println();
	}
	
	protected void printGeneratedCode(PrintWriter writer) {
		String code = getGeneratedCode();
		if (code != null) {
			CodeGenHelper.printCode(code, writer);
		}
	}
	protected void printFunctionBodyClose(PrintWriter writer) {
		if (writer instanceof IndentingPrintWriter) {
			((IndentingPrintWriter)writer).unindent();
		}
		writer.println();
		writer.println("}");
	}
	
	protected void printFunctionBody(PrintWriter writer) {
		printFunctionBodyOpen(writer);
		printGeneratedCode(writer);
		printFunctionBodyClose(writer);
	}
	
	protected void printFunctionBodyWithReturnTrue(PrintWriter writer) {
		printFunctionBodyOpen(writer);
		printGeneratedCode(writer);
		writer.println();
		writer.print("return true;");
		printFunctionBodyClose(writer);
	}
	
	@Override
	public int acceptVisitor(IEntityVisitor visitor) {
		int result = super.acceptVisitor(visitor);
		if ((result & (IEntityVisitor.CANCEL_THIS | IEntityVisitor.STOP)) == 0) {
			result |= visitor.visit(this);
		}
		return result;
	}
}
