package com.sk89q.craftbook.mechanics;

public class MechanicContainer {

	private Class<?> clazz;

	private Mechanic properties;

	public MechanicContainer(Class<?> clazz) {

		this.clazz = clazz;

		properties = clazz.getAnnotation(Mechanic.class);
	}

	public String getName() {

		return properties.name();
	}

	public Class<?> getMechanicClass() {

		return clazz;
	}
}