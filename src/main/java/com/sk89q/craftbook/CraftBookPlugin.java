package com.sk89q.craftbook;

import java.lang.reflect.Field;
import java.util.Vector;

import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.ServerStartingEvent;
import org.spongepowered.api.plugin.Plugin;

import com.sk89q.craftbook.mechanics.Mechanic;

@Plugin(id = "CraftBook", name = "CraftBook", version = "4.0")
public class CraftBookPlugin {

	@Subscribe
	public void onServerStart(ServerStartingEvent event) {
		
		searchClasspath();
	}
	
	public void searchClasspath() {
		
		try {
			Field f = ClassLoader.class.getDeclaredField("classes");
			  f.setAccessible(true);

			  Vector<Class> classes =  (Vector<Class>) f.get(getClass().getClassLoader());
			  
			  for(Class clazz : classes) {
				  if(clazz.isAnnotationPresent(Mechanic.class)) { //It's a mechanic!
					  
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