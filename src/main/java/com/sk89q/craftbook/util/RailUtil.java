package com.sk89q.craftbook.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.blocks.BlockID;

public class RailUtil {

    public static List<Chest> getNearbyChests(Block body) {

        int x = body.getX();
        int y = body.getY();
        int z = body.getZ();
        List<Chest> containers = new ArrayList<Chest>();
        if (body.getWorld().getBlockAt(x, y, z).getType() == Material.CHEST || body.getWorld().getBlockAt(x, y, z).getType() == Material.TRAPPED_CHEST) {
            containers.add((Chest) body.getWorld().getBlockAt(x, y, z).getState());
        }
        if (body.getWorld().getBlockAt(x - 1, y, z).getType() == Material.CHEST || body.getWorld().getBlockAt(x - 1, y, z).getType() == Material.TRAPPED_CHEST) {
            containers.add((Chest) body.getWorld().getBlockAt(x - 1, y, z).getState());
            if (body.getWorld().getBlockAt(x - 2, y, z).getType() == Material.CHEST || body.getWorld().getBlockAt(x - 2, y, z).getType() == Material.TRAPPED_CHEST) {
                containers.add((Chest) body.getWorld().getBlockAt(x - 2, y, z).getState());
            }
        }
        if (body.getWorld().getBlockAt(x + 1, y, z).getType() == Material.CHEST || body.getWorld().getBlockAt(x + 1, y, z).getType() == Material.TRAPPED_CHEST) {
            containers.add((Chest) body.getWorld().getBlockAt(x + 1, y, z).getState());
            if (body.getWorld().getBlockAt(x + 2, y, z).getType() == Material.CHEST || body.getWorld().getBlockAt(x + 2, y, z).getType() == Material.TRAPPED_CHEST) {
                containers.add((Chest) body.getWorld().getBlockAt(x + 2, y, z).getState());
            }
        }
        if (body.getWorld().getBlockAt(x, y, z - 1).getType() == Material.CHEST || body.getWorld().getBlockAt(x, y, z - 1).getType() == Material.TRAPPED_CHEST) {
            containers.add((Chest) body.getWorld().getBlockAt(x, y, z - 1).getState());
            if (body.getWorld().getBlockAt(x, y, z - 2).getType() == Material.CHEST || body.getWorld().getBlockAt(x, y, z - 2).getType() == Material.TRAPPED_CHEST) {
                containers.add((Chest) body.getWorld().getBlockAt(x, y, z - 2).getState());
            }
        }
        if (body.getWorld().getBlockAt(x, y, z + 1).getType() == Material.CHEST  || body.getWorld().getBlockAt(x, y, z + 1).getType() == Material.TRAPPED_CHEST) {
            containers.add((Chest) body.getWorld().getBlockAt(x, y, z + 1).getState());
            if (body.getWorld().getBlockAt(x, y, z + 2).getType() == Material.CHEST || body.getWorld().getBlockAt(x, y, z + 2).getType() == Material.TRAPPED_CHEST) {
                containers.add((Chest) body.getWorld().getBlockAt(x, y, z + 2).getState());
            }
        }

        return containers;
    }

    private static final int[] trackBlocks = new int[] { BlockID.MINECART_TRACKS, BlockID.POWERED_RAIL, BlockID.DETECTOR_RAIL, BlockID.ACTIVATOR_RAIL};

    public static boolean isTrack(int id) {

        if (CraftBookPlugin.inst().getConfiguration().minecartMoreRailsPressurePlate)
            if (id == BlockID.STONE_PRESSURE_PLATE || id == BlockID.WOODEN_PRESSURE_PLATE || id == BlockID.PRESSURE_PLATE_HEAVY || id == BlockID.PRESSURE_PLATE_LIGHT)
                return true;
        if (CraftBookPlugin.inst().getConfiguration().minecartMoreRailsLadder)
            if (id == BlockID.LADDER || id == BlockID.VINE)
                return true;

        for (int trackBlock : trackBlocks) {
            if (id == trackBlock) return true;
        }
        return false;
    }
}