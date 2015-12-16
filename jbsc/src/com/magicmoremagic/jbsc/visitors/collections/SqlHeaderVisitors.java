package com.magicmoremagic.jbsc.visitors.collections;

import java.io.PrintWriter;
import java.util.Set;

import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.visitors.*;
import com.magicmoremagic.jbsc.visitors.base.*;

public class SqlHeaderVisitors implements IOutputFileVisitors {
	
	@Override
	public AbstractSelectionVisitor<Entity> getOutputSelectionVisitor() {
		return new AbstractSelectionVisitor<Entity>() {
			@Override
			public Set<Entity> getSelections() { return null; }
		};
	}
	
	@Override
	public AbstractShouldPrintVisitor getShouldPrintVisitor(Set<Entity> entitiesToPrint) {
		return new AbstractShouldPrintVisitor(entitiesToPrint) {
			{ shouldPrint = true; } // TODO remove me
		};
	}

	@Override
	public IEntityVisitor getPrintVisitor(PrintWriter writer, Set<Entity> entitiesToPrint) {
		return new SqlHeaderPrintVisitor(writer, entitiesToPrint);
	}

}
