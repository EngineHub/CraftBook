package com.sk89q.craftbook.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.MechanicDataCache;

public abstract class CraftBookAPI {

    public static CraftBookAPI instance;

    private Map<String, Class<? extends Mechanic>> availableMechanics = new HashMap<String, Class<? extends Mechanic>>();

    @SuppressWarnings("unchecked")
    public static <T extends CraftBookAPI> T inst() {

        return (T) instance;
    }

    public abstract void discoverMechanics();

    public boolean registerMechanic(String name, Class<? extends Mechanic> mechanic) {

        if(mechanic == null) return false;

        return availableMechanics.put(name, mechanic) == null;
    }

    public Mechanic createMechanic(Class<? extends Mechanic> clazz) throws InstantiationException, IllegalAccessException, CraftBookException {

        Mechanic mechanic = clazz.newInstance();

        mechanic.onInitialize();

        return mechanic;
    }

    /**
     * Gets a collection of all available mechanics.
     */
    public Set<Entry<String, Class<? extends Mechanic>>> getAvailableMechanics() {

        return availableMechanics.entrySet();
    }

    public abstract MechanicDataCache getCache();
}