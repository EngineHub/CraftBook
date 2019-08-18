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

import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.vehicle.minecart.ChestMinecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Collection;

@Module(id = "minecartitempickup", name = "MinecartItemPickup", onEnable="onInitialize", onDisable="onDisable")
public class ItemPickup extends SpongeMechanic implements DocumentationProvider {

    @Listener
    public void onVehicleCollide(CollideEntityEvent event, @First ChestMinecart chestMinecart) {
        event.getEntities().stream().filter(entity -> entity.getType() == EntityTypes.ITEM).filter(item -> !item.isRemoved()).forEach(item -> {
            Collection<ItemStackSnapshot> rejects = chestMinecart.getInventory().offer(item.get(Keys.REPRESENTED_ITEM).get().createStack()).getRejectedItems();
            if (rejects.isEmpty()) {
                item.remove();
            }
        });
    }

    @Override
    public String getPath() {
        return "mechanics/minecart/item_pickup";
    }
}
