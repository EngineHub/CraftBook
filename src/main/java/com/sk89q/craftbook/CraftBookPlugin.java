package com.sk89q.craftbook;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.ServerStartedEvent;
import org.spongepowered.api.plugin.Plugin;

import com.sk89q.craftbook.mechanics.Mechanic;
import com.sk89q.craftbook.mechanics.MechanicContainer;

@Plugin(id = "CraftBook", name = "CraftBook", version = "4.0")
public class CraftBookPlugin {

	Set<MechanicContainer> availableMechanics = new HashSet<MechanicContainer>();

	Set<MechanicContainer> enabledMechanics = new HashSet<MechanicContainer>();

	@Subscribe
	public void onServerStarted(ServerStartedEvent event) {

		searchClasspath();

		for(MechanicContainer mech : availableMechanics) {

			//TODO is enabled check.

			enabledMechanics.add(mech);
		}
	}

	@SuppressWarnings("unchecked")
	public void searchClasspath() {

		try {
			Field f = ClassLoader.class.getDeclaredField("classes");
			f.setAccessible(true);

			Vector<Class<?>> classes =  (Vector<Class<?>>) f.get(getClass().getClassLoader());

			for(Class<?> clazz : classes) {
				if(clazz.isAnnotationPresent(Mechanic.class)) { //It's a mechanic!
					availableMechanics.add(new MechanicContainer(clazz));
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}