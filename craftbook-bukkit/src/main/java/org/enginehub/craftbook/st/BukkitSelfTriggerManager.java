/*
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
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitTask;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.SignUtil;
import org.enginehub.craftbook.util.events.SelfTriggerPingEvent;
import org.enginehub.craftbook.util.events.SelfTriggerThinkEvent;
import org.enginehub.craftbook.util.events.SelfTriggerUnregisterEvent;
import org.enginehub.craftbook.util.events.SelfTriggerUnregisterEvent.UnregisterReason;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BukkitSelfTriggerManager implements SelfTriggerManager, Listener {

    private final MechanicClock mechanicClock = new MechanicClock();
    private final Set<Location> thinkingMechanics = new HashSet<>();
    private final List<Location> removingLocations = new ArrayList<>();

    private @Nullable BukkitTask clockTask;

    @Override
    public void setup() {
        CraftBook.LOGGER.info("Enumerating chunks for self-triggered components...");

        long start = System.currentTimeMillis();
        int numChunks = 0;

        for (World world : Bukkit.getWorlds()) {
            Chunk[] chunks = world.getLoadedChunks();
            for (Chunk chunk : chunks) {
                registerSelfTrigger(chunk);
            }
            numChunks += chunks.length;
        }

        CraftBook.LOGGER.info(numChunks + " chunk(s) for " + Bukkit.getWorlds().size() + " world(s) processed " + "(" + (System.currentTimeMillis() - start) + "ms elapsed)");

        // Set up the clock for self-triggered ICs.
        clockTask = Bukkit.getScheduler().runTaskTimer(CraftBookPlugin.inst(), mechanicClock, 0, CraftBook.getInstance().getPlatform().getConfiguration().stThinkRate);
        Bukkit.getPluginManager().registerEvents(this, CraftBookPlugin.inst());
    }

    @Override
    public void shutdown() {
        HandlerList.unregisterAll(this);
        if (clockTask != null) {
            clockTask.cancel();
            clockTask = null;
        }

        // First, remove them.
        for (Location location : removingLocations) {
            thinkingMechanics.remove(location);
        }

        for (Location location : thinkingMechanics) {
            unregisterSelfTrigger(location, UnregisterReason.UNLOAD);
        }

        thinkingMechanics.clear();
        removingLocations.clear();
    }

    @Override
    public void think() {
        // First, remove them.
        for (Location location : removingLocations) {
            thinkingMechanics.remove(location);
        }
        removingLocations.clear();

        for (Location location : thinkingMechanics) {
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
                CraftBook.LOGGER.warn("CraftBook mechanic: Failed to think for " + location, t);
                unregisterSelfTrigger(location, UnregisterReason.ERROR);
            }
        }
    }

    private void registerSelfTrigger(Chunk chunk) {
        try {
            for (BlockState state : chunk.getTileEntities(SignUtil::isSign, false)) {
                if (!(state instanceof Sign)) {
                    continue;
                }
                if (thinkingMechanics.contains(state.getLocation())) {
                    continue;
                }

                SelfTriggerPingEvent event = new SelfTriggerPingEvent(state.getBlock());
                Bukkit.getServer().getPluginManager().callEvent(event);

                if (event.isHandled()) {
                    registerSelfTrigger(state.getLocation());
                }
            }
        } catch (Throwable e) {
            CraftBook.LOGGER.warn("A corrupt tile entity was found in the chunk: (world: " + chunk.getWorld().getName() + " x: " + chunk.getX() + " z: " + chunk.getZ() + ") Self-Triggering mechanics may not work here until the issue is resolved.", e);
        }
    }

    public void registerSelfTrigger(Location location) {
        if (thinkingMechanics.contains(location)) {
            return;
        }
        thinkingMechanics.add(location);
    }

    public void unregisterSelfTrigger(Location location, UnregisterReason reason) {
        if (!thinkingMechanics.contains(location)) {
            // Can't unregister if it's not in here.
            return;
        }

        SelfTriggerUnregisterEvent event = new SelfTriggerUnregisterEvent(location.getBlock(), reason);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            removingLocations.add(location);
            CraftBookPlugin.logDebugMessage("Unregistered ST at: " + location + " for reason: " + reason.name(), "st.unregister");
        }
    }

    /**
     * Gets a list of all self-triggering locations.
     *
     * @return The self triggering locations
     */
    public List<Location> getSelfTriggeringMechanics() {
        return List.copyOf(thinkingMechanics);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkLoad(final ChunkLoadEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        registerSelfTrigger(event.getChunk());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        long chunkKey = event.getChunk().getChunkKey();

        for (Location location : thinkingMechanics) {
            if (Chunk.getChunkKey(location) == chunkKey) {
                unregisterSelfTrigger(location, UnregisterReason.UNLOAD);
            }
        }
    }
}
