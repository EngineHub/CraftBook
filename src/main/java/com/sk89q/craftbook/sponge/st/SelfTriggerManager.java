package com.sk89q.craftbook.sponge.st;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;

import com.sk89q.craftbook.sponge.CraftBookPlugin;

public class SelfTriggerManager {

    private static Map<Location, SelfTriggeringMechanic> selfTriggeringMechanics = new HashMap<Location, SelfTriggeringMechanic>();

    public static void initialize() {
        CraftBookPlugin.game.getSyncScheduler().runRepeatingTask(CraftBookPlugin.<CraftBookPlugin> inst().container, new SelfTriggerClock(), 2L);
    }

    public static void register(SelfTriggeringMechanic mechanic, Location location) {
        selfTriggeringMechanics.put(location, mechanic);
    }

    public static void unregisterAll(Extent extent) {
        // TODO Make some API to make this super easy.
    }

    public static void think() {
        for (Entry<Location, SelfTriggeringMechanic> entry : selfTriggeringMechanics.entrySet()) {
            entry.getValue().onThink(entry.getKey());
        }
    }
}
