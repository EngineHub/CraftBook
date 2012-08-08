package com.sk89q.craftbook;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;

import java.util.ArrayList;

public class RailUtil {

    public static ArrayList<Chest> getNearbyChests(Block body) {
	int x = body.getX();
	int y = body.getY();
	int z = body.getZ();
	ArrayList<Chest> containers = new ArrayList<Chest>();
	if (body.getWorld().getBlockAt(x, y, z).getType() == Material.CHEST)
	    containers.add((Chest) body.getWorld().getBlockAt(x, y, z).getState());
	if (body.getWorld().getBlockAt(x - 1, y, z).getType() == Material.CHEST) {
	    containers.add((Chest) body.getWorld().getBlockAt(x - 1, y, z).getState());
	    if (body.getWorld().getBlockAt(x - 2, y, z).getType() == Material.CHEST)
		containers.add((Chest) body.getWorld().getBlockAt(x - 2, y, z).getState());
	}
	if (body.getWorld().getBlockAt(x + 1, y, z).getType() == Material.CHEST) {
	    containers.add((Chest) body.getWorld().getBlockAt(x + 1, y, z).getState());
	    if (body.getWorld().getBlockAt(x + 2, y, z).getType() == Material.CHEST)
		containers.add((Chest) body.getWorld().getBlockAt(x + 2, y, z).getState());
	}
	if (body.getWorld().getBlockAt(x, y, z - 1).getType() == Material.CHEST) {
	    containers.add((Chest) body.getWorld().getBlockAt(x, y, z - 1).getState());
	    if (body.getWorld().getBlockAt(x, y, z - 2).getType() == Material.CHEST)
		containers.add((Chest) body.getWorld().getBlockAt(x, y, z - 2).getState());
	}
	if (body.getWorld().getBlockAt(x, y, z + 1).getType() == Material.CHEST) {
	    containers.add((Chest) body.getWorld().getBlockAt(x, y, z + 1).getState());
	    if (body.getWorld().getBlockAt(x, y, z + 2).getType() == Material.CHEST)
		containers.add((Chest) body.getWorld().getBlockAt(x, y, z + 2).getState());
	}

	return containers;
    }
}
