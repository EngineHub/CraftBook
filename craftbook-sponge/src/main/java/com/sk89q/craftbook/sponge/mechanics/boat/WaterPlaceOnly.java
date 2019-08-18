/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics.boat;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@Module(id = "boatwaterplaceonly", name = "WaterPlaceOnly", onEnable="onInitialize", onDisable="onDisable")
public class WaterPlaceOnly extends SpongeMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    @Listener
    public void onEntityConstruct(SpawnEntityEvent event) {
        event.getEntities().stream().filter(entity -> entity instanceof Boat).forEach(boat -> {
            if (!isWater(boat.getLocation())) {
                event.setCancelled(true);
            }
        });
    }

    private static boolean isWater(Location<World> location) {
        return location.getBlockType() == BlockTypes.WATER || location.getBlockType() == BlockTypes.FLOWING_WATER;
    }

    @Override
    public String getName() {
        return "Boat" + super.getName();
    }

    @Override
    public String getPath() {
        return "mechanics/boat/waterplaceonly";
    }
}
