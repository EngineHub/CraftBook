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
package org.enginehub.craftbook.sponge.st;

import com.google.common.collect.ImmutableMap;
import com.me4502.modularframework.exception.ModuleNotInstantiatedException;
import com.me4502.modularframework.module.ModuleWrapper;
import com.me4502.modularframework.module.SpongeModuleWrapper;
import org.enginehub.craftbook.core.st.SelfTriggerClock;
import org.enginehub.craftbook.core.st.SelfTriggerManager;
import org.enginehub.craftbook.sponge.CraftBookPlugin;
import org.enginehub.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import org.enginehub.craftbook.sponge.mechanics.types.SpongeMechanic;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class SpongeSelfTriggerManager implements SelfTriggerManager {

    private Map<Location<World>, SelfTriggeringMechanic> selfTriggeringMechanics = new ConcurrentHashMap<>();

    @Override
    public void initialize() {
        Sponge.getGame().getScheduler().createTaskBuilder().intervalTicks(2L).execute(new SelfTriggerClock()).submit(CraftBookPlugin.inst());
        Sponge.getGame().getEventManager().registerListeners(CraftBookPlugin.inst(), this);

        for(World world : Sponge.getGame().getServer().getWorlds()) {
            registerAll(world);
        }
    }

    @Override
    public void unload() {
        selfTriggeringMechanics.clear();
    }

    public Map<Location<World>,SelfTriggeringMechanic> getSelfTriggeringMechanics() {
        return ImmutableMap.copyOf(selfTriggeringMechanics);
    }

    public void register(SelfTriggeringMechanic mechanic, Location<World> location) {
        selfTriggeringMechanics.put(location, mechanic);
    }

    public void unregister(SelfTriggeringMechanic mechanic, Location<World> location) {
        selfTriggeringMechanics.remove(location, mechanic);
    }

    private void registerAll(World world) {
        for (Chunk chunk : world.getLoadedChunks()) {
            registerAll(chunk);
        }
    }

    private void registerAll(Chunk chunk) {
        for(TileEntity tileEntity : chunk.getTileEntities()) {
            for (ModuleWrapper module : CraftBookPlugin.spongeInst().moduleController.getModules()) {
                if(!module.isEnabled()) continue;
                try {
                    SpongeMechanic mechanic = (SpongeMechanic) ((SpongeModuleWrapper) module).getModuleUnchecked();
                    if (mechanic instanceof SpongeBlockMechanic && mechanic instanceof SelfTriggeringMechanic) {
                        if (((SpongeBlockMechanic) mechanic).isValid(tileEntity.getLocation())) {
                            register((SelfTriggeringMechanic) mechanic, tileEntity.getLocation());
                        }
                    }
                } catch (ModuleNotInstantiatedException e) {
                    CraftBookPlugin.spongeInst().getLogger().error("Failed to register self-triggering mechanic for module: " + module.getName(), e);
                }
            }
        }
    }

    private void unregisterAll(Extent extent) {
        new HashSet<>(selfTriggeringMechanics.keySet()).stream().filter(loc -> loc.inExtent(extent)).forEach(selfTriggeringMechanics::remove);
    }

    @Override
    public void think() {
        for (Entry<Location<World>, SelfTriggeringMechanic> entry : selfTriggeringMechanics.entrySet()) {
            entry.getValue().onThink(entry.getKey());
        }
    }

    @Listener
    public void onChunkLoad(LoadChunkEvent event) {
        registerAll(event.getTargetChunk());
    }

    @Listener
    public void onChunkUnload(UnloadChunkEvent event) {
        unregisterAll(event.getTargetChunk());
    }

    @Listener
    public void onWorldLoad(LoadWorldEvent event) {
        registerAll(event.getTargetWorld());
    }

    @Listener
    public void onWorldUnload(UnloadWorldEvent event) {
        unregisterAll(event.getTargetWorld());
    }
}
