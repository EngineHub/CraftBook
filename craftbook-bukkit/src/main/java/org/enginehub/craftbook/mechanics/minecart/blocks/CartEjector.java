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

package org.enginehub.craftbook.mechanics.minecart.blocks;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.enginehub.craftbook.bukkit.events.CartBlockImpactEvent;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.RedstoneUtil.Power;
import org.enginehub.craftbook.util.SignUtil;

import java.util.List;

public class CartEjector extends CartBlockMechanism {

    private static final List<String> SIGNS = List.of("Eject");

    public CartEjector(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {
        if (event.getMinecart().isEmpty() || !event.getBlocks().matches(getBlock()) || Power.OFF == isActive(event.getBlocks())) {
            return;
        }

        List<Entity> passengers = event.getMinecart().getPassengers();
        event.getMinecart().eject();

        if (event.getBlocks().matches("eject") != null) {
            Block ejectTarget = event.getBlocks().rail().getRelative(SignUtil.getFront(event.getBlocks().sign()));
            for (Entity ent : passengers) {
                ent.teleport(ejectTarget.getLocation().toCenterLocation());
            }
        }

        // notice!
        // if a client tries to board a cart immediately before it crosses an ejector,
        // it may appear to them that they crossed the ejector and it failed to activate.
        // what's actually happening is that the server didn't see them enter the cart
        // until -after- it had triggered the ejector... it's just client anticipating.
    }

    @Override
    public List<String> getApplicableSigns() {
        return SIGNS;
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("block", "The block the Cart Ejector mechanic uses.");
        setBlock(BlockParser.getBlock(config.getString("block", BlockTypes.IRON_BLOCK.id()), true));
    }
}
