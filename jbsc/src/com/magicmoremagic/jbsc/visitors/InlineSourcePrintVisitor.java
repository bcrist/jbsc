package com.magicmoremagic.jbsc.visitors;

import java.io.PrintWriter;
import java.util.Set;

import com.magicmoremagic.jbsc.OutputFileType;
import com.magicmoremagic.jbsc.objects.Function;
import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.objects.containers.Spec;
import com.magicmoremagic.jbsc.util.CodeGenConfig;
import com.magicmoremagic.jbsc.util.CodeGenHelper;
import com.magicmoremagic.jbsc.visitors.base.AbstractPrintVisitor;

public class InlineSourcePrintVisitor extends AbstractPrintVisitor {

	public InlineSourcePrintVisitor(PrintWriter writer, Set<Entity> entitiesToPrint) {
		super(writer, entitiesToPrint);
	}

	@Override
	protected OutputFileType getOutputType() {
		return OutputFileType.INLINE_SOURCE;
	}
	
	@Override
	public int visit(Spec spec) {
		super.visit(spec);
		printGuard(spec);
		return CONTINUE;
	}
	
	protected void printGuard(Spec spec) {
		String guardName = getGuard(spec, getOutputType());
		String headerGuardName = getGuard(spec, OutputFileType.HEADER);
		
		writer.print("#if !defined(");
		writer.print(headerGuardName);
		writer.println(") && !defined(DOXYGEN)");
		
		writer.print("#include \"");
		writer.print(spec.getOutputFileName(OutputFileType.HEADER));
		writer.println("\"");
		
		writer.print("#elif !defined(");
		writer.print(guardName);
		writer.println(")");
		
		writer.print("#define ");
		writer.println(guardName);
		writer.println();
	}
	
	@Override
	public int visit(Function function) {
		if (function.isImplementationInline()) {
			CodeGenHelper.printCode(CodeGenConfig.FUNCTION_IMPL_PREFIX, writer);
			function.printImplementation(writer);
			writer.println();
		}
		
		return CONTINUE;
	}
	
	@Override
	public int leave(Spec spec) {
		printGuardEnd(spec);
		return CONTINUE;
	}
	
	protected void printGuardEnd(Spec spec) {
		writer.println("#endif");
		writer.println();
	}
	
	
}
