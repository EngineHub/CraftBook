package com.sk89q.craftbook.bukkit.commands;

import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.mech.area.CopyManager;
import com.sk89q.craftbook.mech.area.CuboidCopy;
import com.sk89q.craftbook.mech.area.FlatCuboidCopy;
import com.sk89q.craftbook.mech.area.MCEditCuboidCopy;
import com.sk89q.craftbook.util.ArrayUtil;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.data.DataException;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
	    usage = "<id>",
        flags = "n:",
	    min = 1
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
        if (context.hasFlag('n')) {
            namespace = context.getFlag('n');
        }

	id = context.getString(0);

	if (!CopyManager.isValidName(id)) {
	    player.printError("Invalid area name. Needs to be between 1 and 13 letters long.");
	    return;
	}

	try {
	    WorldEditPlugin worldEdit = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin
		    ("WorldEdit");

	    World world = ((Player) sender).getWorld();
	    Selection sel = worldEdit.getSelection((Player) sender);
	    Vector min = BukkitUtil.toVector(sel.getMinimumPoint());
	    Vector max = BukkitUtil.toVector(sel.getMaximumPoint());
	    Vector size = max.subtract(min).add(1, 1, 1);

	    // Check maximum size
	    if (plugin.getLocalConfiguration().areaSettings.maxSizePerArea != -1 &&
		    size.getBlockX() * size.getBlockY() * size.getBlockZ() > plugin.getLocalConfiguration()
		    .areaSettings.maxSizePerArea) {
		player.printError("Area is larger than allowed "
			+ plugin.getLocalConfiguration().areaSettings.maxSizePerArea + " blocks.");
		return;
	    }

	    // Check to make sure that a user doesn't have too many toggle
	    // areas (to prevent flooding the server with files)
	    if (plugin.getLocalConfiguration().areaSettings.maxAreasPerUser >= 0 && !namespace.equals("global")) {
		int count = plugin.copyManager.meetsQuota(world,
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
	    CuboidCopy copy;
	    if (plugin.getLocalConfiguration().areaSettings.useSchematics) {
		copy = new MCEditCuboidCopy(min, size, world);
	    } else {
		copy = new FlatCuboidCopy(min, size, world);
	    }
	    copy.copy();

	    plugin.getServer().getLogger().info(player.getName() + " saving toggle area with folder '"
		    + namespace + "' and ID '" + id + "'.");

	    // Save
	    try {
		CopyManager.INSTANCE.save(world, namespace, id, copy, plugin);
		player.print("Area saved as '" + id + "' under the " + namespace + " namespace.");
	    } catch (IOException e) {
		player.printError("Could not save area: " + e.getMessage());
	    } catch (DataException e) {
		player.print(e.getMessage());
	    }
	} catch (NoClassDefFoundError e) {
	    player.printError("WorldEdit.jar does not exist in plugins/.");
	}
    }

    @Command(
            aliases = {"list"},
            desc = "Lists the areas of the given namespace or lists all areas.",
            usage = "[page]",
            flags = "an:"
    )
    @CommandPermissions({"craftbook.mech.area.list"})
    public void list(CommandContext context, CommandSender sender) throws CommandException {

        if (!(sender instanceof Player)) return;
        LocalPlayer player = plugin.wrap((Player) sender);

        String namespace = "~" + player.getName();
        // get the namespace from the flag (if set)
        if (context.hasFlag('n')) {
            if (!player.hasPermission("craftbook.mech.area.list.other")) {
                throw new CommandException("You dont have permission to list other players areas.");
            }
            namespace = context.getFlag('n');
        }
        // list all areas
        if (context.hasFlag('a')) {
            if (!player.hasPermission("craftbook.mech.area.list.all")) {
                throw new CommandException("You dont have permission to list all areas.");
            }
            namespace = null;
        }

        int page = 1;
        try {
            page = context.getInteger(0);
        } catch (Exception e) {
            // use default page: 1
        }

        // get the areas for the defined namespace
        File areas = new File(plugin.getDataFolder(), "areas");
        if (namespace != null && !namespace.equals("")) {
            areas = new File(areas, namespace);
        }
        if (!areas.exists()) {
            throw new CommandException("The namespace " + namespace + " does not exist.");
        }
        ArrayList<String> areaList = new ArrayList<String>();
        // collect the areas from the subfolders
        String currentNamespace;
        for (File file : areas.listFiles()) {
            if (file.isDirectory()) {
                currentNamespace = file.getName();
                for (File area : file.listFiles()) {
                    String strArea = area.getName().replace(".cbcopy", "");
                    strArea = strArea.replace(".schematic", "");
                    areaList.add(ChatColor.AQUA + currentNamespace + ":" + ChatColor.YELLOW + strArea);
                }
            } else if (file.isFile()) {
                String strArea = file.getName().replace(".cbcopy", "");
                strArea = strArea.replace(".schematic", "");
                areaList.add(ChatColor.YELLOW + strArea);
            }
        }

        // now lets list the areas with a nice pagination
        if (areaList.size() > 0) {
            String tmp = namespace == null || namespace.equals("") ? "All Areas " : "Areas for " + namespace;
            player.print(ChatColor.GREEN + tmp + " - Page " + Math.abs(page) + " von " + ((areaList.size() / 8) + 1));
            // list the areas one by one
            for (String str : ArrayUtil.getArrayPage(areaList, page)) {
                if (str != null && !str.equals("")) {
                    player.print(str);
                }
            }
        } else {
            player.printError("There are no saved areas" +
                    (namespace == null ? "." :  " in the " +  namespace + " namespace."));
        }
    }

    @Command(
            aliases = {"delete"},
            desc = "Lists the areas of the given namespace or lists all areas.",
            usage = "[area]",
            flags = "an:"
    )
    @CommandPermissions({"craftbook.mech.area.delete"})
    public void delete(CommandContext context, CommandSender sender) throws CommandException {

        if (!(sender instanceof Player)) return;
        LocalPlayer player = plugin.wrap((Player) sender);

        String namespace = "~" + player.getName();
        String areaId = null;
        if (context.argsLength() > 0 && !context.hasFlag('a')) {
            areaId = context.getString(0);
        } else if (!context.hasFlag('a')) {
            throw new CommandException("You need to define an area or -a to delete all areas.");
        }
        boolean deleteAll = false;

        // add the area suffix
        areaId = areaId + "." + (plugin.getLocalConfiguration().areaSettings.useSchematics ? "schematic" : "cbcopy");

        // get the namespace from the flag (if set)
        if (context.hasFlag('n')) {
            if (!player.hasPermission("craftbook.mech.area.delete.other")) {
                throw new CommandException("You dont have permission to delete other players areas.");
            }
            namespace = context.getFlag('n');
        }
        // should we delete all areas of the given namespace?
        if (context.hasFlag('a')) {
            if (!player.hasPermission("craftbook.mech.area.delete.all")) {
                throw new CommandException("You dont have permission to delete all areas of that namespace.");
            }
            deleteAll = true;
        }

        File areas = null;
        try {
            areas = new File(plugin.getDataFolder(), "areas/" + namespace);
        } catch (Exception e) {
        }
        if (areas == null || !areas.exists()) {
            throw new CommandException("The namespace " + namespace + " does not exist.");
        }

        if (deleteAll) {
            areas.delete();
            player.print("All areas in the namespace " + namespace + " have been deleted.");
        } else {
            File file = new File(areas, areaId);
            file.delete();
            player.print("The area " +
                    ChatColor.AQUA + namespace + ":" + ChatColor.YELLOW + areaId +
                    ChatColor.GOLD + " has been deleted.");
        }
    }
}
