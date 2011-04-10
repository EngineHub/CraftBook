package com.sk89q.craftbook.cart;

import static com.sk89q.craftbook.cart.CartUtils.pickDirector;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;

import com.sk89q.craftbook.util.*;

public class CartSorter extends CartMechanism {
    public void impact(Minecart cart, Block entered, Block from) {
        Block director = pickDirector(entered.getFace(BlockFace.DOWN, 1), "sort");
        if (director == null) return;
        Sign sign = (Sign) director.getState();
        
        // pick which sort conditions apply
        //  (left dominates if both apply)
        Hand dir = Hand.STRAIGHT;
        if (isSortApplicable(sign.getLine(2), cart)) {
            dir = Hand.LEFT;
        } else if (isSortApplicable((sign).getLine(3), cart)) {
            dir = Hand.RIGHT;
        }
        
        // pick the track block to modify and the curve to give it. 
        //   perhaps oddly, it's the sign facing that determines the concepts of left and right, and not the track.
        //    this is required since there's not a north track and a south track; just a north-south track type.
        byte trackData;
        BlockFace next = SignUtil.getFacing(director);
        switch (next) {
        case WEST:
            switch (dir) {
            case LEFT:
                trackData = 9; break;
            case RIGHT:
                trackData = 8; break;
            default:
                trackData = 0;
            }
            break;
        case EAST:
            switch (dir) {
            case LEFT:
                trackData = 7; break;
            case RIGHT:
                trackData = 6; break;
            default:
                trackData = 0;
            }
            break;
        case NORTH:
            switch (dir) {
            case LEFT:
                trackData = 6; break;
            case RIGHT:
                trackData = 9; break;
            default:
                trackData = 1;
            }
            break;
        case SOUTH:
            switch (dir) {
            case LEFT:
                trackData = 8; break;
            case RIGHT:
                trackData = 7; break;
            default:
                trackData = 1;
            }
            break;
        default:
            //XXX ohgod the sign's not facing any sensible direction at all, who do we tell?
            return;
        }
        Block targetTrack = entered.getFace(next);
        
        // now check sanity real quick that there's actually a track after this,
        // and then make the change.
        if (targetTrack.getType() == Material.RAILS)
            targetTrack.setData(trackData);
    }
    
    private enum Hand {
        STRAIGHT, LEFT, RIGHT;
    }
    
    public static boolean isSortApplicable(String line, Minecart minecart) {
        if (line.equalsIgnoreCase("All")) {
            return true;
        }
        Entity test = minecart.getPassenger();
        Player player = null;
        if (test instanceof Player)
            player = (Player) test;

        if ((line.equalsIgnoreCase("Unoccupied")
                || line.equalsIgnoreCase("Empty"))
                && minecart.getPassenger() == null) {
            return true;
        }

        if (line.equalsIgnoreCase("Storage")
                && minecart instanceof StorageMinecart) {
            return true;
        } else if (line.equalsIgnoreCase("Powered")
                && minecart instanceof PoweredMinecart) {
            return true;
        } else if (line.equalsIgnoreCase("Minecart")
                && minecart instanceof Minecart) {
            return true;
        }

        if ((line.equalsIgnoreCase("Occupied")
                || line.equalsIgnoreCase("Full"))
                && minecart.getPassenger() != null) {
            return true;
        }

        if (line.equalsIgnoreCase("Animal")
                && test instanceof Animals) {
            return true;
        }

        if (line.equalsIgnoreCase("Mob")
                && test instanceof Monster) {
            return true;
        }

        if ((line.equalsIgnoreCase("Player")
                || line.equalsIgnoreCase("Ply"))
                && player != null) {
            return true;
        }

        String[] parts = line.split(":");

        if (parts.length >= 2) {
            if (player != null && parts[0].equalsIgnoreCase("Held")) {
                try {
                    int item = Integer.parseInt(parts[1]);
                    if (player.getItemInHand().getTypeId() == item) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                }
            } else if (player != null && parts[0].equalsIgnoreCase("Ply")) {
                if (parts[1].equalsIgnoreCase(player.getName())) {
                    return true;
                }
            } else if (parts[0].equalsIgnoreCase("Mob")) {
                String testMob = parts[1];
                test.toString().toLowerCase().equalsIgnoreCase(testMob);
            }
        }

        return false;
    }
}
