package com.magicmoremagic.jbsc.visitors;

import java.io.PrintWriter;

import com.magicmoremagic.jbsc.objects.base.*;
import com.magicmoremagic.jbsc.util.IndentingPrintWriter;
import com.magicmoremagic.jbsc.visitors.base.AbstractEntityVisitor;

public class PrintHierarchyVisitor extends AbstractEntityVisitor {

	private PrintWriter writer;
	private IEntity initialEntity;
	
	public PrintHierarchyVisitor(PrintWriter writer) {
		this.writer = writer;
	}
	
	@Override
	public int init(IEntity entity) {
		initialEntity = entity;
		return CONTINUE;
	}
	
	@Override
	public int visitAbstractEntity(AbstractEntity entity) {
		
		if (entity != initialEntity) {
			writer.println();
		}
		
		writer.print(entity.getClass().getSimpleName());
		writer.print(" ");
		writer.print(entity.getName());
		writer.print(" ");
		
		return CONTINUE;
	}
	
	@Override
	public int visitAbstractContainer(AbstractContainer container) {
		writer.print("{");
		if (writer instanceof IndentingPrintWriter)
			((IndentingPrintWriter)writer).indent();
		return CONTINUE;
	}
	
	@Override
	public int leaveAbstractContainer(AbstractContainer container) {
		if (writer instanceof IndentingPrintWriter)
			((IndentingPrintWriter)writer).unindent();
		
		if (!container.getChildren().isEmpty())
			writer.println();
		
		writer.print("}");
		
		if (container == initialEntity) {
			writer.println();
		}
		
		return CONTINUE;
	}
	
}
