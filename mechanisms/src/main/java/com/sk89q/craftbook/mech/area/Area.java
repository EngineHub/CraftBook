package com.sk89q.craftbook.mech.area;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;

/**
 * Area.
 *
 * @author Me4502, Sk89q
 */

public class Area extends AbstractMechanic {

	public static class Factory extends AbstractMechanicFactory<Area> {

		public Factory(MechanismsPlugin plugin) {

			this.plugin = plugin;
		}

		private final MechanismsPlugin plugin;

		/**
		 * Detect the mechanic at a placed sign.
		 *
		 * @throws ProcessedMechanismException
		 */
		@Override
		public Area detect(BlockWorldVector pt, LocalPlayer player, Sign sign)
				throws InvalidMechanismException, ProcessedMechanismException {

			if (!plugin.getLocalConfiguration().areaSettings.enable)
				return null;
			if (sign.getLine(1).equalsIgnoreCase("[Area]") || sign.getLine(1).equalsIgnoreCase("[SaveArea]")) {
				if (!player.hasPermission("craftbook.mech.area")) {
					throw new InsufficientPermissionsException();
				}
				if (sign.getLine(0).trim().equalsIgnoreCase("")) sign.setLine(0, "~" + player.getName());
				if (sign.getLine(1).equalsIgnoreCase("[Area]")) sign.setLine(1, "[Area]");
				else sign.setLine(1, "[SaveArea]");
				player.print("Toggle area created.");
			} else {
				return null;
			}

			throw new ProcessedMechanismException();
		}

		/**
		 * Explore around the trigger to find a Door; throw if things look funny.
		 *
		 * @param pt the trigger (should be a signpost)
		 * @return a Area if we could make a valid one, or null if this looked
		 *         nothing like a area.
		 * @throws InvalidMechanismException if the area looked like it was intended to be a area, but
		 *                                   it failed.
		 */
		@Override
		public Area detect(BlockWorldVector pt) throws InvalidMechanismException {

			if (!plugin.getLocalConfiguration().areaSettings.enableRedstone)
				return null;

			Block block = BukkitUtil.toWorld(pt).getBlockAt(
					BukkitUtil.toLocation(pt));
			if (block.getTypeId() == BlockID.SIGN_POST || block.getTypeId() == BlockID.WALL_SIGN) {
				BlockState state = block.getState();
				if (state instanceof Sign) {
					Sign sign = (Sign) state;
					if (sign.getLine(1).equalsIgnoreCase("[Area]") || sign.getLine(1).equalsIgnoreCase("[SaveArea]")) {
						if (!sign.getLine(0).equalsIgnoreCase(""))
							sign.setLine(0, "global");
						return new Area(pt, plugin);
					}
				}
			}
			return null;
		}
	}

	public final MechanismsPlugin plugin;

	public final BlockWorldVector pt;


	/**
	 * Raised when a block is right clicked.
	 *
	 * @param event
	 */
	@Override
	public void onRightClick(PlayerInteractEvent event) {

		if (!event.getPlayer().hasPermission("craftbook.mech.area.use")) {
			event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to use areas.");
			return;
		}
		try {
			Sign s = null;
			if (BukkitUtil.toBlock(pt).getState() instanceof Sign)
				s = ((Sign) BukkitUtil.toBlock(pt).getState());
			if (s == null) return;
			boolean save = s.getLine(1).equalsIgnoreCase("[SaveArea]");
			String namespace = s.getLine(0);
			String id = s.getLine(2);
			String inactiveID = s.getLine(3);

			if (id == null || id.equalsIgnoreCase("") || id.length() < 1) return;
			if (namespace == null || namespace.equalsIgnoreCase("") || namespace.length() < 1) return;
			if (event.getPlayer().getWorld() == null) return;

			CuboidCopy copy = plugin.copyManager.load(event.getPlayer().getWorld(), namespace, id, plugin);
			if (!copy.shouldClear(event.getPlayer().getWorld())) {
				if (save)
					plugin.copyManager.save(event.getPlayer().getWorld(), namespace, inactiveID, copy, plugin);
				copy.paste(event.getPlayer().getWorld());
			} else {
				if (inactiveID.length() == 0) {
					if (save)
						plugin.copyManager.save(event.getPlayer().getWorld(), namespace, id, copy, plugin);
					copy.clear(event.getPlayer().getWorld());
				} else {
					if (save)
						plugin.copyManager.save(event.getPlayer().getWorld(), namespace, id, copy, plugin);
					copy = plugin.copyManager.load(event.getPlayer().getWorld(), namespace, inactiveID, plugin);
					copy.paste(event.getPlayer().getWorld());
				}
			}
		} catch (Exception e) {
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			plugin.getLogger().log(Level.SEVERE, "Failed to toggle Area: " + result.toString());
		}

		event.setCancelled(true);
	}

	/**
	 * Raised when an input redstone current changes.
	 *
	 * @param event
	 */
	@Override
	public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

		if (!plugin.getLocalConfiguration().areaSettings.enableRedstone)
			return;
		try {
			Sign s = null;
			if (BukkitUtil.toBlock(pt).getState() instanceof Sign)
				s = ((Sign) BukkitUtil.toBlock(pt).getState());
			if (s == null) return;
			String namespace = s.getLine(0);
			String id = s.getLine(2);

			CuboidCopy copy = plugin.copyManager.load(BukkitUtil.toWorld(pt.getWorld()), namespace, id, plugin);

			if (!copy.shouldClear(BukkitUtil.toWorld(pt.getWorld()))) {
				copy.paste(BukkitUtil.toWorld(pt.getWorld()));
			} else {
				String inactiveID = s.getLine(3);

				if (inactiveID.length() == 0) {
					copy.clear(BukkitUtil.toWorld(pt.getWorld()));
				} else {
					copy = plugin.copyManager.load(BukkitUtil.toWorld(pt.getWorld()), namespace, inactiveID, plugin);
					copy.paste(BukkitUtil.toWorld(pt.getWorld()));
				}
			}
		} catch (Exception e) {
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			plugin.getLogger().log(Level.SEVERE, "Failed to toggle Area: " + result.toString());
		}
	}

	/**
	 * @param pt     if you didn't already check if this is a signpost with appropriate
	 *               text, you're going on Santa's naughty list.
	 * @param plugin
	 * @throws InvalidMechanismException
	 */
	private Area(BlockWorldVector pt, MechanismsPlugin plugin) throws InvalidMechanismException {

		super();
		this.plugin = plugin;
		this.pt = pt;
	}

	@Override
	public void unload() {

	}

	@Override
	public boolean isActive() {

		return false;
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event) {

	}

	@Override
	public void unloadWithEvent(ChunkUnloadEvent event) {

	}
}