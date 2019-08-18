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

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;

@Module(id = "boatspeedmodifiers", name = "BoatSpeedModifiers", onEnable="onInitialize", onDisable="onDisable")
public class SpeedModifiers extends SpongeMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Double> maxSpeed = new ConfigValue<>("max-speed", "Sets the maximum speed of boats. -1 to disable.", -1d, TypeToken.of(Double.class));
    private ConfigValue<Double> occupiedDeceleration = new ConfigValue<>("occupied-deceleration",
            "Sets the occupied deceleration of boats. -1 to disable.", -1d, TypeToken.of(Double.class));
    private ConfigValue<Double> unoccupiedDeceleration = new ConfigValue<>("unoccupied-deceleration",
            "Sets the unoccupied deceleration of boats. -1 to disable.", -1d, TypeToken.of(Double.class));

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        maxSpeed.load(config);
        occupiedDeceleration.load(config);
        unoccupiedDeceleration.load(config);
    }

    @Listener
    public void onEntityConstruct(SpawnEntityEvent event) {
        event.getEntities().stream().filter(entity -> entity instanceof Boat).forEach(boat -> {
            if (maxSpeed.getValue() >= 0) {
                ((Boat) boat).setMaxSpeed(maxSpeed.getValue());
            }
            if (occupiedDeceleration.getValue() >= 0) {
                ((Boat) boat).setOccupiedDeceleration(occupiedDeceleration.getValue());
            }
            if (unoccupiedDeceleration.getValue() >= 0) {
                ((Boat) boat).setUnoccupiedDeceleration(unoccupiedDeceleration.getValue());
            }
        });
    }

    @Override
    public String getName() {
        return "Boat" + super.getName();
    }

    @Override
    public String getPath() {
        return "mechanics/boat/speedmodifiers";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue[] {
                maxSpeed,
                occupiedDeceleration,
                unoccupiedDeceleration
        };
    }
}
