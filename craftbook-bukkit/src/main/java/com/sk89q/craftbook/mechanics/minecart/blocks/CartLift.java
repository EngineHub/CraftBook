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

package com.sk89q.craftbook.mechanics.minecart.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.CartUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;

public class CartLift extends CartBlockMechanism {

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {

        // validate
        if (!event.getBlocks().matches(getMaterial())) return;
        if (!event.getBlocks().hasSign()) return;
        if (event.isMinor()) return;
        if (!(event.getBlocks().matches("cartlift up") || event.getBlocks().matches("cartlift down"))) return;

        Minecart cart = (Minecart) event.getVehicle();

        // go
        boolean up = event.getBlocks().matches("cartlift up");
        Block destination = event.getBlocks().sign;

        BlockFace face;
        if (up) face = BlockFace.UP;
        else face = BlockFace.DOWN;

        while (true) {

            if(destination.getLocation().getBlockY() <= 0 && !up)
                return;
            if(destination.getLocation().getBlockY() >= destination.getWorld().getMaxHeight()-1 && up)
                return;

            destination = destination.getRelative(face);

            if (SignUtil.isSign(destination) && event.getBlocks().base.getType() == destination.getRelative(BlockFace.UP, 1).getType()) {

                ChangedSign state = CraftBookBukkitUtil.toChangedSign(destination);
                String testLine = state.getLine(1);

                if (testLine.equalsIgnoreCase("[CartLift Up]") || testLine.equalsIgnoreCase("[CartLift Down]") || testLine.equalsIgnoreCase("[CartLift]")) {
                    destination = destination.getRelative(BlockFace.UP, 2);
                    break;
                }
            }
        }

        CartUtil.teleport(cart, new Location(destination.getWorld(), destination.getX(), destination.getY(), destination.getZ(), cart.getLocation().getYaw(), cart.getLocation().getPitch()));
    }

    @Override
    public String getName() {

        return "CartLift";
    }

    @Override
    public String[] getApplicableSigns() {

        return new String[] {"CartLift Up", "CartLift Down", "CartLift"};
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("block", "Sets the block that is the base of the elevator mechanic.");
        material = BlockSyntax.getBlock(config.getString("block", BlockTypes.NETHER_BRICKS.getId()), true);
    }
}