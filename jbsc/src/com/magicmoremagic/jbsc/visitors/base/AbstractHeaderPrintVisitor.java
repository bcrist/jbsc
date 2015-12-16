package com.magicmoremagic.jbsc.visitors.base;

import java.io.PrintWriter;
import java.util.Set;

import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.objects.containers.Spec;

public abstract class AbstractHeaderPrintVisitor extends AbstractPrintVisitor {

	public AbstractHeaderPrintVisitor(PrintWriter writer, Set<Entity> entitiesToPrint) {
		super(writer, entitiesToPrint);
	}
	
	@Override
	public int visit(Spec spec) {
		super.visit(spec);
		printGuard(spec);
		return CONTINUE;
	}
	
	protected void printGuard(Spec spec) {
		String guardName = getGuard(spec, getOutputType());
		
		writer.println("#pragma once");
		writer.print("#ifndef ");
		writer.println(guardName);
		writer.print("#define ");
		writer.println(guardName);
		writer.println();
	}
	
	@Override
	public int leave(Spec spec) {
		printGuardEnd(spec);
		return CONTINUE;
	}
	
	protected void printGuardEnd(Spec spec) {
		writer.println();
		writer.println("#endif");
		writer.println();
	}

}
