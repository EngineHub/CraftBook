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
package com.sk89q.craftbook.sponge.mechanics.boat;

import com.flowpowered.math.vector.Vector3d;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.filter.cause.First;

@Module(id = "boatremoveentities", name = "BoatRemoveEntities", onEnable="onInitialize", onDisable="onDisable")
public class RemoveEntities extends SpongeMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Boolean> damageOnly = new ConfigValue<>("damage-only", "Only damage entities, don't remove them.", false);
    private ConfigValue<Boolean> damageOtherBoats = new ConfigValue<>("damage-other-boats", "Allow boats to damage eachother.", false);

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        damageOnly.load(config);
        damageOtherBoats.load(config);
    }

    @Listener
    public void onEntityCollide(CollideEntityEvent event, @First Boat boat) {
        if (boat.getPassengers().isEmpty()) {
            return;
        }
        event.getEntities().forEach(entity -> {
            if (entity == boat || boat.getPassengers().contains(entity)) {
                return;
            }
            if (entity instanceof Boat && !damageOtherBoats.getValue()) {
                return;
            }
            if (damageOnly.getValue() && (!(entity instanceof Living) && !(entity instanceof Minecart) && !(entity instanceof Boat))) {
                return;
            }

            if (entity instanceof Living) {
                entity.damage(10, DamageSource.builder().type(DamageTypes.CONTACT).build());
                entity.setVelocity(boat.getVelocity().normalize().mul(1.6).add(new Vector3d(0, 0.3, 0)));
            } else {
                entity.remove();
            }
        });
    }

    @Override
    public String getName() {
        return "Boat" + super.getName();
    }

    @Override
    public String getPath() {
        return "mechanics/boat/removeentities";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue[] {
                damageOnly,
                damageOtherBoats
        };
    }
}
