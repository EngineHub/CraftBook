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

package org.enginehub.craftbook.mechanics.minecart.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.HandlerList;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import org.enginehub.craftbook.mechanics.minecart.blocks.CartMechanismBlocks;

public class CartBlockEnterEvent extends VehicleEnterEvent {

    private static final HandlerList handlers = new HandlerList();

    protected final CartMechanismBlocks blocks;

    public CartBlockEnterEvent (Vehicle vehicle, Entity entered, CartMechanismBlocks blocks) {
        super(vehicle, entered);

        this.blocks = blocks;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public CartMechanismBlocks getBlocks() {

        return blocks;
    }

    public Minecart getMinecart() {

        return (Minecart) getVehicle();
    }
}