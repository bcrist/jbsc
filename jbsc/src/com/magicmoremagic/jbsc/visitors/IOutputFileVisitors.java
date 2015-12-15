package com.magicmoremagic.jbsc.visitors;

import java.io.PrintWriter;
import java.util.Set;

import com.magicmoremagic.jbsc.objects.base.Entity;

public interface IOutputFileVisitors {

	AbstractSelectionVisitor<Entity> getOutputSelectionVisitor();
	AbstractShouldPrintVisitor getShouldPrintVisitor(Set<Entity> entitiesToPrint);
	IEntityVisitor getPrintVisitor(PrintWriter writer, Set<Entity> entitiesToPrint);
	
}
