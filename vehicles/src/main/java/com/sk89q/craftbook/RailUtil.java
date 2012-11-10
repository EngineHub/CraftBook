package com.sk89q.craftbook;

import com.sk89q.worldedit.blocks.BlockID;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;

import java.util.ArrayList;

public class RailUtil {

    public static ArrayList<Chest> getNearbyChests(Block body) {

        int x = body.getX();
        int y = body.getY();
        int z = body.getZ();
        ArrayList<Chest> containers = new ArrayList<Chest>();
        if (body.getWorld().getBlockAt(x, y, z).getTypeId() == BlockID.CHEST) {
            containers.add((Chest) body.getWorld().getBlockAt(x, y, z).getState());
        }
        if (body.getWorld().getBlockAt(x - 1, y, z).getTypeId() == BlockID.CHEST) {
            containers.add((Chest) body.getWorld().getBlockAt(x - 1, y, z).getState());
            if (body.getWorld().getBlockAt(x - 2, y, z).getTypeId() == BlockID.CHEST) {
                containers.add((Chest) body.getWorld().getBlockAt(x - 2, y, z).getState());
            }
        }
        if (body.getWorld().getBlockAt(x + 1, y, z).getTypeId() == BlockID.CHEST) {
            containers.add((Chest) body.getWorld().getBlockAt(x + 1, y, z).getState());
            if (body.getWorld().getBlockAt(x + 2, y, z).getTypeId() == BlockID.CHEST) {
                containers.add((Chest) body.getWorld().getBlockAt(x + 2, y, z).getState());
            }
        }
        if (body.getWorld().getBlockAt(x, y, z - 1).getTypeId() == BlockID.CHEST) {
            containers.add((Chest) body.getWorld().getBlockAt(x, y, z - 1).getState());
            if (body.getWorld().getBlockAt(x, y, z - 2).getTypeId() == BlockID.CHEST) {
                containers.add((Chest) body.getWorld().getBlockAt(x, y, z - 2).getState());
            }
        }
        if (body.getWorld().getBlockAt(x, y, z + 1).getTypeId() == BlockID.CHEST) {
            containers.add((Chest) body.getWorld().getBlockAt(x, y, z + 1).getState());
            if (body.getWorld().getBlockAt(x, y, z + 2).getTypeId() == BlockID.CHEST) {
                containers.add((Chest) body.getWorld().getBlockAt(x, y, z + 2).getState());
            }
        }

        return containers;
    }

    private static final int[] trackBlocks = new int[] {
            BlockID.MINECART_TRACKS, BlockID.POWERED_RAIL, BlockID.DETECTOR_RAIL
    };

    public static boolean isTrack(int id) {

        for (int trackBlock : trackBlocks) {
            if (id == trackBlock) return true;
        }
        return false;
    }
}
