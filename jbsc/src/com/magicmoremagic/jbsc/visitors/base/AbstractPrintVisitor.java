package com.magicmoremagic.jbsc.visitors.base;

import java.io.PrintWriter;
import java.util.*;

import com.magicmoremagic.jbsc.OutputFileType;
import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.objects.containers.Namespace;
import com.magicmoremagic.jbsc.objects.containers.Spec;
import com.magicmoremagic.jbsc.util.CodeGenConfig;
import com.magicmoremagic.jbsc.util.CodeGenHelper;

public abstract class AbstractPrintVisitor extends AbstractEntityVisitor {

	protected PrintWriter writer;
	protected Set<Entity> entitiesToPrint;
	
	public AbstractPrintVisitor(PrintWriter writer, Set<Entity> entitiesToPrint) {
		this.writer = writer;
		this.entitiesToPrint = entitiesToPrint;
	}
	
	protected abstract OutputFileType getOutputType();
	
	protected String getGuard(Spec spec, OutputFileType type) {
		String guardName = spec.getOutputFileName(type);
		guardName = guardName.replaceAll("[\\\\/.]", "_");
		guardName = guardName.toUpperCase(Locale.US);
		guardName = guardName + "_";
		return guardName;
	}
	
	@Override
	public int visit(Entity entity) {
		if (entitiesToPrint != null && !entitiesToPrint.contains(entity))
			return CANCEL_THIS | CANCEL_CHILDREN;
		
		return CONTINUE;
	}
	
	@Override
	public int visit(Spec spec) {
		printHeader(spec);
		return CONTINUE;
	}
	
	protected void printHeader(Spec spec) {
		String header = CodeGenConfig.OUTPUT_HEADER;
		header = header.replace("$(Filename)", spec.getOutputFileName(getOutputType()));
		header = header.replace("$(SpecFile)", spec.getInputPath() == null ? "???" : spec.getInputPath().getFileName().toString());
		header = header.replace("$(Date)", String.format("%1$tY-%1$tm-%1$td", new Date()));
		
		CodeGenHelper.printCode(header, writer);
	}
	
	@Override
	public int visit(Namespace namespace) {
		writer.print("namespace ");
		writer.print(namespace.getName());
		writer.println(" {");
		writer.println();
		return CONTINUE;
	}
	
	@Override
	public int leave(Namespace namespace) {
		writer.print("} // namespace ");
		writer.println(namespace.getQualifiedCodeName(null));
		writer.println();
		return CONTINUE;
	}

}
