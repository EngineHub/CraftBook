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

import com.google.common.collect.ImmutableList;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.CartUtil;
import org.enginehub.craftbook.util.SignUtil;

import java.util.List;

public class CartLift extends CartBlockMechanism {

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {

        // validate
        if (!event.getBlocks().matches(getBlock())) return;
        if (!event.getBlocks().hasSign()) return;
        if (event.isMinor()) return;
        if (!(event.getBlocks().matches("cartlift up") || event.getBlocks().matches("cartlift down")))
            return;

        Minecart cart = (Minecart) event.getVehicle();

        // go
        boolean up = event.getBlocks().matches("cartlift up");
        Block destination = event.getBlocks().sign();

        BlockFace face;
        if (up) face = BlockFace.UP;
        else face = BlockFace.DOWN;

        while (true) {

            if (destination.getLocation().getBlockY() <= 0 && !up)
                return;
            if (destination.getLocation().getBlockY() >= destination.getWorld().getMaxHeight() - 1 && up)
                return;

            destination = destination.getRelative(face);

            if (SignUtil.isSign(destination) && event.getBlocks().base().getType() == destination.getRelative(BlockFace.UP, 1).getType()) {

                ChangedSign state = CraftBookBukkitUtil.toChangedSign(destination);
                String testLine = state.getLine(1);

                if (testLine.equalsIgnoreCase("[CartLift Up]") || testLine.equalsIgnoreCase("[CartLift Down]") || testLine.equalsIgnoreCase("[CartLift]")) {
                    destination = destination.getRelative(BlockFace.UP, 2);
                    break;
                }
            }
        }

        Location newLocation = destination.getLocation();
        newLocation.setYaw(cart.getLocation().getYaw());
        newLocation.setPitch(cart.getLocation().getPitch());
        cart.teleport(newLocation, true);
    }

    @Override
    public List<String> getApplicableSigns() {

        return ImmutableList.copyOf(new String[] { "CartLift Up", "CartLift Down", "CartLift" });
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("block", "Sets the block that is the base of the elevator mechanic.");
        setBlock(BlockParser.getBlock(config.getString("block", BlockTypes.NETHER_BRICKS.getId()), true));
    }
}