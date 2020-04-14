package org.enginehub.craftbook.mechanic;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MechanicManager {

    private Map<String, Class<? extends Mechanic>> availableMechanics = new HashMap<>();
    private Collection<Mechanic> enabledMechanics = new ArrayList<>();

    public MechanicManager() {
        setup();
    }

    private void setup() {

    }

    /**
     * Gets a list of all enabled mechanics.
     *
     * <p>
     *     Note: This list is immutable and should not be
     *     modified. To enable a mechanic, call //TODO
     * </p>
     *
     * @return A list of enabled mechanics
     */
    public Collection<Mechanic> getEnabledMechanics() {
        return ImmutableList.copyOf(enabledMechanics);
    }

    /**
     * Gets whether this mechanic is enabled.
     *
     * @param mechanicClass The mechanic class
     * @return Whether the mechanic is enabled
     */
    public boolean isMechanicEnabled(Class<? extends Mechanic> mechanicClass) {
        return getMechanic(mechanicClass).isPresent();
    }

    /**
     * Gets the instance of this mechanic class, if enabled.
     *
     * @param mechanicClass The mechanic class
     * @param <T> The type of the mechanic
     * @return The mechanic, if enabled
     */
    @SuppressWarnings("unchecked")
    public <T extends Mechanic> Optional<T> getMechanic(Class<T> mechanicClass) {
        for (Mechanic mech : enabledMechanics) {
            if (mech.getClass().equals(mechanicClass)) {
                // If mech.getClass() == Class<T>, we can assume mech is of type T
                return Optional.of((T) mech);
            }
        }
        return Optional.empty();
    }
}
