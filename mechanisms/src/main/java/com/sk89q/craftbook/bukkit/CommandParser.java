package com.sk89q.craftbook.bukkit;

import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.mech.area.CopyManager;
import com.sk89q.craftbook.mech.area.CuboidCopy;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class CommandParser implements CommandExecutor{

    MechanismsPlugin plugin;

    public CommandParser(MechanismsPlugin plugin) {
	this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	if(command.getName().equalsIgnoreCase("cbmech") && sender.hasPermission("craftbook.mech.cbmech")) {
	    if(args.length>0)
		if(args[0].equalsIgnoreCase("reload")) plugin.reloadLocalConfiguration();
	    return true;
	}
	if(command.getName().equalsIgnoreCase("savearea") && sender.hasPermission("craftbook.mech.savearea")) {

	    if(!(sender instanceof Player)) return false;
	    Player player = (Player)sender;

	    String id;
	    String namespace = player.getName();

	    id = args[0];

	    if (!CopyManager.isValidNamespace(namespace)) {
		player.sendMessage(ChatColor.RED + "Invalid namespace name. For the global namespace, use @");
		return true;
	    }
	    namespace = "~" + namespace;

	    if (!CopyManager.isValidName(id)) {
		player.sendMessage(ChatColor.RED + "Invalid area name.");
		return true;
	    }

	    try {
		WorldEditPlugin worldEdit = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");

		Selection sel = worldEdit.getSelection(player);
		Vector min = BukkitUtil.toVector(sel.getMinimumPoint());
		Vector max = BukkitUtil.toVector(sel.getMaximumPoint());
		Vector size = max.subtract(min).add(1, 1, 1);

		// Check maximum size
		if (size.getBlockX() * size.getBlockY() * size.getBlockZ() > plugin.getLocalConfiguration().areaSettings.maxSizePerArea) {
		    player.sendMessage(ChatColor.RED + "Area is larger than allowed "
			    + plugin.getLocalConfiguration().areaSettings.maxSizePerArea + " blocks.");
		    return true;
		}

		// Check to make sure that a user doesn't have too many toggle
		// areas (to prevent flooding the server with files)
		if (plugin.getLocalConfiguration().areaSettings.maxAreasPerUser >= 0 && !namespace.equals("global")) {
		    int count = plugin.copyManager.meetsQuota(player.getWorld(),
			    namespace, id, plugin.getLocalConfiguration().areaSettings.maxAreasPerUser, plugin);

		    if (count > -1) {
			player.sendMessage(ChatColor.RED + "You are limited to "
				+ plugin.getLocalConfiguration().areaSettings.maxAreasPerUser + " toggle area(s). You have "
				+ count + " areas.");
			return true;
		    }
		}

		// Copy
		CuboidCopy copy = new CuboidCopy(min, size);
		copy.copy(player.getWorld());

		plugin.getServer().getLogger().info(player.getName() + " saving toggle area with folder '"
			+ namespace + "' and ID '" + id + "'.");

		// Save
		try {
		    plugin.copyManager.save(player.getWorld(), namespace, id, copy, plugin);
		    player.sendMessage(ChatColor.GOLD + "Area saved as '" + id + "' under the specified namespace.");
		} catch (IOException e) {
		    player.sendMessage(ChatColor.RED + "Could not save area: " + e.getMessage());
		}
	    } catch (NoClassDefFoundError e) {
		player.sendMessage(ChatColor.RED + "WorldEdit.jar does not exist in plugins/.");
	    }
	    return true;
	}
	else if(command.getName().equalsIgnoreCase("savensarea") && sender.hasPermission("craftbook.mech.savensarea")) {

	    if(!(sender instanceof Player)) return false;
	    Player player = (Player)sender;

	    String id;
	    String namespace;

	    id = args[1];
	    namespace = args[0];

	    if (namespace.equalsIgnoreCase("@")) {
		namespace = "global";
	    } else {
		if (!CopyManager.isValidNamespace(namespace)) {
		    player.sendMessage(ChatColor.RED + "Invalid namespace name. For the global namespace, use @");
		    return true;
		}
		namespace = "~" + namespace;
	    }

	    if (!CopyManager.isValidName(id)) {
		player.sendMessage(ChatColor.RED + "Invalid area name.");
		return true;
	    }

	    try {
		WorldEditPlugin worldEdit = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");

		Selection sel = worldEdit.getSelection(player);
		Vector min = BukkitUtil.toVector(sel.getMinimumPoint());
		Vector max = BukkitUtil.toVector(sel.getMaximumPoint());
		Vector size = max.subtract(min).add(1, 1, 1);

		// Check maximum size
		if (size.getBlockX() * size.getBlockY() * size.getBlockZ() > plugin.getLocalConfiguration().areaSettings.maxSizePerArea) {
		    player.sendMessage(ChatColor.RED + "Area is larger than allowed "
			    + plugin.getLocalConfiguration().areaSettings.maxSizePerArea + " blocks.");
		    return true;
		}

		// Check to make sure that a user doesn't have too many toggle
		// areas (to prevent flooding the server with files)
		if (plugin.getLocalConfiguration().areaSettings.maxAreasPerUser >= 0 && !namespace.equals("global")) {
		    int count = plugin.copyManager.meetsQuota(player.getWorld(),
			    namespace, id, plugin.getLocalConfiguration().areaSettings.maxAreasPerUser, plugin);

		    if (count > -1) {
			player.sendMessage(ChatColor.RED + "You are limited to "
				+ plugin.getLocalConfiguration().areaSettings.maxAreasPerUser + " toggle area(s). You have "
				+ count + " areas.");
			return true;
		    }
		}

		// Copy
		CuboidCopy copy = new CuboidCopy(min, size);
		copy.copy(player.getWorld());

		plugin.getServer().getLogger().info(player.getName() + " saving toggle area with folder '"
			+ namespace + "' and ID '" + id + "'.");

		// Save
		try {
		    plugin.copyManager.save(player.getWorld(), namespace, id, copy, plugin);
		    player.sendMessage(ChatColor.GOLD + "Area saved as '" + id + "' under the specified namespace.");
		} catch (IOException e) {
		    player.sendMessage(ChatColor.RED + "Could not save area: " + e.getMessage());
		}
	    } catch (NoClassDefFoundError e) {
		player.sendMessage(ChatColor.RED + "WorldEdit.jar does not exist in plugins/.");
	    }
	    return true;
	}
	return false;
    }
}
