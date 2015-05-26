package com.sk89q.craftbook.core;

import com.sk89q.craftbook.core.mechanics.MechanicData;
import com.sk89q.craftbook.core.util.CraftBookException;

/**
 * The base class for any CraftBook mechanic.
 */
public interface Mechanic {

    /**
     * Gets the name of the mechanic.
     *
     * @return The name
     */
    String getName();

    /**
     * Called when the mechanic should initialize.
     *
     * @throws CraftBookException Occurs when the mechanic can not be started.
     */
    void onInitialize() throws CraftBookException;

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
     <T extends MechanicData> T getData(Class<T> clazz, String locationKey);
}
