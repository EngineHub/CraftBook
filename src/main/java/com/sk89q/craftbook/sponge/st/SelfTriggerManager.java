package com.sk89q.craftbook.sponge.st;

import com.sk89q.craftbook.core.Mechanic;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.world.ChunkLoadEvent;
import org.spongepowered.api.event.world.ChunkUnloadEvent;
import org.spongepowered.api.event.world.WorldUnloadEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SelfTriggerManager {

    private static Map<Location, SelfTriggeringMechanic> selfTriggeringMechanics = new HashMap<Location, SelfTriggeringMechanic>();

    public static void initialize() {
        CraftBookPlugin.game.getScheduler().getTaskBuilder().interval(2L).execute(new SelfTriggerClock()).submit(CraftBookPlugin.inst());
        CraftBookPlugin.game.getEventManager().register(CraftBookPlugin.inst(), new SelfTriggerManager());
    }

    public static void register(SelfTriggeringMechanic mechanic, Location location) {
        selfTriggeringMechanics.put(location, mechanic);
    }

    public static void unregisterAll(Extent extent) {
        for (Location loc : selfTriggeringMechanics.keySet()) {
            if (loc.inExtent(extent)) selfTriggeringMechanics.remove(loc);
        }
    }

    public static void think() {
        for (Entry<Location, SelfTriggeringMechanic> entry : selfTriggeringMechanics.entrySet()) {
            entry.getValue().onThink(entry.getKey());
        }
    }

    @Subscribe
    public void onChunkLoad(final ChunkLoadEvent event) {

        // TODO change this if the world explodes.

        event.getGame().getScheduler().getTaskBuilder().execute(new Runnable() {
            @Override
            public void run() {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < event.getChunk().getWorld().getBlockMax().getY(); y++) {
                            Location block = event.getChunk().getLocation(x, y, z).add(event.getChunk().getBlockMin().toDouble());
                            for (Mechanic mechanic : CraftBookPlugin.<CraftBookPlugin>inst().enabledMechanics) {
                                if (mechanic instanceof SpongeBlockMechanic && mechanic instanceof SelfTriggeringMechanic) {
                                    if (((SpongeBlockMechanic) mechanic).isValid(block))
                                        register((SelfTriggeringMechanic) mechanic, block);
                                }
                            }
                        }
                    }
                }
            }
        }).submit(CraftBookPlugin.inst());
    }

    @Subscribe
    public void onChunkUnload(ChunkUnloadEvent event) {
        unregisterAll(event.getChunk());
    }

    @Subscribe
    public void onWorldUnload(WorldUnloadEvent event) {
        unregisterAll(event.getWorld());
    }
}
