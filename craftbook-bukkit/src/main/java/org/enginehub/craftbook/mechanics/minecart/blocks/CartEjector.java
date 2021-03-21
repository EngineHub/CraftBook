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

package org.enginehub.craftbook.mechanics.minecart.blocks;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.RedstoneUtil.Power;
import org.enginehub.craftbook.util.SignUtil;

import java.util.List;

public class CartEjector extends CartBlockMechanism {

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {

        // care?
        if (!event.getBlocks().matches(getBlock())) return;
        if (event.getMinecart().isEmpty()) return;

        // enabled?
        if (Power.OFF == isActive(event.getBlocks())) return;

        // go
        Block ejectTarget;
        if (!event.getBlocks().hasSign()) {
            ejectTarget = event.getBlocks().rail;
        } else if (!event.getBlocks().matches("eject")) {
            ejectTarget = event.getBlocks().rail;
        } else {
            ejectTarget = event.getBlocks().rail.getRelative(SignUtil.getFront(event.getBlocks().sign));
        }
        // if you use just
        // cart.getPassenger().teleport(ejectTarget.getLocation());
        // the client tweaks as bukkit tries to teleport you, then changes its mind and leaves you in the cart.
        // the cart also comes to a dead halt at the time of writing, and i have no idea why.
        List<Entity> passengers = event.getMinecart().getPassengers();
        event.getMinecart().eject();
        passengers.forEach(ent -> ent.teleport(LocationUtil.center(ejectTarget.getLocation())));

        // notice!
        // if a client tries to board a cart immediately before it crosses an ejector,
        // it may appear to them that they crossed the ejector and it failed to activate.
        // what's actually happening is that the server didn't see them enter the cart
        // until -after- it had triggered the ejector... it's just client anticipating.
    }

    @Override
    public String getName() {

        return "Ejector";
    }

    @Override
    public String[] getApplicableSigns() {

        return new String[] { "Eject" };
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("block", "Sets the block that is the base of the ejector mechanic.");
        material = BlockParser.getBlock(config.getString("block", BlockTypes.IRON_BLOCK.getId()), true);
    }
}