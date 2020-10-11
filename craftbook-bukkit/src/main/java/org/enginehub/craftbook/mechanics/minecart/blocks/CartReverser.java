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
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import org.enginehub.craftbook.util.BlockSyntax;
import org.enginehub.craftbook.util.RedstoneUtil.Power;
import org.enginehub.craftbook.util.SignUtil;

public class CartReverser extends CartBlockMechanism {

    public void reverse(Minecart cart) {
        cart.setVelocity(cart.getVelocity().normalize().multiply(-1));
    }

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {

        // validate
        if (!event.getBlocks().matches(getMaterial())) return;
        if (event.isMinor()) return;

        // enabled?
        if (Power.OFF == isActive(event.getBlocks())) return;

        if (!event.getBlocks().hasSign() || !event.getBlocks().matches("reverse")) {
            reverse(event.getMinecart());
            return;
        }

        BlockFace dir = SignUtil.getFacing(event.getBlocks().sign);

        Vector normalVelocity = event.getMinecart().getVelocity().normalize();

        switch (dir) {
            case NORTH:
                if (normalVelocity.getBlockZ() != -1) {
                    reverse(event.getMinecart());
                }
                break;
            case SOUTH:
                if (normalVelocity.getBlockZ() != 1) {
                    reverse(event.getMinecart());
                }
                break;
            case EAST:
                if (normalVelocity.getBlockX() != 1) {
                    reverse(event.getMinecart());
                }
                break;
            case WEST:
                if (normalVelocity.getBlockX() != -1) {
                    reverse(event.getMinecart());
                }
                break;
            default:
                reverse(event.getMinecart());
        }
    }

    @Override
    public String getName() {

        return "Reverser";
    }

    @Override
    public String[] getApplicableSigns() {

        return new String[] { "reverse" };
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("block", "Sets the block that is the base of the reverse mechanic.");
        material = BlockSyntax.getBlock(config.getString("block", BlockTypes.WHITE_WOOL.getId()), true);
    }
}