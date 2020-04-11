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
package org.enginehub.craftbook.sponge.mechanics.minecart;

import com.me4502.modularframework.module.Module;
import org.enginehub.craftbook.util.documentation.DocumentationProvider;
import org.enginehub.craftbook.sponge.mechanics.types.SpongeMechanic;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.vehicle.minecart.RideableMinecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.RideEntityEvent;
import org.spongepowered.api.event.filter.cause.First;

@Module(id = "minecartmobblocker", name = "MinecartMobBlocker", onEnable="onInitialize", onDisable="onDisable")
public class MobBlocker extends SpongeMechanic implements DocumentationProvider {

    @Listener
    public void onVehicleEnter(RideEntityEvent.Mount event, @First Entity entity) {
        if (event.getTargetEntity() instanceof RideableMinecart) {
            if (!(entity instanceof Player)) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public String getPath() {
        return "mechanics/minecart/mob_blocker";
    }
}
