package com.sk89q.craftbook.common.st;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.events.SelfTriggerPingEvent;
import com.sk89q.craftbook.util.events.SelfTriggerThinkEvent;
import com.sk89q.craftbook.util.events.SelfTriggerUnregisterEvent;
import com.sk89q.craftbook.util.events.SelfTriggerUnregisterEvent.UnregisterReason;

public class SelfTriggeringManager {

    /**
     * List of mechanics that think on a routine basis.
     */
    public final Set<Location> thinkingMechanics = new HashSet<Location>();

    public void registerSelfTrigger(Chunk chunk) {
        try {
            for(BlockState state : chunk.getTileEntities()) {
                Block block = state.getBlock();
                if(thinkingMechanics.contains(block.getLocation())) continue;
                SelfTriggerPingEvent event = new SelfTriggerPingEvent(block);
                Bukkit.getServer().getPluginManager().callEvent(event);
            }
        } catch (NullPointerException e) {
            Bukkit.getLogger().warning("A corrupt tile entity was found in the chunk: (x:" + chunk.getX() + " z: " + chunk.getZ() + ") Self-Triggering mechanics may not work here until the issue is resolved.");
            if(CraftBookPlugin.inst().getConfiguration().debugMode)
                BukkitUtil.printStacktrace(e);
        }
    }

    public void registerSelfTrigger(Location location) {

        if(thinkingMechanics.contains(location)) return;
        hasChanged = true;
        thinkingMechanics.add(location);
    }

    public void unregisterSelfTrigger(Location location, UnregisterReason reason) {

        if(!thinkingMechanics.contains(location)) return;
        SelfTriggerUnregisterEvent event = new SelfTriggerUnregisterEvent(location.getBlock(), reason);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if(!event.isCancelled()) {
            hasChanged = true;
            thinkingMechanics.remove(location);
        }
    }

    public void unregisterSelfTrigger(Chunk chunk) {

        if(thinkingMechanics.size() == 0) return; //Skip the checks this round. Save a little CPU with the array creation.

        Location[] registeredLocations;

        synchronized (this) {
            // Copy to array to get rid of concurrency snafus
            registeredLocations = thinkingMechanics.toArray(new Location[thinkingMechanics.size()]);
        }

        for (Location location : registeredLocations) {
            if(location.getChunk().equals(chunk))
                unregisterSelfTrigger(location, UnregisterReason.UNLOAD);
        }
    }

    Location[] registeredLocations;
    private boolean hasChanged = false;

    /**
     * Causes all thinking mechanics to think.
     */
    public void think() {

        if(thinkingMechanics.size() == 0) return; //Skip the checks this round. Save a little CPU with the array creation.

        if(hasChanged || registeredLocations == null) {
            synchronized (this) {
                // Copy to array to get rid of concurrency snafus
                registeredLocations = thinkingMechanics.toArray(new Location[thinkingMechanics.size()]);
            }
        }

        for (Location location : registeredLocations) {
            if(!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
                unregisterSelfTrigger(location, UnregisterReason.UNLOAD);
                continue;
            }
            try {
                SelfTriggerThinkEvent event = new SelfTriggerThinkEvent(location.getBlock());
                Bukkit.getServer().getPluginManager().callEvent(event);
                if(!event.isHandled()) {
                    unregisterSelfTrigger(location, UnregisterReason.UNKNOWN);
                }
            } catch (Throwable t) { // Mechanic failed to think for some reason
                CraftBookPlugin.logger().log(Level.WARNING, "CraftBook mechanic: Failed to think for " + location.toString());
                BukkitUtil.printStacktrace(t);
                unregisterSelfTrigger(location, UnregisterReason.ERROR);
            }
        }
    }
}