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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.material.Diode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BridgeProtectBlockListener implements Listener {

        protected static final List<CuboidRegion> regions = new ArrayList<CuboidRegion>();
	private static BridgeProtectBlockListener singleton = null;

        public static void addCuboidRegion(CuboidRegion region) {
		regions.add(region);
		if (singleton == null) {
			singleton = new BridgeProtectBlockListener();
		}
        }

        /**
         * Construct the listener.
         */
        public BridgeProtectBlockListener() {
		CraftBookPlugin.registerEvents(this);
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onBlockPlace(BlockPlaceEvent event) {
		checkProtected(event.getBlock());
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onBlockBreak(BlockBreakEvent event) {
		checkProtected(event.getBlock());
	}

	public void checkProtected(Block b) {
		for (CuboidRegion region : regions) {
			if (region.contains(BukkitUtil.toVector(b))) {
				event.setCancelled(true);
				event.getPlayer().sendMessage("Protected area. Cannot build here.");
				break;
			}
		}
        }
}
