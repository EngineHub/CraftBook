package com.sk89q.craftbook.core;

import java.util.HashSet;
import java.util.Set;

import com.sk89q.craftbook.core.util.CraftBookException;

public abstract class CraftBookAPI {

    public static CraftBookAPI instance;

    private Set<Class<? extends Mechanic>> availableMechanics = new HashSet<Class<? extends Mechanic>>();

    @SuppressWarnings("unchecked")
    public static <T extends CraftBookAPI> T inst() {

        return (T) instance;
    }

    public abstract void discoverMechanics();

    public boolean registerMechanic(Class<? extends Mechanic> mechanic) {

        if(mechanic == null) return false;

        return availableMechanics.add(mechanic);
    }

    public Mechanic createMechanic(Class<? extends Mechanic> clazz) throws InstantiationException, IllegalAccessException, CraftBookException {

        Mechanic mechanic = clazz.newInstance();

        mechanic.onInitialize();

        return mechanic;
    }

    /**
     * Gets a collection of all available mechanics.
     */
    public Set<Class<? extends Mechanic>> getAvailableMechanics() {

        return availableMechanics;
    }
}