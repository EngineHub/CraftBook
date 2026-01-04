/*
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

package org.enginehub.craftbook.bukkit.events;

import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.event.HandlerList;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.enginehub.craftbook.mechanics.minecart.blocks.CartMechanismBlocks;

/**
 * Called when a Minecart crosses a block boundary.
 */
public class CartBlockImpactEvent extends VehicleMoveEvent {
    private static final HandlerList handlers = new HandlerList();

    private final CartMechanismBlocks blocks;
    private final boolean minor;

    public CartBlockImpactEvent(Minecart minecart, Location from, Location to, CartMechanismBlocks blocks, boolean minor) {
        super(minecart, from, to);

        this.blocks = blocks;
        this.minor = minor;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public CartMechanismBlocks getBlocks() {
        // TODO Look into whether lazy loading this is worthwhile
        return this.blocks;
    }

    /**
     * Gets whether the movement is within a block or crosses a block boundary.
     *
     * @return If it's a minor movement within a block
     */
    public boolean isMinor() {
        return this.minor;
    }

    public Minecart getMinecart() {
        return (Minecart) getVehicle();
    }
}
