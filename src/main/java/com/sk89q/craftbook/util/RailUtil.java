package com.sk89q.craftbook.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;

import com.sk89q.craftbook.mechanics.minecart.MoreRails;

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

    private static final Material[] trackBlocks = new Material[] { Material.RAILS, Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.ACTIVATOR_RAIL};

    public static boolean isTrack(Material id) {

        if (MoreRails.instance != null && MoreRails.instance.pressurePlate)
            if (id == Material.STONE_PLATE || id == Material.WOOD_PLATE || id == Material.IRON_PLATE || id == Material.GOLD_PLATE)
                return true;
        if (MoreRails.instance != null && MoreRails.instance.ladder)
            if (id == Material.LADDER || id == Material.VINE)
                return true;

        for (Material trackBlock : trackBlocks) {
            if (id == trackBlock) return true;
        }
        return false;
    }
}