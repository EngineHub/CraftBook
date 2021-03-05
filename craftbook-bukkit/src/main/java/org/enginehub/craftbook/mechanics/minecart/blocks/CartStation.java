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
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockEnterEvent;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockRedstoneEvent;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.ItemSyntax;
import org.enginehub.craftbook.util.ItemUtil;
import org.enginehub.craftbook.util.SignUtil;

import static org.enginehub.craftbook.util.CartUtil.stop;

public class CartStation extends CartBlockMechanism {

    @Override
    public void enable() {
        // TODO
//        CraftBookPlugin.inst().registerCommands(StationCommands.class);
    }

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {

        stationInteraction(event.getMinecart(), event.getBlocks());
    }

    @EventHandler
    public void onBlockPower(CartBlockRedstoneEvent event) {

        stationInteraction(event.getMinecart(), event.getBlocks());
    }

    public void stationInteraction(Minecart cart, CartMechanismBlocks blocks) {

        // validate
        if (!blocks.matches(getMaterial())) return;
        if (!blocks.matches("station")) return;

        if (cart == null)
            return;

        // go
        switch (isActive(blocks)) {
            case ON:
                // standardize its speed and direction.
                launch(cart, blocks.sign);
                break;
            case OFF:
            case NA:
                // park it.
                stop(cart);
                // recenter it
                Location l = blocks.rail.getLocation().add(0.5, 0.5, 0.5);
                if (!cart.getLocation().equals(l)) {
                    cart.teleport(l);
                }
                // recentering and parking almost completely prevents more than one cart from getting onto the same
                // station.
                break;
        }
    }

    private static void launch(Minecart cart, Block director) {
        cart.setVelocity(propel(SignUtil.getFacing(director)));
    }

    /**
     * WorldEdit's Vector type collides with Bukkit's Vector type here. It's not pleasant.
     */
    public static Vector propel(BlockFace face) {

        return new Vector(face.getModX() * 0.2, face.getModY() * 0.2, face.getModZ() * 0.2);
    }

    @EventHandler
    public void onVehicleEnter(CartBlockEnterEvent event) {

        // validate
        if (!event.getBlocks().matches(getMaterial())) return;
        if (!event.getBlocks().matches("station")) return;

        if (!event.getBlocks().getSign().getLine(2).equalsIgnoreCase("AUTOSTART")) return;

        if (!event.getBlocks().getSign().getLine(3).isEmpty() && event.getEntered() instanceof Player) {

            ItemStack testItem = ItemSyntax.getItem(event.getBlocks().getSign().getLine(3));
            if (!ItemUtil.areItemsIdentical(testItem, ((Player) event.getEntered()).getItemInHand()))
                return;
        }

        // go
        switch (isActive(event.getBlocks())) {
            case ON:
                // standardize its speed and direction.
                launch(event.getMinecart(), event.getBlocks().sign);
                break;
            case OFF:
            case NA:
                // park it.
                stop(event.getMinecart());
                // recenter it
                Location l = event.getBlocks().rail.getLocation().add(0.5, 0.5, 0.5);
                if (!event.getMinecart().getLocation().equals(l)) {
                    event.getMinecart().teleport(l);
                }
                // recentering and parking almost completely prevents more than one cart from getting onto the same
                // station.
                break;
        }
    }

    @Override
    public String getName() {

        return "Station";
    }

    @Override
    public String[] getApplicableSigns() {

        return new String[] { "station" };
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("block", "Sets the block that is the base of the station mechanic.");
        material = BlockParser.getBlock(config.getString("block", BlockTypes.OBSIDIAN.getId()), true);
    }
}