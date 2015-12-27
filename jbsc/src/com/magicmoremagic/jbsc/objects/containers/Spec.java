package com.magicmoremagic.jbsc.objects.containers;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.magicmoremagic.jbsc.OutputFileType;
import com.magicmoremagic.jbsc.objects.base.AbstractContainer;
import com.magicmoremagic.jbsc.objects.base.AbstractEntity;
import com.magicmoremagic.jbsc.objects.base.EntityFlags;
import com.magicmoremagic.jbsc.objects.base.EntityIncludes;
import com.magicmoremagic.jbsc.visitors.base.IEntityVisitor;

public class Spec extends AbstractContainer {

	private static final EntityIncludes HEADER_INCLUDES = new EntityIncludes() {{
		add("\"be/_be.hpp\"");
	}};
	
	private int errorCount;
	private int warningCount;
	private Path inputPath;
	private Set<Spec> includedSpecs;
	private Set<Spec> unmodIncludedSpecs;

	private EntityIncludes headerIncludes = new EntityIncludes(HEADER_INCLUDES, false);
	private EntityIncludes sourceIncludes = new EntityIncludes();
	
	public Spec() {
		super(new EntityFlags());
		includedSpecs = new LinkedHashSet<>();
		unmodIncludedSpecs = Collections.unmodifiableSet(includedSpecs);
	}
	
	@Override
	public String getCName() {
		return null;
	}
	
	@Override
	public EntityIncludes requiredIncludes(OutputFileType type) {
		switch (type) {
		case HEADER:
			return headerIncludes;
		case SOURCE:
			return sourceIncludes;
		default:
			return super.requiredIncludes(type);
		}
	}
	
	public int getErrorCount() {
		return errorCount;
	}

	public Spec setErrorCount(int errorCount) {
		this.errorCount = errorCount;
		return this;
	}

	public int getWarningCount() {
		return warningCount;
	}

	public Spec setWarningCount(int warningCount) {
		this.warningCount = warningCount;
		return this;
	}

	public Path getInputPath() {
		return inputPath;
	}

	public Spec setInputPath(Path inputPath) {
		this.inputPath = inputPath;
		return this;
	}

	public String getOutputFileName(OutputFileType type) {
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		sb.append(type.getFileExtension());
		return sb.toString();
	}
	
	public Set<Spec> getIncludedSpecs() {
		return unmodIncludedSpecs;
	}
		
	public boolean hasIncludedSpec(Spec spec) {
		if (includedSpecs.contains(spec))
			return true;
		
		for (Spec s : includedSpecs) {
			if (s.hasIncludedSpec(spec))
				return true;
		}
		
		return false;
	}
	
	public Spec addIncludedSpec(Spec spec) {
		includedSpecs.add(spec);
		return this;
	}
	
	public Spec removeIncludedSpec(Spec spec) {
		includedSpecs.remove(spec);
		return this;
	}
	
	public AbstractEntity lookupIncludedEntity(String qualifiedName) {
		for (Spec includedSpec : includedSpecs) {
			AbstractEntity entity = includedSpec.lookupEntity(qualifiedName, false);
			if (entity != null)
				return entity;
		}
		return null;
	}
	
	@Override
	protected int onVisitorVisit(IEntityVisitor visitor) {
		int result = super.onVisitorVisit(visitor);
		if ((result & (IEntityVisitor.CANCEL_THIS | IEntityVisitor.STOP)) == 0) {
			result |= visitor.visit(this);
		}
		return result;
	}
	
	@Override
	protected int onVisitorLeave(IEntityVisitor visitor) {
		int result = super.onVisitorLeave(visitor);
		if ((result & (IEntityVisitor.CANCEL_THIS | IEntityVisitor.STOP)) == 0) {
			result |= visitor.leave(this);
		}
		return result;
	}
	
}
