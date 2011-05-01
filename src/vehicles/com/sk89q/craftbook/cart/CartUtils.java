package com.sk89q.craftbook.cart;

import java.util.ArrayList;

import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.util.*;
import org.bukkit.Location;
import org.bukkit.event.block.*;
import org.bukkit.Material;

import com.sk89q.craftbook.util.*;

public abstract class CartUtils {
    /**
     * Search for a "director" sign one or two blocks below the block that
     * supports the cart tracks.
     *
     * @param base the block beneath the tracks
     * @param keyword the case-insensitive keyword to search for between brackets
     * @param line the line number on which to search for the keyword; -1 searches all lines
     *
     * @return a director sign if one can be found; null otherwise.
     */
    public static Block pickDirector(Block base, String keyword) {
        return pickDirector(base, keyword, 1);
    }

    public static Block pickDirector(Block base, String keyword, int lineNum) {
        if (lineNum == -1) {
            for (int i = 1; i <= 2; i++) {
                Block director = base.getFace(BlockFace.DOWN, i);
                if (SignUtil.isSign(director)) {
                    for (String line : ((Sign) director.getState()).getLines()) {
                        if (line.equalsIgnoreCase("[" + keyword + "]")) return director;
                    }
                }
            }
        }
        if (lineNum < 0 || lineNum > 3) return null; // screw with me, I screw with you
        for (int i = 1; i <= 2; i++) {
            Block director = base.getFace(BlockFace.DOWN, i);
            if (SignUtil.isSign(director))
                if (((Sign) director.getState()).getLine(lineNum).equalsIgnoreCase("["+keyword+"]"))
                    return director;
        }
        return null;
    }
    
    public static Location centerBlock(Location loc) {
        Location toLoc = loc;
        toLoc.setX(loc.getX() + 0.5D);
        toLoc.setZ(loc.getZ() + 0.5D);
        return toLoc;
    }
    
    public static void reverse(Minecart cart) {
        cart.setVelocity(cart.getVelocity().normalize().multiply(-1));
    }
    
    public static void stop(Minecart cart) {
        cart.setVelocity(new Vector(0,0,0));
    }

    /**
     * Ok this is super hacky, I need to get the SourcedBlockRedstoneEvent
     * from the CraftBookCommon but it only sends it to mechanisms not vehicles.
     *
     * @param event
     * @return an arraylist of blocks
     */
    public static ArrayList<Block> poweredByRSCEvent(BlockRedstoneEvent event) {
        Block source = event.getBlock();
        ArrayList<Block> affected = new ArrayList<Block>();
        String affectedStuff = new String();
        //if block is torch > check for orientation
        if (source.getType() == Material.REDSTONE_TORCH_OFF) {
            return affected;
        } else if (source.getType() == Material.REDSTONE_TORCH_ON) {
            byte data = source.getData();
            switch (data) {
                case 5:
                    affectedStuff = "UNSWE";
                    break;
                case 1:
                    affectedStuff = "UDSWE";
                    break;
                case 2:
                    affectedStuff = "UDNWE";
                    break;
                case 3:
                    affectedStuff = "UDNSW";
                    break;
                case 4:
                    affectedStuff = "UDNSE";
                    break;
                default: //stop fucking with torches you prick
                    break;
            }
            if (affectedStuff.contains("U")) affected.add(source.getFace(BlockFace.UP));
            if (affectedStuff.contains("D")) affected.add(source.getFace(BlockFace.DOWN));
            if (affectedStuff.contains("N")) affected.add(source.getFace(BlockFace.NORTH));
            if (affectedStuff.contains("S")) affected.add(source.getFace(BlockFace.SOUTH));
            if (affectedStuff.contains("W")) affected.add(source.getFace(BlockFace.WEST));
            if (affectedStuff.contains("E")) affected.add(source.getFace(BlockFace.EAST));
            return affected;
        } else if (source.getType() == Material.REDSTONE_WIRE) {
            affected.add(source.getFace(BlockFace.UP));
            affected.add(source.getFace(BlockFace.NORTH));
            affected.add(source.getFace(BlockFace.SOUTH));
            affected.add(source.getFace(BlockFace.WEST));
            affected.add(source.getFace(BlockFace.EAST));
            return affected;
        }
        return affected;
    }
}
