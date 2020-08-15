/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.st;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.events.SelfTriggerPingEvent;
import org.enginehub.craftbook.util.events.SelfTriggerThinkEvent;
import org.enginehub.craftbook.util.events.SelfTriggerUnregisterEvent;
import org.enginehub.craftbook.util.events.SelfTriggerUnregisterEvent.UnregisterReason;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class SelfTriggeringManager implements Listener {

    /**
     * List of mechanics that think on a routine basis.
     */
    private final Collection<Location> thinkingMechanics = new HashSet<>();

    public void registerSelfTrigger(Chunk chunk) {
        if (!chunk.getWorld().isChunkLoaded(chunk))
            return;
        try {
            for (BlockState state : chunk.getTileEntities()) {
                if (!(state instanceof Sign)) continue;
                if (thinkingMechanics.contains(state.getLocation())) continue;
                SelfTriggerPingEvent event = new SelfTriggerPingEvent(state.getBlock());
                Bukkit.getServer().getPluginManager().callEvent(event);
            }
        } catch (Throwable e) {
            Bukkit.getLogger().warning("A corrupt tile entity was found in the chunk: (world: " + chunk.getWorld().getName() + " x: " + chunk.getX() + " z: " + chunk.getZ() + ") Self-Triggering mechanics may not work here until the issue is resolved.");
            if (CraftBook.getInstance().getPlatform().getConfiguration().debugMode)
                e.printStackTrace();
        }
    }

    public void registerSelfTrigger(Location location) {

        if (thinkingMechanics.contains(location)) return;
        hasChanged = true;
        thinkingMechanics.add(location);
    }

    public void unregisterSelfTrigger(Location location, UnregisterReason reason) {

        if (thinkingMechanics.isEmpty())
            return; //Skip the checks this round. Save a little CPU with the array creation.

        if (!thinkingMechanics.contains(location)) return;
        SelfTriggerUnregisterEvent event = new SelfTriggerUnregisterEvent(location.getBlock(), reason);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            hasChanged = true;
            thinkingMechanics.remove(location);
            CraftBookPlugin.logDebugMessage("Unregistered ST at: " + location.toString() + " for reason: " + reason.name(), "st.unregister");
        }
    }

    public void unregisterSelfTrigger(Chunk chunk) {

        if (thinkingMechanics.isEmpty())
            return; //Skip the checks this round. Save a little CPU with the array creation.

        if (hasChanged || registeredLocations == null) {
            registeredLocations = thinkingMechanics.toArray(new Location[thinkingMechanics.size()]);
        }

        for (Location location : registeredLocations) {
            if (location.getChunk().equals(chunk))
                unregisterSelfTrigger(location, UnregisterReason.UNLOAD);
        }
    }

    public Collection<Location> getSelfTriggeringMechanics() {

        return new ArrayList<>(thinkingMechanics);
    }

    private Location[] registeredLocations;
    private boolean hasChanged = false;

    /**
     * Causes all thinking mechanics to think.
     */
    public void think() {

        if (thinkingMechanics.isEmpty())
            return; //Skip the checks this round. Save a little CPU with the array creation.

        if (hasChanged || registeredLocations == null) {
            registeredLocations = thinkingMechanics.toArray(new Location[thinkingMechanics.size()]);
        }

        for (Location location : registeredLocations) {
            if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
                unregisterSelfTrigger(location, UnregisterReason.UNLOAD);
                continue;
            }
            try {
                SelfTriggerThinkEvent event = new SelfTriggerThinkEvent(location.getBlock());
                Bukkit.getServer().getPluginManager().callEvent(event);
                if (!event.isHandled()) {
                    unregisterSelfTrigger(location, UnregisterReason.NOT_HANDLED);
                }
            } catch (Throwable t) { // Mechanic failed to think for some reason
                CraftBook.logger.warn("CraftBook mechanic: Failed to think for " + location.toString());
                t.printStackTrace();
                unregisterSelfTrigger(location, UnregisterReason.ERROR);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkLoad(final ChunkLoadEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        Bukkit.getServer().getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> registerSelfTrigger(event.getChunk()), 2);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkUnload(ChunkUnloadEvent event) {

        if (!EventUtil.passesFilter(event))
            return;

        unregisterSelfTrigger(event.getChunk());
    }
}