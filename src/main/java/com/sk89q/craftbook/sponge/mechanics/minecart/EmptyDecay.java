/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
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
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.entity.PassengerData;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.entity.vehicle.minecart.RideableMinecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DismountEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;

@Module(moduleName = "MinecartEmptyDecay", onEnable="onInitialize", onDisable="onDisable")
public class EmptyDecay extends SpongeMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Long> emptyTicks = new ConfigValue<>("empty-ticks", "The amount of time that the cart must be empty before it decays, in ticks.", 40L);
    private ConfigValue<Boolean> onlyOnExit = new ConfigValue<>("only-on-exit", "Only start the decay timer on exit, preventing carts being incorrectly removed.", true);

    @Override
    public void onInitialize() throws CraftBookException {
        emptyTicks.load(config);
        onlyOnExit.load(config);
    }

    @Listener
    public void onVehicleExit(DismountEntityEvent event) {
        if (event.getTargetEntity() instanceof RideableMinecart) {
            Sponge.getGame().getScheduler().createTaskBuilder().delayTicks(emptyTicks.getValue()).execute(new MinecartDecay((Minecart) event.getTargetEntity())).submit(CraftBookPlugin.inst());
        }
    }

    @Listener
    public void onEntityCreate(SpawnEntityEvent event) {
        if(onlyOnExit.getValue())
            return;
        event.getEntities().stream().filter(entity -> entity instanceof RideableMinecart).forEach(entity -> Sponge.getGame().getScheduler().createTaskBuilder().delayTicks(emptyTicks.getValue()).execute(new MinecartDecay((Minecart) entity)).submit(CraftBookPlugin.inst()));
    }

    private static class MinecartDecay implements Runnable {

        Minecart cart;

        MinecartDecay(Minecart cart) {
            this.cart = cart;
        }

        @Override
        public void run() {
            if (!cart.get(PassengerData.class).isPresent()) {
                cart.remove();
            }
        }
    }

    @Override
    public String getName() {
        return "Minecart" + super.getName();
    }

    @Override
    public String getPath() {
        return "mechanics/minecart/emptydecay";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[] {
                emptyTicks,
                onlyOnExit
        };
    }
}
