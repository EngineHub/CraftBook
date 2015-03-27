package com.sk89q.craftbook.core;

import com.sk89q.craftbook.core.mechanics.MechanicData;
import com.sk89q.craftbook.core.util.CraftBookException;

public interface Mechanic {

    public String getName();

    public void onInitialize() throws CraftBookException;

    /**
     * Gets the persistent data for this mechanic, using the given location key.
     * <p>
     * The location key should be able to identify each individual block in a world, as well as the world that it exists in. The location does not,
     * however, have to refer to a location inside a world. The data could exist inside an item, or inside an entity.
     * </p>
     * 
     * @param locationKey The locationKey.
     * @return The data
     */
    public <T extends MechanicData> T getData(Class<T> clazz, String locationKey);
}
