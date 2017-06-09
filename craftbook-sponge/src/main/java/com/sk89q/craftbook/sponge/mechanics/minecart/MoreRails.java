/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
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
package com.sk89q.craftbook.sponge.mechanics.minecart;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@Module(id = "minecartmorerails", name = "MinecartMoreRails", onEnable="onInitialize", onDisable="onDisable")
public class MoreRails extends SpongeMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Boolean> verticalRails = new ConfigValue<>("vertical-rails", "Allow ladders and vines to be used for vertical rails.", true);
    private ConfigValue<Boolean> intersectionRails = new ConfigValue<>("intersection-rails", "Allow pressure plates to be used as 4 way intersections.",
            true);
    private ConfigValue<Double> verticalVelocity = new ConfigValue<>("vertical-velocity",
            "Velocity that vertical rails adds to the cart.", 0.15d, TypeToken.of(Double.class));

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        verticalRails.load(config);
        verticalVelocity.load(config);
        intersectionRails.load(config);
    }

    @Listener
    public void onEntityMove(MoveEntityEvent event, @Getter("getTargetEntity") Minecart minecart) {
        Location<World> toBlock = event.getToTransform().getLocation();

        if (intersectionRails.getValue()) {
            if (toBlock.getBlockType() == BlockTypes.STONE_PRESSURE_PLATE
                    || toBlock.getBlockType() == BlockTypes.WOODEN_PRESSURE_PLATE
                    || toBlock.getBlockType() == BlockTypes.LIGHT_WEIGHTED_PRESSURE_PLATE
                    || toBlock.getBlockType() == BlockTypes.HEAVY_WEIGHTED_PRESSURE_PLATE) {
                minecart.setVelocity(minecart.getVelocity().normalize().mul(4d));
            }
        }
        if (verticalRails.getValue()) {
            if (toBlock.getBlockType() == BlockTypes.VINE || toBlock.getBlockType() == BlockTypes.LADDER) {
                Vector3d velocity = new Vector3d(0, verticalVelocity.getValue(), 0);
                if (toBlock.getBlockType() == BlockTypes.VINE) {
                    Vector3d directionVelocity = new Vector3d();
                    for (Direction direction : toBlock.get(Keys.CONNECTED_DIRECTIONS).get()) {
                        directionVelocity = directionVelocity.add(direction.asOffset());
                    }
                    if (directionVelocity.length() == 0)
                        return;
                    velocity = velocity.add(directionVelocity.normalize());
                } else {
                    velocity = velocity.add(toBlock.get(Keys.DIRECTION).get().asOffset().mul(-1));
                }

                minecart.setVelocity(minecart.getVelocity().add(velocity));
            }
        }
    }

    @Override
    public String getPath() {
        return "mechanics/minecart/more_rails";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue[] {
                verticalRails,
                verticalVelocity,
                intersectionRails
        };
    }
}
