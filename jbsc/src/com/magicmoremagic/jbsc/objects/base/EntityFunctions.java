package com.magicmoremagic.jbsc.objects.base;

import java.util.*;

import com.magicmoremagic.jbsc.objects.Function;
import com.magicmoremagic.jbsc.objects.FunctionType;
import com.magicmoremagic.jbsc.objects.containers.Namespace;

public class EntityFunctions {
	
	public static class Mediator {
		
		private EntityFunctions that;
		
		private void connect(EntityFunctions functions) {
			if (that != null)
				throw new IllegalArgumentException("This mediator has already been connected to another EntityFunctions object!");
			
			that = functions;
		}
		
		public void put(FunctionType type, Function func) {
			that.functions.put(type, func);
			that.onParentChanged(that.parent);
		}
		
		public void calculateNames() {
			that.calculateNames();
		}
		
		public void setNamespace(Namespace newParent) {
			AbstractEntity oldParent = that.parent;
			that.parent = newParent;
			that.onParentChanged(oldParent);
		}
	}
	
	protected Namespace parent;
	protected Map<FunctionType, Function> functions;
	protected Collection<Function> unmodFunctions;
	
	public EntityFunctions(Mediator mediator) {
		functions = new EnumMap<>(FunctionType.class);
		unmodFunctions = Collections.unmodifiableCollection(functions.values());
		mediator.connect(this);
	}
	
	public Function get(FunctionType type) {
		return functions.get(type);
	}
	
	public Collection<Function> get() {
		return unmodFunctions;
	}
	
	public Namespace getNamespace() {
		return parent;
	}
	
	protected void onParentChanged(AbstractEntity oldParent) {
		if (parent != null) {
			for (Function func : functions.values()) {
				parent.addChild(func);
			}
		} else if (oldParent != null){
			for (Function func : functions.values()) {
				oldParent.removeChild(func);
			}
		}
	}
	
	protected void calculateNames() {
		for (Function func : functions.values()) {
			func.setName(func.calculateDefaultName());
		}
	}
	
}
