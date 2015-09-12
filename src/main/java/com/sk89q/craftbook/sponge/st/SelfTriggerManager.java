package com.sk89q.craftbook.sponge.st;

import com.me4502.modularframework.exception.ModuleNotInstantiatedException;
import com.me4502.modularframework.module.ModuleWrapper;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SelfTriggerManager {

    public static boolean isInitialized = false;

    private static Map<Location, SelfTriggeringMechanic> selfTriggeringMechanics = new HashMap<>();

    public static void initialize() {
        CraftBookPlugin.game.getScheduler().createTaskBuilder().interval(2L).execute(new SelfTriggerClock()).submit(CraftBookPlugin.inst());
        CraftBookPlugin.game.getEventManager().registerListeners(CraftBookPlugin.inst(), new SelfTriggerManager());

        isInitialized = true;
    }

    public static void register(SelfTriggeringMechanic mechanic, Location location) {
        selfTriggeringMechanics.put(location, mechanic);
    }

    public static void unregisterAll(Extent extent) {
        selfTriggeringMechanics.keySet().stream().filter(loc -> loc.inExtent(extent)).forEach(selfTriggeringMechanics::remove);
    }

    public static void think() {
        for (Entry<Location, SelfTriggeringMechanic> entry : selfTriggeringMechanics.entrySet()) {
            entry.getValue().onThink(entry.getKey());
        }
    }

    @Listener
    public void onChunkLoad(LoadChunkEvent event) {

        // TODO change this if the world explodes.

        event.getGame().getScheduler().createTaskBuilder().execute(() -> {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < event.getTargetChunk().getWorld().getBlockMax().getY(); y++) {
                        Location block = event.getTargetChunk().getLocation(x, y, z).add(event.getTargetChunk().getBlockMin().toDouble());
                        for (ModuleWrapper module : CraftBookPlugin.<CraftBookPlugin>inst().moduleController.getModules()) {
                            if(!module.isEnabled()) continue;
                            try {
                                SpongeMechanic mechanic = (SpongeMechanic) module.getModule();
                                if (mechanic instanceof SpongeBlockMechanic && mechanic instanceof SelfTriggeringMechanic) {
                                    if (((SpongeBlockMechanic) mechanic).isValid(block))
                                        register((SelfTriggeringMechanic) mechanic, block);
                                }
                            } catch (ModuleNotInstantiatedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }).submit(CraftBookPlugin.inst());
    }

    @Listener
    public void onChunkUnload(UnloadChunkEvent event) {
        unregisterAll(event.getTargetChunk());
    }

    @Listener
    public void onWorldUnload(UnloadWorldEvent event) {
        unregisterAll(event.getTargetWorld());
    }
}
