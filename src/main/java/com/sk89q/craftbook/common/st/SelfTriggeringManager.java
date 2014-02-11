package com.sk89q.craftbook.common.st;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.events.SelfTriggerPingEvent;
import com.sk89q.craftbook.util.events.SelfTriggerThinkEvent;
import com.sk89q.craftbook.util.events.SelfTriggerUnregisterEvent;
import com.sk89q.craftbook.util.events.SelfTriggerUnregisterEvent.UnregisterReason;
import com.sk89q.worldedit.BlockWorldVector;

public class SelfTriggeringManager {

    /**
     * List of mechanics that think on a routine basis.
     */
    private final Collection<BlockWorldVector> thinkingMechanics = new Vector<BlockWorldVector>(32,5);

    public void registerSelfTrigger(Chunk chunk) {
        try {
            for(BlockState state : chunk.getTileEntities()) {
                if(!(state instanceof Sign)) continue;
                Block block = state.getBlock();
                if(thinkingMechanics.contains(BukkitUtil.toWorldVector(block.getLocation()))) continue;
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

        BlockWorldVector vec = BukkitUtil.toWorldVector(location);
        if(thinkingMechanics.contains(vec)) return;
        hasChanged = true;
        thinkingMechanics.add(vec);
    }

    public void unregisterSelfTrigger(Location location, UnregisterReason reason) {

        if(thinkingMechanics.isEmpty()) return; //Skip the checks this round. Save a little CPU with the array creation.

        BlockWorldVector vec = BukkitUtil.toWorldVector(location);
        if(!thinkingMechanics.contains(vec)) return;
        SelfTriggerUnregisterEvent event = new SelfTriggerUnregisterEvent(location.getBlock(), reason);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if(!event.isCancelled()) {
            hasChanged = true;
            thinkingMechanics.remove(vec);
        }
    }

    public void unregisterSelfTrigger(Chunk chunk) {

        if(thinkingMechanics.isEmpty()) return; //Skip the checks this round. Save a little CPU with the array creation.

        if(hasChanged || registeredLocations == null) {
            synchronized (this) {
                // Copy to array to get rid of concurrency snafus
                registeredLocations = thinkingMechanics.toArray(new BlockWorldVector[thinkingMechanics.size()]);
            }
        }

        for (BlockWorldVector location : registeredLocations) {
            Location loc = BukkitUtil.toLocation(location);
            if(loc.getChunk().equals(chunk))
                unregisterSelfTrigger(loc, UnregisterReason.UNLOAD);
        }
    }

    public Collection<BlockWorldVector> getSelfTriggeringMechanics() {

        return new ArrayList<BlockWorldVector>(thinkingMechanics);
    }

    private BlockWorldVector[] registeredLocations;
    private boolean hasChanged = false;

    /**
     * Causes all thinking mechanics to think.
     */
    public void think() {

        if(thinkingMechanics.isEmpty()) return; //Skip the checks this round. Save a little CPU with the array creation.

        if(hasChanged || registeredLocations == null) {
            synchronized (this) {
                // Copy to array to get rid of concurrency snafus
                registeredLocations = thinkingMechanics.toArray(new BlockWorldVector[thinkingMechanics.size()]);
            }
        }

        for (BlockWorldVector location : registeredLocations) {
            Location loc = BukkitUtil.toLocation(location);
            if(!loc.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
                unregisterSelfTrigger(loc, UnregisterReason.UNLOAD);
                continue;
            }
            try {
                SelfTriggerThinkEvent event = new SelfTriggerThinkEvent(loc.getBlock());
                Bukkit.getServer().getPluginManager().callEvent(event);
                if(!event.isHandled()) {
                    unregisterSelfTrigger(loc, UnregisterReason.UNKNOWN);
                }
            } catch (Throwable t) { // Mechanic failed to think for some reason
                CraftBookPlugin.logger().log(Level.WARNING, "CraftBook mechanic: Failed to think for " + location.toString());
                BukkitUtil.printStacktrace(t);
                unregisterSelfTrigger(loc, UnregisterReason.ERROR);
            }
        }
    }
}