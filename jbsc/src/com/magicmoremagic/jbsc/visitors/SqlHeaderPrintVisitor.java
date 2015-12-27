package com.magicmoremagic.jbsc.visitors;

import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;

import com.magicmoremagic.jbsc.OutputFileType;
import com.magicmoremagic.jbsc.objects.base.AbstractEntity;
import com.magicmoremagic.jbsc.objects.base.IEntity;
import com.magicmoremagic.jbsc.objects.containers.Namespace;
import com.magicmoremagic.jbsc.objects.containers.Spec;
import com.magicmoremagic.jbsc.visitors.base.AbstractHeaderPrintVisitor;

public class SqlHeaderPrintVisitor extends AbstractHeaderPrintVisitor {

	private Set<AbstractEntity> statements = new LinkedHashSet<>();
	
	public SqlHeaderPrintVisitor(PrintWriter writer, Set<AbstractEntity> entitiesToPrint) {
		super(writer, entitiesToPrint);
	}

	@Override
	protected OutputFileType getOutputType() {
		return OutputFileType.SQL_HEADER;
	}

	@Override
	public int visit(Namespace namespace) {
		// everything's preprocessor here; no namespaces necessary
		return CONTINUE;
	}
	
	@Override
	public int leave(Namespace namespace) {
		// everything's preprocessor here; no namespaces necessary
		return CONTINUE;
	}
	
	
	@Override
	public int leave(Spec spec) {

		writer.println("#ifdef BE_ID_NAMES_ENABLED");
		writer.println();
		writer.println("// <[gen-ids in]>");
		writer.println();
		
		for (IEntity statement : statements) {
			// TODO SQL ID
		}
		
		writer.println();
		writer.println("// </[gen-ids in]>");
		writer.println();
		writer.println("#else");
		writer.println();
		writer.println("// <[gen-ids out]>");
		writer.println();
		//TODO calculate ID values automatically?
		writer.println();
		writer.println("// </[gen-ids out]>");
		writer.println();
		writer.println("#endif");
		writer.println();
		writer.println("// <[gen-ids outv]>");
		writer.println();
		//TODO calculate ID values automatically?
		writer.println();
		writer.println("// </[gen-ids outv]>");
		
		statements.clear();
		
		return super.leave(spec);
	}
	
}
