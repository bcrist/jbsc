package com.magicmoremagic.jbsc.visitors.collections;

import java.io.PrintWriter;
import java.util.Set;

import com.magicmoremagic.jbsc.objects.base.AbstractEntity;
import com.magicmoremagic.jbsc.visitors.base.*;

public interface IOutputFileVisitors {

	AbstractSelectionVisitor<AbstractEntity> getOutputSelectionVisitor();
	AbstractShouldPrintVisitor getShouldPrintVisitor(Set<AbstractEntity> entitiesToPrint);
	IEntityVisitor getPrintVisitor(PrintWriter writer, Set<AbstractEntity> entitiesToPrint);
	
}
