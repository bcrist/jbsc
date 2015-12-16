package com.magicmoremagic.jbsc.visitors.collections;

import java.io.PrintWriter;
import java.util.Set;

import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.visitors.base.*;

public interface IOutputFileVisitors {

	AbstractSelectionVisitor<Entity> getOutputSelectionVisitor();
	AbstractShouldPrintVisitor getShouldPrintVisitor(Set<Entity> entitiesToPrint);
	IEntityVisitor getPrintVisitor(PrintWriter writer, Set<Entity> entitiesToPrint);
	
}
