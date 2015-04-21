package com.sk89q.craftbook.sponge.mechanics.types;

import org.spongepowered.api.world.Location;

public abstract class SpongeBlockMechanic extends SpongeMechanic {

	public abstract boolean isValid(Location location);
}
