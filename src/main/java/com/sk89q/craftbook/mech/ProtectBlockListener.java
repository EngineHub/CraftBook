package com.sk89q.craftbook.mech;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;

public class ProtectBlockListener implements Listener {

	protected static final Map<String, CuboidRegion> regions = new HashMap<String, CuboidRegion>();
	protected static final Map<String, WorldVector> blocks = new HashMap<String, WorldVector>();
	private static ProtectBlockListener singleton = null;

	/**
	 * Construct the listener.
	 */
	public ProtectBlockListener() {
		CraftBookPlugin.registerEvents(this);
	}

	/**
	 * Make sure there is an instance of the listener.
	 */
	public static void initialize() {
		if (singleton == null) {
			singleton = new ProtectBlockListener();
		}
	}

	/**
	 * Protect a cuboid region.
	 * 
	 * @param region
	 *        the region
	 */
	public static void addCuboidRegion(CuboidRegion region) {
		if (!regions.containsKey(region.toString()))
			System.out.println("ProtectBlockListener: add: " + region);

		regions.put(region.toString(), region);
	}

	/**
	 * Don't protect a cuboid region any more.
	 * 
	 * @param region
	 *        the region
	 */
	public static void removeCuboidRegion(CuboidRegion region) {
		System.out.println("ProtectBlockListener: remove: " + region);
		regions.remove(region.toString());
	}

	/**
	 * Protect a block.
	 * 
	 * @param vector
	 *        the vector
	 */
	public static void addBlock(WorldVector vector) {
		if (!blocks.containsKey(vector.toString()))
			System.out.println("ProtectBlockListener: add: " + vector);
		blocks.put(vector.toString(), vector);
	}

	/**
	 * Don't protect a block any more.
	 * 
	 * @param vector
	 *        the vector
	 */
	public static void removeBlock(WorldVector vector) {
		System.out.println("ProtectBlockListener: remove: " + vector);
		blocks.remove(vector.toString());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (isProtected(event.getBlock())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage("Protected area. Cannot place blocks here.");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (isProtected(event.getBlock())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage("Protected area. Cannot break blocks here.");
		}
	}

	/**
	 * Check whether a block is protected.
	 * 
	 * @param b
	 *        the block
	 * @return {@code true} if it is protected, {@code false} otherwise.
	 */
	private boolean isProtected(Block b) {
		if (blocks.containsKey(BukkitUtil.toWorldVector(b).toString())) {
			return true;
		}
		for (CuboidRegion region : regions.values()) {
			if (region.contains(BukkitUtil.toVector(b))) {
				return true;
			}
		}
		return false;
	}
}
