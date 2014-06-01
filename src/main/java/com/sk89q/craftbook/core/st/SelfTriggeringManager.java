package com.sk89q.craftbook.core.st;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

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
    private final Collection<Location> thinkingMechanics = new HashSet<Location>();

    public void registerSelfTrigger(Chunk chunk) {
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
                BukkitUtil.printStacktrace(e);
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
            synchronized (this) {
                // Copy to array to get rid of concurrency snafus
                registeredLocations = thinkingMechanics.toArray(new Location[thinkingMechanics.size()]);
            }
        }

        for (Location location : registeredLocations) {
            if(location.getChunk().equals(chunk))
                unregisterSelfTrigger(location, UnregisterReason.UNLOAD);
        }
    }

    public Collection<Location> getSelfTriggeringMechanics() {

        return new ArrayList<Location>(thinkingMechanics);
    }

    private Location[] registeredLocations;
    private boolean hasChanged = false;

    /**
     * Causes all thinking mechanics to think.
     */
    public void think() {

        if(thinkingMechanics.isEmpty()) return; //Skip the checks this round. Save a little CPU with the array creation.

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
                    unregisterSelfTrigger(location, UnregisterReason.NOT_HANDLED);
                }
            } catch (Throwable t) { // Mechanic failed to think for some reason
                CraftBookPlugin.logger().log(Level.WARNING, "CraftBook mechanic: Failed to think for " + location.toString());
                BukkitUtil.printStacktrace(t);
                unregisterSelfTrigger(location, UnregisterReason.ERROR);
            }
        }
    }
}