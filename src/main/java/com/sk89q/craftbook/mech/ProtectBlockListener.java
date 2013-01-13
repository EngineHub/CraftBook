package com.sk89q.craftbook.mech;

import com.sk89q.craftbook.MechanicManager;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.regions.*;
import com.sk89q.worldedit.blocks.*;
import com.sk89q.worldedit.bukkit.*;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.material.Diode;

import java.util.*;

public class ProtectBlockListener implements Listener {

        protected static final Map<String, CuboidRegion> regions = new HashMap<String, CuboidRegion>();
        protected static final Map<String, WorldVector> blocks = new HashMap<String, WorldVector>();
	private static ProtectBlockListener singleton = null;

        public static void addCuboidRegion(CuboidRegion region) {
		if (!regions.containsKey(region.toString())
			System.out.println("ProtectBlockListener: add: " + region);

		regions.put(region.toString(), region);
		if (singleton == null) {
			singleton = new ProtectBlockListener();
		}
        }

        public static void removeCuboidRegion(CuboidRegion region) {
		System.out.println("ProtectBlockListener: remove: " + region);
		regions.remove(region.toString());
        }

	public static void addBlock(WorldVector vector) {
		if (!blocks.containsKey(vector.toString())
			System.out.println("ProtectBlockListener: add: " + vector);
		blocks.put(vector.toString(), vector);
	}

	public static void removeBlock(WorldVector vector) {
		System.out.println("ProtectBlockListener: remove: " + vector);
		blocks.remove(vector.toString());
	}

        /**
         * Construct the listener.
         */
        public ProtectBlockListener() {
		CraftBookPlugin.registerEvents(this);
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onBlockPlace(BlockPlaceEvent event) {
		if (isProtected(event.getBlock(), event)) {
			event.getPlayer().sendMessage("Protected area. Cannot place blocks here.");
		}
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onBlockBreak(BlockBreakEvent event) {
		if (isProtected(event.getBlock(), event)) {
			event.getPlayer().sendMessage("Protected area. Cannot break blocks here.");
		}
	}

	public boolean isProtected(Block b, Cancellable event) {
		if (blocks.containsKey(BukkitUtil.toWorldVector(b).toString())) {
			event.setCancelled(true);
			return true;
		}
		for (CuboidRegion region : regions.values()) {
			if (region.contains(BukkitUtil.toVector(b))) {
				event.setCancelled(true);
				return true;
			}
		}
		return false;
        }
}

