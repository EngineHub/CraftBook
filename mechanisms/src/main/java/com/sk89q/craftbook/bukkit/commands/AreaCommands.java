package com.sk89q.craftbook.bukkit.commands;

import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.mech.area.CuboidCopy;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.data.DataException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

/**
 * @author Silthus
 */
public class AreaCommands {

	private final MechanismsPlugin plugin;
	private final WorldEditPlugin worldEdit;

	public AreaCommands(MechanismsPlugin plugin) {
		this.plugin = plugin;
		worldEdit = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
	}

	@Command(
			aliases = {"save"},
			desc = "Saves the selected area",
			usage = "<name>",
			help = "Saves the selection as area:\n" +
					"Flags:\n" +
					"  -n <namespace> saves the to the given namespace",
			min = 1,
			flags = "n"
	)
	@CommandPermissions({"craftbook.mech.area.save"})
	public void saveArea(CommandContext context, CommandSender sender) throws CommandException {

		Player player = checkPlayer(sender);
		String id = context.getString(0);
		String namespace = "~" + sender.getName();
		// set the namespace correctly
		if (context.hasFlag('n')) {
			namespace = context.getFlag('n');
			if (namespace.equals("@") || namespace.equals("global")) {
				namespace = "global";
			} else {
				namespace = "~" + namespace;
			}
		}

		// lets grap the worldedit selection
		Selection selection = worldEdit.getSelection((Player) sender);
		if (!(selection instanceof CuboidSelection)) {
			throw new CommandException("Selection needs to be a cuboid.");
		}
		Vector min = selection.getNativeMinimumPoint();
		Vector max = selection.getNativeMaximumPoint();
		Vector size = max.subtract(min).add(1, 1, 1);

		// Check maximum size
		if (size.getBlockX() * size.getBlockY() * size.getBlockZ() > plugin.getLocalConfiguration()
				.areaSettings.maxSizePerArea) {
			throw new CommandException("Area is larger than allowed "
					+ plugin.getLocalConfiguration().areaSettings.maxSizePerArea + " blocks.");
		}

		// Check to make sure that a user doesn't have too many toggle
		// areas (to prevent flooding the server with files)
		if (plugin.getLocalConfiguration().areaSettings.maxAreasPerUser >= 0 && !namespace.equals("global")) {
			int count = plugin.copyManager.meetsQuota(((Player) sender).getWorld(),
					namespace, id, plugin.getLocalConfiguration().areaSettings.maxAreasPerUser, plugin);

			if (count > -1) {
				throw new CommandException("You are limited to "
						+ plugin.getLocalConfiguration().areaSettings.maxAreasPerUser + " toggle area(s). You" +
						" have "
						+ count + " areas.");
			}
		}
		// thats the actual saving part
		// Copy
		CuboidCopy copy = new CuboidCopy(min, size);
		copy.copy(worldEdit.createEditSession(player));

		plugin.getServer().getLogger().info(player.getName() + " saving toggle area with folder '"
				+ namespace + "' and ID '" + id + "'.");

		// Save
		try {
			plugin.copyManager.save(player.getWorld(), namespace, id, copy, plugin);
			player.sendMessage(ChatColor.GOLD + "Area saved as '" + id + "' under the specified namespace.");
		} catch (IOException e) {
			player.sendMessage(ChatColor.RED + "Could not save area: " + e.getMessage());
		} catch (DataException e) {
			player.sendMessage(ChatColor.RED + "Could not save area: " + e.getMessage());
		}
	}

	private Player checkPlayer(CommandSender sender) throws CommandException {
		if (sender instanceof Player) {
			return (Player) sender;
		}
		throw new CommandException("Needs to be executed as player.");
	}
}
