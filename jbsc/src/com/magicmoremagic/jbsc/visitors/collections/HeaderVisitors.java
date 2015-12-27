package com.magicmoremagic.jbsc.visitors.collections;

import java.io.PrintWriter;
import java.util.Set;

import com.magicmoremagic.jbsc.objects.Function;
import com.magicmoremagic.jbsc.objects.base.AbstractEntity;
import com.magicmoremagic.jbsc.visitors.*;
import com.magicmoremagic.jbsc.visitors.base.*;

public class HeaderVisitors implements IOutputFileVisitors {

	@Override
	public AbstractSelectionVisitor<AbstractEntity> getOutputSelectionVisitor() {
		return new AbstractSelectionVisitor<AbstractEntity>() {
			@Override
			public Set<AbstractEntity> getSelections() { return null; }
		};
	}
	
	@Override
	public AbstractShouldPrintVisitor getShouldPrintVisitor(Set<AbstractEntity> entitiesToPrint) {
		return new AbstractShouldPrintVisitor(entitiesToPrint) {
			{ shouldPrint = true; } // TODO remove me!
			
			@Override
			public int visit(Function function) {
				shouldPrint = true;
				return super.visit(function);
			}
			
		};
	}

	@Override
	public IEntityVisitor getPrintVisitor(PrintWriter writer, Set<AbstractEntity> entitiesToPrint) {
		return new HeaderPrintVisitor(writer, entitiesToPrint);
	}

}
