package com.sk89q.craftbook.mechanics.minecart.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.minecart.StationManager;
import com.sk89q.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
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

/**
 * @author LordEnki
 */

public class CartSorter extends CartBlockMechanism {

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {

        // care?
        if (!event.getBlocks().matches(getMaterial())) return;
        if (event.isMinor()) return;

        // validate
        if (!event.getBlocks().matches("sort")) return;
        ChangedSign sign = event.getBlocks().getSign();

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
        BlockFace next = SignUtil.getFacing(event.getBlocks().sign);
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
        Block targetTrack = event.getBlocks().rail.getRelative(next);

        // now check sanity real quick that there's actually a track after this,
        // and then make the change.
        if (targetTrack.getType() == Material.RAIL) {
            targetTrack.setBlockData(trackData);
        }
    }

    private enum Hand {
        STRAIGHT, LEFT, RIGHT
    }

    public static boolean isSortApplicable(String fullLine, Minecart minecart) {
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
                        if (ItemUtil.areItemsSimilar(player.getItemInHand(), item))
                            return true;
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
            if (line.startsWith("#")) {
                if (player != null) {
                    String selectedStation = StationManager.getStation(player.getName());
                    return line.equalsIgnoreCase('#' + selectedStation);
                }
            }
        }
        return false;
    }

    @Override
    public String getName() {

        return "Sorter";
    }

    @Override
    public String[] getApplicableSigns() {

        return new String[] {"Sort"};
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "block", "Sets the block that is the base of the sorter mechanic.");
        material = BlockSyntax.getBlock(config.getString(path + "block", BlockTypes.NETHERRACK.getId()), true);
    }
}