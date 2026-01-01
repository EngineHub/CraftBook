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
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.RedstoneUtil;
import org.enginehub.craftbook.util.SignUtil;

import java.util.List;

public class CartLift extends CartBlockMechanism {

    private static final List<String> SIGNS = List.of("CartLift Up", "CartLift Down", "CartLift");

    public CartLift(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {
        if (event.isMinor()
            || !event.getBlocks().matches(getBlock())
            || RedstoneUtil.Power.OFF == isActive(event.getBlocks())) {
            return;
        }

        Side side = event.getBlocks().matches("cartlift up", "cartlift down");
        if (side == null) {
            return;
        }

        Minecart cart = event.getMinecart();

        boolean up = event.getBlocks().matches("cartlift up", side);
        Block destination = event.getBlocks().sign();
        Material baseType = event.getBlocks().base().getType();

        BlockFace face = up ? BlockFace.UP : BlockFace.DOWN;

        while (true) {
            if (destination.getLocation().getBlockY() <= 0 && !up) {
                return;
            }
            if (destination.getLocation().getBlockY() >= destination.getWorld().getMaxHeight() - 1 && up) {
                return;
            }

            destination = destination.getRelative(face);

            if (SignUtil.isSign(destination) && baseType == destination.getRelative(BlockFace.UP, 1).getType()) {
                ChangedSign state = ChangedSign.create(destination, side);
                String testLine = PlainTextComponentSerializer.plainText().serialize(state.getLine(1));

                if (testLine.equalsIgnoreCase("[CartLift Up]") || testLine.equalsIgnoreCase("[CartLift Down]") || testLine.equalsIgnoreCase("[CartLift]")) {
                    destination = destination.getRelative(BlockFace.UP, 2);
                    break;
                }
            }
        }

        Vector oldVelocity = cart.getVelocity();
        Location newLocation = destination.getLocation();
        newLocation.setDirection(cart.getLocation().getDirection());
        cart.teleport(newLocation);
        cart.setVelocity(oldVelocity);

        for (Entity entity : cart.getPassengers()) {
            if (entity instanceof Player player) {
                CraftBookPlayer localPlayer = CraftBookPlugin.inst().wrapPlayer(player);
                localPlayer.printInfo(TranslatableComponent.of(up ? "craftbook.minecartelevator.moved-up" : "craftbook.minecartelevator.moved-down"));
            }
        }
    }

    @Override
    public List<String> getApplicableSigns() {
        return SIGNS;
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("block", "Sets the block that is the base of the elevator mechanic.");
        setBlock(BlockParser.getBlock(config.getString("block", BlockTypes.NETHER_BRICKS.id()), true));
    }
}
