package com.magicmoremagic.jbsc.objects.containers;

import java.nio.file.Path;
import java.util.*;

import com.magicmoremagic.jbsc.OutputFileType;
import com.magicmoremagic.jbsc.objects.base.Entity;
import com.magicmoremagic.jbsc.objects.base.EntityContainer;
import com.magicmoremagic.jbsc.visitors.IEntityVisitor;

public class Spec extends EntityContainer {

	private int errorCount;
	private int warningCount;
	private Path inputPath;
	private Set<Spec> includedSpecs;
	private Set<Spec> unmodIncludedSpecs;
	
	public Spec() {
		includedSpecs = new LinkedHashSet<>();
		unmodIncludedSpecs = Collections.unmodifiableSet(includedSpecs);
	}
	
	@Override
	protected void initRequiredIncludes() {
		super.initRequiredIncludes();
		requiredIncludes.add("\"be/_be.hpp\"");
	}
	
	@Override
	public Spec getSpec() {
		return this;
	}
	
	@Override
	public Entity setSpec(Spec spec) {
		if (spec != this)
			throw new UnsupportedOperationException();
		
		return this;
	}
	
	@Override
	protected boolean shouldIncludeNameInQualifiedNames() {
		return false;
	}
	
	@Override
	public String getUnqualifiedCodeName() {
		return null;
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
	
	@Override
	public Entity lookupName(String name) {
		{
			// look in this spec first
			Entity entity = super.lookupName(name);
			if (entity != null)
				return entity;
		}
		
		// look in included specs
		for (Spec includedSpec : includedSpecs) {
			Entity entity = includedSpec.lookupName(name);
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
