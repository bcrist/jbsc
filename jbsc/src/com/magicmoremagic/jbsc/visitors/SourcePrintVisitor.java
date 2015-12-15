package com.magicmoremagic.jbsc.visitors;

import java.io.PrintWriter;
import java.util.*;

import com.magicmoremagic.jbsc.OutputFileType;
import com.magicmoremagic.jbsc.objects.Function;
import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.objects.containers.Spec;
import com.magicmoremagic.jbsc.util.CodeGenConfig;
import com.magicmoremagic.jbsc.util.CodeGenHelper;

public class SourcePrintVisitor extends AbstractPrintVisitor {

	public SourcePrintVisitor(PrintWriter writer, Set<Entity> entitiesToPrint) {
		super(writer, entitiesToPrint);
	}

	@Override
	protected OutputFileType getOutputType() {
		return OutputFileType.SOURCE;
	}
	
	@Override
	public int visit(Spec spec) {
		super.visit(spec);
		printInclude(spec);
		return CONTINUE;
	}
	
	protected void printInclude(Spec spec) {
		writer.print("#include \"");
		writer.print(spec.getOutputFileName(OutputFileType.HEADER));
		writer.println("\"");
		writer.println();
		
		AbstractSelectionVisitor<String> visitor = new GetImplementationIncludesVisitor(entitiesToPrint);
		spec.acceptVisitor(visitor);
		
		List<String> includes = new ArrayList<String>(visitor.getSelections());
		if (!includes.isEmpty()) {
			Collections.sort(includes);
			
			for (String include : includes) {
				writer.print("#include ");
				writer.println(include);
			}
			
			writer.println();
		}
	}
	
	@Override
	public int visit(Function function) {
		if (!function.isImplementationInline()) {
			CodeGenHelper.printCode(CodeGenConfig.FUNCTION_IMPL_PREFIX, writer);
			function.printImplementation(writer);
			writer.println();
		}
		
		return CONTINUE;
	}
	
}
