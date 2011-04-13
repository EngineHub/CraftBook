package com.sk89q.craftbook.cart;

import java.util.ArrayList;

import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.util.*;
import org.bukkit.event.block.*;
import org.bukkit.Material;

import com.sk89q.craftbook.util.*;

public abstract class CartUtils {
    /**
     * Search for a "director" sign one or two blocks below the block that
     * supports the cart tracks.
     * 
     * @param base
     *            the block beneath the tracks
     * @param keyword
     *            the case-insensitive keyword to search for between brackets on
     *            the second line of the sign.
     * @return a director sign if one can be found; null otherwise.
     */
    public static Block pickDirector(Block base, String keyword) {
        for (int i = 1; i <= 2; i++) {
            Block director = base.getFace(BlockFace.DOWN, i);
            if (SignUtil.isSign(director))
                if (((Sign)director.getState()).getLine(1).equalsIgnoreCase("["+keyword+"]"))
                    return director;
        }
        return null;
    }
    
    
    public static void reverse(Minecart cart) {
        cart.setVelocity(cart.getVelocity().normalize().multiply(-1));
    }
    
    public static void stop(Minecart cart) {
        cart.setVelocity(new Vector(0,0,0));
    }

    /**
     * Kami-sama, is this really necessary?
     *
     * @param event
     * @return an arraylist of blocks
     * @author IDon'tGetPaidForThis
     */
    public static ArrayList<Block> poweredByRSCEvent(BlockRedstoneEvent event) {
        Block source = event.getBlock();
        ArrayList<Block> affected = new ArrayList<Block>();

        //if block is torch > check for orientation
        if (source.getType() == Material.REDSTONE_TORCH_OFF) {
            return affected;
        } else if (source.getType() == Material.REDSTONE_TORCH_ON) {
            byte data = source.getData();
            switch (data) {
                case 5:
                    affected.add(source.getFace(BlockFace.UP));
                    affected.add(source.getFace(BlockFace.NORTH));
                    affected.add(source.getFace(BlockFace.SOUTH));
                    affected.add(source.getFace(BlockFace.WEST));
                    affected.add(source.getFace(BlockFace.EAST));
                    break;
                case 1:
                    affected.add(source.getFace(BlockFace.UP));
                    affected.add(source.getFace(BlockFace.DOWN));
                    affected.add(source.getFace(BlockFace.SOUTH));
                    affected.add(source.getFace(BlockFace.WEST));
                    affected.add(source.getFace(BlockFace.EAST));
                    break;
                case 2:
                    affected.add(source.getFace(BlockFace.UP));
                    affected.add(source.getFace(BlockFace.DOWN));
                    affected.add(source.getFace(BlockFace.NORTH));
                    affected.add(source.getFace(BlockFace.WEST));
                    affected.add(source.getFace(BlockFace.EAST));
                    break;
                case 3:
                    affected.add(source.getFace(BlockFace.UP));
                    affected.add(source.getFace(BlockFace.DOWN));
                    affected.add(source.getFace(BlockFace.NORTH));
                    affected.add(source.getFace(BlockFace.SOUTH));
                    affected.add(source.getFace(BlockFace.WEST));
                    break;
                case 4:
                    affected.add(source.getFace(BlockFace.UP));
                    affected.add(source.getFace(BlockFace.DOWN));
                    affected.add(source.getFace(BlockFace.NORTH));
                    affected.add(source.getFace(BlockFace.SOUTH));
                    affected.add(source.getFace(BlockFace.EAST));
                    break;
                default: //stop fucking with torches you prick
                    break;
            }
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
