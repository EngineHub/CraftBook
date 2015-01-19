package com.sk89q.craftbook.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sk89q.craftbook.core.util.CraftBookException;

public abstract class CraftBookAPI {

    public static CraftBookAPI instance;

    private Set<Mechanic> availableMechanics = new HashSet<Mechanic>();

    @SuppressWarnings("unchecked")
    public static <T extends CraftBookAPI> T inst() {

        return (T) instance;
    }

    public abstract void discoverFactories();

    public boolean registerMechanic(Mechanic mechanic) {

        if(mechanic == null) return false;

        try {
            mechanic.onInitialize();
        } catch(CraftBookException e) {
            e.printStackTrace();
        }
        return availableMechanics.add(mechanic);
    }

    /**
     * Gets a collection of all available mechanics.
     */
    public Collection<Mechanic> getAvailableMechanics() {

        return availableMechanics;
    }
}