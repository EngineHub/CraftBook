package com.sk89q.craftbook.core.st;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.events.SelfTriggerPingEvent;
import com.sk89q.craftbook.util.events.SelfTriggerThinkEvent;
import com.sk89q.craftbook.util.events.SelfTriggerUnregisterEvent;
import com.sk89q.craftbook.util.events.SelfTriggerUnregisterEvent.UnregisterReason;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;

public class SelfTriggeringManager implements Listener {

    /**
     * List of mechanics that think on a routine basis.
     */
    private final Collection<Location> thinkingMechanics = new HashSet<>();

    public void registerSelfTrigger(Chunk chunk) {
        if (!chunk.getWorld().isChunkLoaded(chunk))
            return;
        try {
            for(BlockState state : chunk.getTileEntities()) {
                if(!(state instanceof Sign)) continue;
                if(thinkingMechanics.contains(state.getLocation())) continue;
                SelfTriggerPingEvent event = new SelfTriggerPingEvent(state.getBlock());
                Bukkit.getServer().getPluginManager().callEvent(event);
            }
        } catch (Throwable e) {
            Bukkit.getLogger().warning("A corrupt tile entity was found in the chunk: (world: " + chunk.getWorld().getName() + " x: " + chunk.getX() + " z: " + chunk.getZ() + ") Self-Triggering mechanics may not work here until the issue is resolved.");
            if(CraftBookPlugin.inst().getConfiguration().debugMode)
                CraftBookBukkitUtil.printStacktrace(e);
        }
    }

    public void registerSelfTrigger(Location location) {

        if(thinkingMechanics.contains(location)) return;
        hasChanged = true;
        thinkingMechanics.add(location);
    }

    public void unregisterSelfTrigger(Location location, UnregisterReason reason) {

        if(thinkingMechanics.isEmpty()) return; //Skip the checks this round. Save a little CPU with the array creation.

        if(!thinkingMechanics.contains(location)) return;
        SelfTriggerUnregisterEvent event = new SelfTriggerUnregisterEvent(location.getBlock(), reason);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if(!event.isCancelled()) {
            hasChanged = true;
            thinkingMechanics.remove(location);
            CraftBookPlugin.logDebugMessage("Unregistered ST at: " + location.toString() + " for reason: " + reason.name(), "st.unregister");
        }
    }

    public void unregisterSelfTrigger(Chunk chunk) {

        if(thinkingMechanics.isEmpty()) return; //Skip the checks this round. Save a little CPU with the array creation.

        if(hasChanged || registeredLocations == null) {
            registeredLocations = thinkingMechanics.toArray(new Location[thinkingMechanics.size()]);
        }

        for (Location location : registeredLocations) {
            if(location.getChunk().equals(chunk))
                unregisterSelfTrigger(location, UnregisterReason.UNLOAD);
        }
    }

    public Collection<Location> getSelfTriggeringMechanics() {

        return new ArrayList<>(thinkingMechanics);
    }

    private Location[] registeredLocations;
    private boolean hasChanged = false;

    private boolean areAdjacentChunksLoaded(Location loc) {
        World world = loc.getWorld();

        final int CX = loc.getBlockX() >> 4;
        final int CZ = loc.getBlockZ() >> 4;

        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                // Check only cardinal directions
                if (x != 0 && z != 0) {
                    continue;
                }

                // Don't test the current chunk
                if (x == 0 && z == 0) {
                    continue;
                }

                if (!world.isChunkLoaded(CX + x, CZ + z)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Causes all thinking mechanics to think.
     */
    public void think() {

        if(thinkingMechanics.isEmpty()) return; //Skip the checks this round. Save a little CPU with the array creation.

        if(hasChanged || registeredLocations == null) {
            registeredLocations = thinkingMechanics.toArray(new Location[thinkingMechanics.size()]);
        }

        for (Location location : registeredLocations) {
            if(!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
                unregisterSelfTrigger(location, UnregisterReason.UNLOAD);
                continue;
            }

            // If some of the adjacent chunks aren't loaded, don't self trigger the IC yet; effectively "pause" it.
            // This prevents some occasionally serious chunk thrashing.
            if (!areAdjacentChunksLoaded(location)) {
                continue;
            }

            try {
                SelfTriggerThinkEvent event = new SelfTriggerThinkEvent(location.getBlock());
                Bukkit.getServer().getPluginManager().callEvent(event);
                if(!event.isHandled()) {
                    unregisterSelfTrigger(location, UnregisterReason.NOT_HANDLED);
                }
            } catch (Throwable t) { // Mechanic failed to think for some reason
                CraftBookPlugin.logger().log(Level.WARNING, "CraftBook mechanic: Failed to think for " + location.toString());
                CraftBookBukkitUtil.printStacktrace(t);
                unregisterSelfTrigger(location, UnregisterReason.ERROR);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkLoad(final ChunkLoadEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        CraftBookPlugin.server().getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> registerSelfTrigger(event.getChunk()), 2);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkUnload(ChunkUnloadEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        unregisterSelfTrigger(event.getChunk());
    }
}