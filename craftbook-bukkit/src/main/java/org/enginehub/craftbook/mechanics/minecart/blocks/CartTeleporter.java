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

import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import org.enginehub.craftbook.util.BlockSyntax;
import org.enginehub.craftbook.util.CartUtil;
import org.enginehub.craftbook.util.LocationUtil;
import org.enginehub.craftbook.util.RegexUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;

public class CartTeleporter extends CartBlockMechanism {

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {
        // validate
        if (!event.getBlocks().matches(getMaterial())) return;
        if (!event.getBlocks().hasSign()) return;
        if (event.isMinor()) return;
        if (!event.getBlocks().matches("teleport")) return;

        // go
        World world = event.getMinecart().getWorld();
        String[] pts = RegexUtil.COMMA_PATTERN.split(event.getBlocks().getSign().getLine(2).trim(), 3);
        if (!event.getBlocks().getSign().getLine(3).trim().isEmpty()) {
            world = event.getMinecart().getServer().getWorld(event.getBlocks().getSign().getLine(3).trim());
        }

        double x;
        double y;
        double z;
        try {
            x = Double.parseDouble(pts[0].trim());
            y = Double.parseDouble(pts[1].trim());
            z = Double.parseDouble(pts[2].trim());
        } catch (NumberFormatException e) {
            // incorrect format, just set them still and let them figure it out
            if (event.getBlocks().from != null) {
                x = event.getBlocks().from.getX();
                y = event.getBlocks().from.getY();
                z = event.getBlocks().from.getZ();
            } else {
                x = event.getBlocks().rail.getX();
                y = event.getBlocks().rail.getY();
                z = event.getBlocks().rail.getZ();
            }
            CartUtil.stop(event.getMinecart());
        }

        Location loc = LocationUtil
                .center(new Location(world, x, y, z, event.getMinecart().getLocation().getYaw(), event.getMinecart().getLocation().getPitch()));
        loc.getChunk().load(true);
        CartUtil.teleport(event.getMinecart(), loc);
    }

    @Override
    public boolean verify(ChangedSign sign, CraftBookPlayer player) {

        String[] pts = RegexUtil.COMMA_PATTERN.split(sign.getLine(2).trim(), 3);
        try {
            Double.parseDouble(pts[0].trim());
            Double.parseDouble(pts[1].trim());
            Double.parseDouble(pts[2].trim());
        } catch (NumberFormatException e) {
            player.printError("Line 3 must contain coordinates seperated by a comma! (x,y,z)");
            return false;
        }
        return true;
    }

    @Override
    public String getName() {

        return "Teleporter";
    }

    @Override
    public String[] getApplicableSigns() {

        return new String[] {"Teleport"};
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("block", "Sets the block that is the base of the teleport mechanic.");
        material = BlockSyntax.getBlock(config.getString("block", BlockTypes.EMERALD_BLOCK.getId()), true);
    }
}