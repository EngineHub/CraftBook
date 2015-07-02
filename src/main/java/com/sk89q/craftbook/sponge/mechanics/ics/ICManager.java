package com.sk89q.craftbook.sponge.mechanics.ics;

import java.util.HashSet;
import java.util.Set;

import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.Clock;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.Inverter;
import com.sk89q.craftbook.sponge.mechanics.ics.chips.logic.Repeater;

public class ICManager {

    public static Set<ICType<? extends IC>> registeredICTypes = new HashSet<ICType<? extends IC>>();

    static {

        registerICType(new ICType<Repeater>("MC1000", "REPEATER", Repeater.class));
        registerICType(new ICType<Inverter>("MC1001", "INVERTER", Inverter.class));

        registerICType(new ICType<Clock>("MC1421", "CLOCK", Clock.class));
    }

    public static void registerICType(ICType<? extends IC> ic) {

        registeredICTypes.add(ic);
    }

    public static ICType<? extends IC> getICType(String id) {

        for (ICType<? extends IC> icType : registeredICTypes) {
            if (id.equalsIgnoreCase("[" + icType.modelId + "]") || id.equalsIgnoreCase("=" + icType.shorthandId)) return icType;
        }

        return null;
    }
}
