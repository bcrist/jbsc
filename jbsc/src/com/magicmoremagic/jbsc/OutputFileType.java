package com.magicmoremagic.jbsc;

import java.io.PrintWriter;
import java.util.Set;

import com.magicmoremagic.jbsc.objects.base.AbstractEntity;
import com.magicmoremagic.jbsc.objects.containers.Spec;
import com.magicmoremagic.jbsc.util.CodeGenConfig;
import com.magicmoremagic.jbsc.visitors.base.*;
import com.magicmoremagic.jbsc.visitors.collections.*;

public enum OutputFileType {

	SQL_HEADER(CodeGenConfig.SQL_HEADER_EXT, false, new SqlHeaderVisitors()),
	HEADER(CodeGenConfig.HEADER_EXT, false, new HeaderVisitors()),
	INLINE_SOURCE(CodeGenConfig.INLINE_SOURCE_EXT, false, new InlineSourceVisitors()),
	SOURCE(CodeGenConfig.SOURCE_EXT, true, new SourceVisitors()),
	;
	
	private String extension;
	private boolean usesSourceOutputDirectory;
	private IOutputFileVisitors outputFileVisitors;
	
	private OutputFileType(String extension, boolean usesSourceDir, IOutputFileVisitors outputFileVisitors) {
		this.extension = extension;
		this.usesSourceOutputDirectory = usesSourceDir;
		this.outputFileVisitors = outputFileVisitors;
	}
	
	public String getFileExtension() {
		return extension;
	}
	
	public boolean shouldUseSourceDirectory() {
		return usesSourceOutputDirectory;
	}
	
	public boolean shouldPrintCode(Spec spec) {
		AbstractSelectionVisitor<AbstractEntity> selectionVisitor = outputFileVisitors.getOutputSelectionVisitor();
		spec.visit(selectionVisitor);
		AbstractShouldPrintVisitor visitor = outputFileVisitors.getShouldPrintVisitor(selectionVisitor.getSelections());
		spec.visit(visitor);
		return visitor.shouldPrint();
	}
	
	public IEntityVisitor getPrintVisitor(PrintWriter writer, Set<AbstractEntity> entitiesToPrint) {
		return outputFileVisitors.getPrintVisitor(writer, entitiesToPrint);
	}
	
	public IEntityVisitor getPrintVisitor(Spec spec, PrintWriter writer) {
		AbstractSelectionVisitor<AbstractEntity> selectionVisitor = outputFileVisitors.getOutputSelectionVisitor();
		spec.visit(selectionVisitor);
		return outputFileVisitors.getPrintVisitor(writer, selectionVisitor.getSelections());
	}
	
}
