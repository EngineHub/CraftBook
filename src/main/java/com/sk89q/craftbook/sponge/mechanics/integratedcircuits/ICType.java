package com.sk89q.craftbook.sponge.mechanics.integratedcircuits;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ICType<T extends IC> {

	String modelId;
	String shorthandId;
	String defaultPinset;
	
	Class<T> icClass;
	Object[] extraArguments = new Object[0];
	Class<?>[] argumentTypes = new Class<?>[1];
	
	public ICType(String modelId, String shorthandId, Class<T> icClass) {
		this.modelId = modelId;
		this.shorthandId = shorthandId;
		this.icClass = icClass;
		argumentTypes[0] = ICType.class;
	}
	
	public ICType(String modelId, String shorthandId, Class<T> icClass, String defaultPinset) {
		this(modelId, shorthandId, icClass);
		this.defaultPinset = defaultPinset;
	}
	
	public ICType<T> setExtraArguments(Object ... args) {
		extraArguments = args;
		
		argumentTypes = new Class<?>[extraArguments.length + 1];
		argumentTypes[0] = ICType.class;
		int num = 1;
		for(Object obj : args)
			argumentTypes[num ++] = obj.getClass();
		
		return this;
	}
	
    public String getDefaultPinSet() {
        return defaultPinset != null ? defaultPinset : "SISO";
    }
    
    public IC buildIC() {
    	
    	try {
			Constructor<? extends IC> construct = icClass.getConstructor(argumentTypes);
			IC ic = construct.newInstance(this, extraArguments);
			
			return ic;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
    	
    	return null;
    }
}
