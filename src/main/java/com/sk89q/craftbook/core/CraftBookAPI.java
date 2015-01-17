package com.sk89q.craftbook.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sk89q.craftbook.core.util.CraftBookException;

public abstract class CraftBookAPI {

    public static CraftBookAPI instance;

    private Set<MechanicFactory<? extends Mechanic>> availableMechanics = new HashSet<MechanicFactory<? extends Mechanic>>();

    @SuppressWarnings("unchecked")
    public static <T extends CraftBookAPI> T inst() {

        return (T) instance;
    }

    public abstract void discoverFactories();

    public boolean registerMechanic(MechanicFactory<? extends Mechanic> factory) {

        if(factory == null) return false;

        try {
            factory.onRegister();
        } catch(CraftBookException e) {
            e.printStackTrace();
        }
        return availableMechanics.add(factory);
    }

    /**
     * Gets a collection of all available mechanics.
     */
    public Collection<MechanicFactory<? extends Mechanic>> getAvailableMechanics() {

        return availableMechanics;
    }
}