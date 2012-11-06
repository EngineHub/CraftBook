package com.sk89q.craftbook.cart;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.bukkit.VehiclesPlugin;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockID;

/*
 * @contributor LordEnki
 */

public class CartSorter extends CartMechanism {

    private static VehiclesPlugin plugin;

    public CartSorter(VehiclesPlugin plugin){
        CartSorter.plugin = plugin;
    }

    @Override
    public void impact(Minecart cart, CartMechanismBlocks blocks, boolean minor) {
        // care?
        if (minor) return;

        // validate
        if (cart == null) return;
        if (!blocks.matches("sort")) return;
        Sign sign = (Sign) blocks.sign.getState();

        // pi(sign)hich sort conditions apply
        //  (left dominates if both apply)
        Hand dir = Hand.STRAIGHT;
        if (isSortApplicable(sign.getLine(2), cart)) {
            dir = Hand.LEFT;
        } else if (isSortApplicable(sign.getLine(3), cart)) {
            dir = Hand.RIGHT;
        }

        // pick the track block to modify and the curve to give it.
        //   perhaps oddly, it's the sign facing that determines the concepts of left and right, and not the track.
        //    this is required since there's not a north track and a south track; just a north-south track type.
        byte trackData;
        BlockFace next = SignUtil.getFacing(blocks.sign);
        switch (next) {
            case WEST:
                switch (dir) {
                    case LEFT:
                        trackData = 9;
                        break;
                    case RIGHT:
                        trackData = 8;
                        break;
                    default:
                        trackData = 0;
                }
                break;
            case EAST:
                switch (dir) {
                    case LEFT:
                        trackData = 7;
                        break;
                    case RIGHT:
                        trackData = 6;
                        break;
                    default:
                        trackData = 0;
                }
                break;
            case NORTH:
                switch (dir) {
                    case LEFT:
                        trackData = 6;
                        break;
                    case RIGHT:
                        trackData = 9;
                        break;
                    default:
                        trackData = 1;
                }
                break;
            case SOUTH:
                switch (dir) {
                    case LEFT:
                        trackData = 8;
                        break;
                    case RIGHT:
                        trackData = 7;
                        break;
                    default:
                        trackData = 1;
                }
                break;
            default:
                //XXX ohgod the sign's not facing any sensible direction at all, who do we tell?
                return;
       }
        Block targetTrack = blocks.rail.getRelative(next);

        // now check sanity real quick that there's actually a track after this,
        // and then make the change.
        if (targetTrack.getTypeId() == BlockID.MINECART_TRACKS) {
            targetTrack.setData(trackData);
        }
    }

    private enum Hand {
        STRAIGHT, LEFT, RIGHT
    }

    public static boolean isSortApplicable(String line, Minecart minecart) {
        if (line.equalsIgnoreCase("All")) return true;
        Entity test = minecart.getPassenger();
        Player player = null;
        if (test instanceof Player) {
            player = (Player) test;
        }

        if ((line.equalsIgnoreCase("Unoccupied")
                || line.equalsIgnoreCase("Empty"))
                && minecart.getPassenger() == null) return true;

        if (line.equalsIgnoreCase("Storage")
                && minecart instanceof StorageMinecart)
            return true;
        else if (line.equalsIgnoreCase("Powered")
                && minecart instanceof PoweredMinecart)
            return true;
        else if (line.equalsIgnoreCase("Minecart")
                && minecart instanceof Minecart) return true;

        if ((line.equalsIgnoreCase("Occupied")
                || line.equalsIgnoreCase("Full"))
                && minecart.getPassenger() != null) return true;

        if (line.equalsIgnoreCase("Animal")
                && test instanceof Animals) return true;

        if (line.equalsIgnoreCase("Mob")
                && test instanceof Monster) return true;

        if ((line.equalsIgnoreCase("Player")
                || line.equalsIgnoreCase("Ply"))
                && player != null) return true;

        String[] parts = line.split(":");

        if (parts.length >= 2) if (player != null && parts[0].equalsIgnoreCase("Held")) {
            try {
                int item = Integer.parseInt(parts[1]);
                if (player.getItemInHand().getTypeId() == item) return true;
            } catch (NumberFormatException ignored) {
            }
        } else if (player != null && parts[0].equalsIgnoreCase("Ply")) {
            if (parts[1].equalsIgnoreCase(player.getName())) return true;
        } else if (parts[0].equalsIgnoreCase("Mob")) {
            String testMob = parts[1];
            test.toString().toLowerCase().equalsIgnoreCase(testMob);
        } else if (minecart instanceof StorageMinecart && parts[0].equalsIgnoreCase("Ctns")) {
            StorageMinecart storageCart = (StorageMinecart) minecart;
            Inventory storageInventory = storageCart.getInventory();

            if (parts.length == 4) {
                try {
                    int item = Integer.parseInt(parts[1]);
                    short durability = Short.parseShort(parts[2]);
                    int index = Math.min(Math.max(Integer.parseInt(parts[3]) - 1, 0), storageInventory.getContents().length - 1);
                    ItemStack indexed = storageInventory.getContents()[index];
                    if (indexed != null && indexed.equals(new ItemStack(item, 1, durability))) return true;
                } catch (NumberFormatException ignored) {
                }
            } else if (parts.length == 3) {
                try {
                    int item = Integer.parseInt(parts[1]);
                    short durability = Short.parseShort(parts[2]);
                    if (storageInventory.contains(new ItemStack(item, 1, durability))) return true;
                } catch (NumberFormatException ignored) {
                }
            } else {
                try {
                    int item = Integer.parseInt(parts[1]);
                    if (storageInventory.contains(item)) return true;
                } catch (NumberFormatException ignored) {
                }
            }
        }if(line.startsWith("#")){
            if(player!=null){
                String stationName = line;
                String selectedStation = plugin.getStation(player.getName());
                return stationName.equalsIgnoreCase("#" + selectedStation);
            }
        }

        return false;
    }

    @Override
    public void enter(Minecart cart, Entity entity, CartMechanismBlocks blocks,
            boolean minor) {

    }
}