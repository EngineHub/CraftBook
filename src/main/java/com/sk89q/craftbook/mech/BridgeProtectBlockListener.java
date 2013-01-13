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

public class BridgeProtectBlockListener implements Listener {

        protected static final Set<CuboidRegion> regions = new HashSet<CuboidRegion>();
	private static BridgeProtectBlockListener singleton = null;

        public static void addCuboidRegion(CuboidRegion region) {
		regions.add(region);
		if (singleton == null) {
			singleton = new BridgeProtectBlockListener();
		}
        }

        public static void removeCuboidRegion(CuboidRegion region) {
		regions.remove(region);
        }

        /**
         * Construct the listener.
         */
        public BridgeProtectBlockListener() {
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
		for (CuboidRegion region : regions) {
			if (region.contains(BukkitUtil.toVector(b))) {
				event.setCancelled(true);
				return true;
			}
		}
		return false;
        }
}

