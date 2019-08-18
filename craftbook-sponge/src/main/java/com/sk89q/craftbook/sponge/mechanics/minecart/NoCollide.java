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
package com.sk89q.craftbook.sponge.mechanics.minecart;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.entity.vehicle.minecart.RideableMinecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.filter.cause.First;

@Module(id = "minecartnocollide", name = "MinecartNoCollide", onEnable="onInitialize", onDisable="onDisable")
public class NoCollide extends SpongeMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Boolean> emptyCarts = new ConfigValue<>("empty-carts", "Removes collision with empty minecarts.", true);
    private ConfigValue<Boolean> fullCarts = new ConfigValue<>("full-carts", "Removes collision with occupied (Or chest/powered/tnt) minecarts.", false);

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        emptyCarts.load(config);
        fullCarts.load(config);
    }

    @Listener
    public void onCollision(CollideEntityEvent event, @First Minecart minecart) {
        if (event.getEntities().stream().filter(entity -> entity instanceof Minecart).anyMatch(entity -> (
                        (emptyCarts.getValue()
                            && entity instanceof RideableMinecart
                            && entity.getPassengers().isEmpty()))
                        || (fullCarts.getValue()
                            && (!(entity instanceof RideableMinecart) || !entity.getPassengers().isEmpty())))
                ) {
            event.setCancelled(true);
        }
    }

    @Override
    public String getPath() {
        return "mechanics/minecart/no_collide";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue[] {
                emptyCarts,
                fullCarts
        };
    }
}
