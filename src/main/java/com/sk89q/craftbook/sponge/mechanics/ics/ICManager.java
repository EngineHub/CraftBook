package com.sk89q.craftbook.sponge.mechanics.ics;

import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.AndGate;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.Clock;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.Inverter;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.Repeater;

import java.util.HashSet;
import java.util.Set;

public class ICManager {

    public static Set<ICType<? extends IC>> registeredICTypes = new HashSet<>();

    static {

        registerICType(new ICType<>("MC1000", "REPEATER", Repeater.class));
        registerICType(new ICType<>("MC1001", "INVERTER", Inverter.class));

        registerICType(new ICType<>("MC1421", "CLOCK", Clock.class));

        registerICType(new ICType<>("MC3002", "AND", AndGate.class, "3ISO"));
    }

    public static void registerICType(ICType<? extends IC> ic) {

        registeredICTypes.add(ic);
    }

    public static ICType<? extends IC> getICType(String id) {

        for (ICType<? extends IC> icType : registeredICTypes) {
            if (id.equalsIgnoreCase("[" + icType.modelId + "]") || id.equalsIgnoreCase("=" + icType.shorthandId) || id.equalsIgnoreCase("[" + icType.modelId + "]S") || id.equalsIgnoreCase("=" + icType.shorthandId + " ST")) return icType;
        }

        return null;
    }
}
