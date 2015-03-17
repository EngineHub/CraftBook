package com.sk89q.craftbook.sponge.mechanics.integratedcircuits;

import java.util.HashSet;
import java.util.Set;

public class ICManager {

	public static Set<ICType<? extends IC>> registeredICTypes = new HashSet<ICType<? extends IC>>();
	
	static {
		
		registerICType(new ICType<IC>("MC1000", "REPEATER", IC.class)); //TODO 
	}
	
	public static void registerICType(ICType<? extends IC> ic) {
		
		registeredICTypes.add(ic);
	}
}
