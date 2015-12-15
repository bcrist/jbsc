package com.magicmoremagic.jbsc.visitors;

import java.io.PrintWriter;
import java.util.Set;

import com.magicmoremagic.jbsc.objects.Function;
import com.magicmoremagic.jbsc.objects.base.Entity;

public class SourceVisitors implements IOutputFileVisitors {

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
			{ shouldPrint = true; } // TODO remove me!			
			
			@Override
			public int visit(Function function) {
				if (!function.isImplementationInline())
					shouldPrint = true;
				
				return super.visit(function);
			}

		};
	}

	@Override
	public IEntityVisitor getPrintVisitor(PrintWriter writer, Set<Entity> entitiesToPrint) {
		return new SourcePrintVisitor(writer, entitiesToPrint);
	}

}
