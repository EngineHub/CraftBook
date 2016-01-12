package com.sk89q.craftbook.sponge.mechanics.types;

import org.spongepowered.api.world.Location;

public abstract class SpongeBlockMechanic extends SpongeMechanic {

    /**
     * Gets whether the chosen block is an instance of this mechanic.
     * <p>
     * Whilst this can be used internally by the mechanic, that may not be the most efficient thing to do.
     * </p>
     * 
     * @param location The location to check at.
     * @return If the block is an instance of this mechanic
     */
    public abstract boolean isValid(Location location);
}
