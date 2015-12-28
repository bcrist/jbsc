package com.magicmoremagic.jbsc.visitors;

import java.io.PrintWriter;
import java.util.*;

import com.magicmoremagic.jbsc.OutputFileType;
import com.magicmoremagic.jbsc.objects.Function;
import com.magicmoremagic.jbsc.objects.base.AbstractEntity;
import com.magicmoremagic.jbsc.objects.containers.Spec;
import com.magicmoremagic.jbsc.objects.types.AggregateType;
import com.magicmoremagic.jbsc.util.CodeGenConfig;
import com.magicmoremagic.jbsc.util.CodeGenHelper;
import com.magicmoremagic.jbsc.visitors.base.AbstractHeaderPrintVisitor;
import com.magicmoremagic.jbsc.visitors.base.AbstractSelectionVisitor;

public class HeaderPrintVisitor extends AbstractHeaderPrintVisitor {
	
	public HeaderPrintVisitor(PrintWriter writer, Set<AbstractEntity> entitiesToPrint) {
		super(writer, entitiesToPrint);
	}
	
	@Override
	protected OutputFileType getOutputType() {
		return OutputFileType.HEADER;
	}

	@Override
	public int visitSpec(Spec spec) {
		super.visitSpec(spec);		
		printIncludes(spec);
		return CONTINUE;
	}
	
	protected void printIncludes(Spec spec) {
		AbstractSelectionVisitor<String> visitor = new GetRequiredIncludesVisitor(OutputFileType.HEADER, entitiesToPrint);
		spec.visit(visitor);
		
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
	public int visitFunction(Function function) {
		CodeGenHelper.printCode(CodeGenConfig.FUNCTION_DECL_PREFIX, writer);
		function.printDeclaration(writer);
		writer.println();
		return CONTINUE;
	}
	
	
	@Override
	public int leaveSpec(Spec spec) {
		printInl(spec);
		super.leaveSpec(spec);
		return CONTINUE;
	}
	
	protected void printInl(Spec spec) {
		if (OutputFileType.INLINE_SOURCE.shouldPrintCode(spec)) {
			writer.print("#include \"");
			writer.print(spec.getOutputFileName(OutputFileType.INLINE_SOURCE));
			writer.println("\"");
			writer.println();
		}
	}
	
	@Override
	protected void printGuardEnd(Spec spec) {
		writer.println("#endif");
		writer.println();
	}

	@Override
	public int visitAggregateType(AggregateType aggregateType) {
		aggregateType.printClassDeclaration(writer);
		return CONTINUE;
	}
	
}
