package com.sk89q.craftbook.sponge.mechanics.integratedcircuits;

import com.sk89q.craftbook.core.util.CachePolicy;
import com.sk89q.craftbook.sponge.mechanics.SpongeMechanic;

public class ICSocket extends SpongeMechanic {

	IC ic;

	/**
	 * Gets the IC that is in use by this IC Socket.
	 * 
	 * @return The IC
	 */
	public IC getIC() {
		return ic;
	}

	@Override
	public String getName() {
		return "IC";
	}

	@Override
	public CachePolicy getCachePolicy() {
		return null;
	}

}
