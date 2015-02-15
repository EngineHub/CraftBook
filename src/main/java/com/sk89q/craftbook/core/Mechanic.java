package com.sk89q.craftbook.core;

import com.sk89q.craftbook.core.mechanics.MechanicData;
import com.sk89q.craftbook.core.util.CraftBookException;

public interface Mechanic {

    public String getName();

    public void onInitialize() throws CraftBookException;

    /**
     * Gets the persistent data for this mechanic, using the given location key.
     * 
     * <p>
     *  The location key should be able to identify each individual block in a world, as well as the world that it exists in.
     * </p>
     * 
     * @param locationKey The locationKey.
     * @return The data
     */
    public MechanicData getData(String locationKey);
}
