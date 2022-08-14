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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.mechanic.MechanicTypes;
import org.enginehub.craftbook.mechanic.exception.MechanicInitializationException;
import org.enginehub.craftbook.mechanics.minecart.blocks.station.CartStation;
import org.enginehub.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.ItemSyntax;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.SignUtil;

import java.util.List;

/**
 * @author LordEnki
 */

public class CartSorter extends CartBlockMechanism {

    private CartStation cartStation;

    @Override
    public void enable() throws MechanicInitializationException {
        super.enable();

        cartStation = CraftBook.getInstance().getPlatform().getMechanicManager().getMechanic(MechanicTypes.MINECART_STATION).orElse(null);
    }

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {

        // care?
        if (!event.getBlocks().matches(getBlock())) return;
        if (event.isMinor()) return;

        // validate
        if (!event.getBlocks().matches("sort")) return;
        ChangedSign sign = event.getBlocks().getChangedSign();

        // pi(sign)hich sort conditions apply
        // (left dominates if both apply)
        Hand dir = Hand.STRAIGHT;
        if (isSortApplicable(sign.getLine(2), event.getMinecart())) {
            dir = Hand.LEFT;
        } else if (isSortApplicable(sign.getLine(3), event.getMinecart())) {
            dir = Hand.RIGHT;
        }

        // pick the track block to modify and the curve to give it.
        // perhaps oddly, it's the sign facing that determines the concepts of left and right, and not the track.
        // this is required since there's not a north track and a south track; just a north-south track type.
        Rail trackData = (Rail) Material.RAIL.createBlockData();
        BlockFace next = SignUtil.getFacing(event.getBlocks().sign());
        switch (next) {
            case SOUTH:
                switch (dir) {
                    case LEFT:
                        trackData.setShape(Rail.Shape.NORTH_EAST);
                        break;
                    case RIGHT:
                        trackData.setShape(Rail.Shape.NORTH_WEST);
                        break;
                    default:
                        trackData.setShape(Rail.Shape.NORTH_SOUTH);
                }
                break;
            case NORTH:
                switch (dir) {
                    case LEFT:
                        trackData.setShape(Rail.Shape.SOUTH_WEST);
                        break;
                    case RIGHT:
                        trackData.setShape(Rail.Shape.SOUTH_EAST);
                        break;
                    default:
                        trackData.setShape(Rail.Shape.NORTH_SOUTH);
                }
                break;
            case WEST:
                switch (dir) {
                    case LEFT:
                        trackData.setShape(Rail.Shape.SOUTH_EAST);
                        break;
                    case RIGHT:
                        trackData.setShape(Rail.Shape.NORTH_EAST);
                        break;
                    default:
                        trackData.setShape(Rail.Shape.EAST_WEST);
                }
                break;
            case EAST:
                switch (dir) {
                    case LEFT:
                        trackData.setShape(Rail.Shape.NORTH_WEST);
                        break;
                    case RIGHT:
                        trackData.setShape(Rail.Shape.SOUTH_WEST);
                        break;
                    default:
                        trackData.setShape(Rail.Shape.EAST_WEST);
                }
                break;
            default:
                return;
        }
        Block targetTrack = event.getBlocks().rail().getRelative(next);

        // now check sanity real quick that there's actually a track after this,
        // and then make the change.
        if (targetTrack.getType() == Material.RAIL) {
            targetTrack.setBlockData(trackData);
        }
    }

    private enum Hand {
        STRAIGHT, LEFT, RIGHT
    }

    public boolean isSortApplicable(String fullLine, Minecart minecart) {
        for (String line : RegexUtil.COMMA_PATTERN.split(fullLine)) {
            if (line.equalsIgnoreCase("All"))
                return true;
            Entity test = minecart.getPassenger();
            Player player = null;
            if (test instanceof Player) {
                player = (Player) test;
            }

            if ((line.equalsIgnoreCase("Unoccupied") || line.equalsIgnoreCase("Empty")) && minecart.getPassenger() == null)
                return true;

            if (line.equalsIgnoreCase("Storage") && minecart instanceof StorageMinecart)
                return true;
            else if (line.equalsIgnoreCase("Powered") && minecart instanceof PoweredMinecart)
                return true;
            else if (line.equalsIgnoreCase("Minecart"))
                return true;

            if ((line.equalsIgnoreCase("Occupied") || line.equalsIgnoreCase("Full")) && minecart.getPassenger() != null)
                return true;

            if (line.equalsIgnoreCase("Animal") && test instanceof Animals)
                return true;

            if (line.equalsIgnoreCase("Mob") && test instanceof Monster)
                return true;

            if ((line.equalsIgnoreCase("Player") || line.equalsIgnoreCase("Ply")) && player != null)
                return true;

            String[] parts = RegexUtil.COLON_PATTERN.split(line);

            if (parts.length >= 2)
                if (player != null && parts[0].equalsIgnoreCase("Held")) {
                    try {
                        ItemStack item = ItemSyntax.getItem(parts[1]);
                        if (item.getType() == player.getInventory().getItemInMainHand().getType()) {
                            return true;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                } else if (player != null && parts[0].equalsIgnoreCase("Ply")) {
                    if (parts[1].equalsIgnoreCase(player.getName()))
                        return true;
                } else if (parts[0].equalsIgnoreCase("Mob")) {
                    String testMob = parts[1];
                    return test.getType().toString().equalsIgnoreCase(testMob);
                } else if (minecart instanceof StorageMinecart && parts[0].equalsIgnoreCase("Ctns")) {
                    StorageMinecart storageCart = (StorageMinecart) minecart;
                    Inventory storageInventory = storageCart.getInventory();

                    if (parts.length == 4) {
                        try {
                            ItemStack item = ItemSyntax.getItem(parts[1] + ':' + parts[2]);
                            int index = Math.min(Math.max(Integer.parseInt(parts[3]) - 1, 0),
                                storageInventory.getContents().length - 1);
                            ItemStack indexed = storageInventory.getContents()[index];
                            if (indexed != null && indexed.equals(item))
                                return true;
                        } catch (NumberFormatException ignored) {
                        }
                    } else if (parts.length == 3) {
                        try {
                            ItemStack item = ItemSyntax.getItem(parts[1] + parts[2]);
                            if (storageInventory.contains(item))
                                return true;
                        } catch (NumberFormatException ignored) {
                        }
                    } else if (parts[1].equalsIgnoreCase("!")) {
                        for (ItemStack item : storageInventory.getContents()) {
                            if (item != null) {
                                return false;
                            }
                        }
                        return true;
                    } else {
                        try {
                            ItemStack item = ItemSyntax.getItem(parts[1]);
                            if (storageInventory.contains(item))
                                return true;
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            if (line.startsWith("#") && cartStation != null) {
                if (player != null) {
                    String selectedStation = cartStation.getStation(player.getUniqueId());
                    return line.equalsIgnoreCase('#' + selectedStation);
                }
            }
        }
        return false;
    }

    @Override
    public List<String> getApplicableSigns() {
        return ImmutableList.copyOf(new String[] { "Sort" });
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("block", "Sets the block that is the base of the sorter mechanic.");
        setBlock(BlockParser.getBlock(config.getString("block", BlockTypes.NETHERRACK.getId()), true));
    }
}
