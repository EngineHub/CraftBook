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
import org.bukkit.block.BlockFace;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.RedstoneUtil.Power;
import org.enginehub.craftbook.util.SignUtil;

import java.util.List;

public class CartReverser extends CartBlockMechanism {

    private static final List<String> SIGNS = List.of("Reverse");

    public CartReverser(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {
        if (event.isMinor() || !event.getBlocks().matches(getBlock()) || Power.OFF == isActive(event.getBlocks())) {
            return;
        }

        Side side = event.getBlocks().matches("reverse");
        if (!event.getBlocks().hasSign() || side == null) {
            reverseCart(event.getMinecart());
            return;
        }

        BlockFace dir = SignUtil.getBack(event.getBlocks().sign());

        Vector normalVelocity = event.getMinecart().getVelocity().normalize();

        switch (dir) {
            case NORTH:
                if (normalVelocity.getBlockZ() != -1) {
                    reverseCart(event.getMinecart());
                }
                break;
            case SOUTH:
                if (normalVelocity.getBlockZ() != 1) {
                    reverseCart(event.getMinecart());
                }
                break;
            case EAST:
                if (normalVelocity.getBlockX() != 1) {
                    reverseCart(event.getMinecart());
                }
                break;
            case WEST:
                if (normalVelocity.getBlockX() != -1) {
                    reverseCart(event.getMinecart());
                }
                break;
            default:
                reverseCart(event.getMinecart());
        }
    }

    private void reverseCart(Minecart cart) {
        cart.setVelocity(cart.getVelocity().multiply(-1));
    }

    @Override
    public List<String> getApplicableSigns() {
        return SIGNS;
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("block", "The block the Cart Reverser mechanic uses.");
        setBlock(BlockParser.getBlock(config.getString("block", BlockTypes.WHITE_WOOL.id()), true));
    }
}
