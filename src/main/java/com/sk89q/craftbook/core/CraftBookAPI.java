package com.sk89q.craftbook.core;

import com.sk89q.craftbook.core.mechanics.MechanicData;
import com.sk89q.craftbook.core.util.MechanicDataCache;

/**
 * The core class for all implementations of the CraftBook Core.
 */
public abstract class CraftBookAPI {

    private static CraftBookAPI instance;

    /**
     * Gets the current instance of the plugin.
     *
     * @param <T> The base plugin type.
     * @return The instance
     */
    @SuppressWarnings("unchecked")
    public static <T extends CraftBookAPI> T inst() {
        return (T) instance;
    }

    public static void setInstance(CraftBookAPI api) throws IllegalAccessException {
        if(instance != null) throw new IllegalAccessException("Illegal assignment of Instance.");
        instance = api;
    }

    /**
     * Called to discover available mechanics.
     */
    public abstract void discoverMechanics();

    /**
     * Gets the cache that stores {@link MechanicData}.
     *
     * @return The {@link MechanicDataCache}
     */
    public abstract MechanicDataCache getCache();
}
