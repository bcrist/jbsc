package com.magicmoremagic.jbsc.visitors.base;

import java.io.PrintWriter;
import java.util.Set;

import com.magicmoremagic.jbsc.objects.base.AbstractEntity;
import com.magicmoremagic.jbsc.objects.containers.Spec;

public abstract class AbstractHeaderPrintVisitor extends AbstractPrintVisitor {

	public AbstractHeaderPrintVisitor(PrintWriter writer, Set<AbstractEntity> entitiesToPrint) {
		super(writer, entitiesToPrint);
	}
	
	@Override
	public int visitSpec(Spec spec) {
		super.visitSpec(spec);
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
	public int leaveSpec(Spec spec) {
		printGuardEnd(spec);
		return CONTINUE;
	}
	
	protected void printGuardEnd(Spec spec) {
		writer.println();
		writer.println("#endif");
		writer.println();
	}

}
