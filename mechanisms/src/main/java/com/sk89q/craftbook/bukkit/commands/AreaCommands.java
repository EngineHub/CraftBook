package com.sk89q.craftbook.bukkit.commands;

import java.io.IOException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.mech.area.CopyManager;
import com.sk89q.craftbook.mech.area.CuboidCopy;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

/**
 * @author Silthus
 */
public class AreaCommands {

    private final MechanismsPlugin plugin;
    public AreaCommands(MechanismsPlugin plugin) {
	this.plugin = plugin;
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

	if (!(sender instanceof Player)) return;
	LocalPlayer player = plugin.wrap((Player) sender);

	if (!player.hasPermission("craftbook.mech.savearea")) {
	    player.printError("You don't have permissions to use this command!");
	    return;
	}
	String id;
	String namespace = "~" + player.getName();

	id = context.getString(0);

	if (!CopyManager.isValidName(id)) {
	    player.printError("Invalid area name.");
	    return;
	}

	try {
	    WorldEditPlugin worldEdit = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin
		    ("WorldEdit");

	    Selection sel = worldEdit.getSelection((Player) sender);
	    Vector min = BukkitUtil.toVector(sel.getMinimumPoint());
	    Vector max = BukkitUtil.toVector(sel.getMaximumPoint());
	    Vector size = max.subtract(min).add(1, 1, 1);

	    // Check maximum size
	    if (size.getBlockX() * size.getBlockY() * size.getBlockZ() > plugin.getLocalConfiguration()
		    .areaSettings.maxSizePerArea) {
		player.printError("Area is larger than allowed "
			+ plugin.getLocalConfiguration().areaSettings.maxSizePerArea + " blocks.");
		return;
	    }

	    // Check to make sure that a user doesn't have too many toggle
	    // areas (to prevent flooding the server with files)
	    if (plugin.getLocalConfiguration().areaSettings.maxAreasPerUser >= 0 && !namespace.equals("global")) {
		int count = plugin.copyManager.meetsQuota(((Player) sender).getWorld(),
			namespace, id, plugin.getLocalConfiguration().areaSettings.maxAreasPerUser, plugin);

		if (count > -1) {
		    player.printError("You are limited to "
			    + plugin.getLocalConfiguration().areaSettings.maxAreasPerUser + " toggle area(s). You" +
			    " have "
			    + count + " areas.");
		    return;
		}
	    }

	    // Copy
	    CuboidCopy copy = new CuboidCopy(min, size);
	    copy.copy(((Player) sender).getWorld());

	    plugin.getServer().getLogger().info(player.getName() + " saving toggle area with folder '"
		    + namespace + "' and ID '" + id + "'.");

	    // Save
	    try {
		plugin.copyManager.save(((Player) sender).getWorld(), namespace, id, copy, plugin);
		player.print("Area saved as '" + id + "' under the specified namespace.");
	    } catch (IOException e) {
		player.printError("Could not save area: " + e.getMessage());
	    }
	} catch (NoClassDefFoundError e) {
	    player.printError("WorldEdit.jar does not exist in plugins/.");
	}
    }
}
