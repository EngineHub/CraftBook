package com.sk89q.craftbook.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.MechanicDataCache;
import com.sk89q.craftbook.core.mechanics.MechanicData;

/**
 * The core class for all implementations of the CraftBook Core.
 */
public abstract class CraftBookAPI {

    private static CraftBookAPI instance;

    private Map<String, Class<? extends Mechanic>> availableMechanics = new HashMap<String, Class<? extends Mechanic>>();

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
     * Registers a mechanic to the available mechanic registry.
     *
     * @param name The name to register the mechanic under
     * @param mechanic The mechanic class to register
     * @return If the mechanic was registered successfully
     */
    public boolean registerMechanic(String name, Class<? extends Mechanic> mechanic) {

        if (mechanic == null) return false;

        return availableMechanics.put(name, mechanic) == null;
    }

    /**
     * Creates a mechanic instance based on a class.
     *
     * @param clazz The mechanic class to create a mechanic based on
     * @return The created mechanic
     * @throws InstantiationException If the class failed to generate an instance
     * @throws IllegalAccessException If the class was not accessible
     * @throws CraftBookException If the mechanic was unable to be initialized
     */
    public Mechanic createMechanic(Class<? extends Mechanic> clazz) throws InstantiationException, IllegalAccessException, CraftBookException {

        Mechanic mechanic = clazz.newInstance();

        mechanic.onInitialize();

        return mechanic;
    }

    /**
     * Gets a collection of all available mechanics.
     *
     * @return A collection of available mechanics
     */
    public Set<Entry<String, Class<? extends Mechanic>>> getAvailableMechanics() {

        return availableMechanics.entrySet();
    }

    /**
     * Gets the cache that stores {@link MechanicData}.
     *
     * @return The {@link MechanicDataCache}
     */
    public abstract MechanicDataCache getCache();
}
