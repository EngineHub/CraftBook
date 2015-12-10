package com.sk89q.craftbook.sponge.st;

import com.me4502.modularframework.exception.ModuleNotInstantiatedException;
import com.me4502.modularframework.module.ModuleWrapper;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

public class SelfTriggerManager {

    public static boolean isInitialized = false;

    private static Map<Location, SelfTriggeringMechanic> selfTriggeringMechanics = new HashMap<>();

    public static void initialize() {
        CraftBookPlugin.game.getScheduler().createTaskBuilder().intervalTicks(2L).execute(new SelfTriggerClock()).submit(CraftBookPlugin.inst());
        CraftBookPlugin.game.getEventManager().registerListeners(CraftBookPlugin.inst(), new SelfTriggerManager());

        isInitialized = true;

        for(World world : Sponge.getGame().getServer().getWorlds()) {
            for(Chunk chunk : world.getLoadedChunks()) {
                registerAll(chunk);
            }
        }
    }

    public static void unload() {
        selfTriggeringMechanics.clear();
    }

    public static void register(SelfTriggeringMechanic mechanic, Location location) {
        selfTriggeringMechanics.put(location, mechanic);
    }

    public static void registerAll(Chunk chunk) {
        /*Sponge.getGame().getScheduler().createTaskBuilder().execute(() -> {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < chunk.getWorld().getBlockMax().getY(); y++) {
                        Location block = chunk.getLocation(x, y, z).add(chunk.getBlockMin().toDouble());
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
        }).submit(CraftBookPlugin.inst());*/
        for(TileEntity tileEntity : chunk.getTileEntities()) {
            for (ModuleWrapper module : CraftBookPlugin.<CraftBookPlugin>inst().moduleController.getModules()) {
                if(!module.isEnabled()) continue;
                try {
                    SpongeMechanic mechanic = (SpongeMechanic) module.getModule();
                    if (mechanic instanceof SpongeBlockMechanic && mechanic instanceof SelfTriggeringMechanic) {
                        if (((SpongeBlockMechanic) mechanic).isValid(tileEntity.getLocation()))
                            register((SelfTriggeringMechanic) mechanic, tileEntity.getLocation());
                    }
                } catch (ModuleNotInstantiatedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void unregisterAll(Extent extent) {
        new HashSet<>(selfTriggeringMechanics.keySet()).stream().filter(loc -> loc.inExtent(extent)).forEach(selfTriggeringMechanics::remove);
    }

    public static void think() {
        for (Entry<Location, SelfTriggeringMechanic> entry : selfTriggeringMechanics.entrySet()) {
            entry.getValue().onThink(entry.getKey());
        }
    }

    @Listener
    public void onChunkLoad(LoadChunkEvent event) {

        // TODO change this if the world explodes.
        registerAll(event.getTargetChunk());
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
