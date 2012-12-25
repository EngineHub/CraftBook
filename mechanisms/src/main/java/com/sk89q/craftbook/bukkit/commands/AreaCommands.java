package com.sk89q.craftbook.bukkit.commands;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.MechanismsConfiguration;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.mech.area.CopyManager;
import com.sk89q.craftbook.mech.area.CuboidCopy;
import com.sk89q.craftbook.mech.area.FlatCuboidCopy;
import com.sk89q.craftbook.mech.area.MCEditCuboidCopy;
import com.sk89q.craftbook.util.ArrayUtil;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.data.DataException;

/**
 * @author Silthus
 */
public class AreaCommands {

    private final MechanismsPlugin plugin;
    private CopyManager copyManager;

    public AreaCommands (MechanismsPlugin plugin) {

        this.plugin = plugin;
        copyManager = plugin.getCopyManager();
    }

    @Command(aliases = { "save" }, desc = "Saves the selected area", usage = "[-n namespace ] <id>", flags = "n:", min = 1)
    public void saveArea (CommandContext context, CommandSender sender) throws CommandException {

        final MechanismsConfiguration.AreaSettings config = plugin.getLocalConfiguration().areaSettings;

        if (!(sender instanceof Player)) return;
        LocalPlayer player = plugin.wrap((Player) sender);

        String id;
        String namespace = player.getName();
        boolean personal = true;

        if (context.hasFlag('n') && player.hasPermission("craftbook.mech.area.save." + context.getFlag('n'))) {
            namespace = context.getFlag('n');
            personal = false;
        } else if (!player.hasPermission("craftbook.mech.area.save.self")) throw new CommandPermissionsException();

        if (!CopyManager.isValidNamespace(namespace)) throw new CommandException("Invalid namespace. Needs to be between 1 and 14 letters long.");

        if (personal) {
            namespace = "~" + namespace;
        }

        id = context.getString(0);

        if (!CopyManager.isValidName(id)) throw new CommandException("Invalid area name. Needs to be between 1 and 13 letters long.");

        try {
            WorldEditPlugin worldEdit = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");

            World world = ((Player) sender).getWorld();
            Selection sel = worldEdit.getSelection((Player) sender);
            Vector min = BukkitUtil.toVector(sel.getMinimumPoint());
            Vector max = BukkitUtil.toVector(sel.getMaximumPoint());
            Vector size = max.subtract(min).add(1, 1, 1);

            // Check maximum size
            if (config.maxSizePerArea != -1 && size.getBlockX() * size.getBlockY() * size.getBlockZ() > config.maxSizePerArea)
                throw new CommandException("Area is larger than allowed " + config.maxSizePerArea + " blocks.");

            // Check to make sure that a user doesn't have too many toggle
            // areas (to prevent flooding the server with files)
            if (config.maxAreasPerUser >= 0 && !namespace.equals("global")) {
                int count = copyManager.meetsQuota(world, namespace, id, config.maxAreasPerUser, plugin);

                if (count > -1)
                    throw new CommandException("You are limited to " + config.maxAreasPerUser + " toggle area(s). You have " + count + " areas.");
            }

            // Copy
            CuboidCopy copy;

            if (config.useSchematics) {
                copy = new MCEditCuboidCopy(min, size, world);
            } else {
                copy = new FlatCuboidCopy(min, size, world);
            }

            copy.copy();

            plugin.getServer().getLogger().info(player.getName() + " saving toggle area with folder '" + namespace + "' and ID '" + id + "'.");

            // Save
            try {
                CopyManager.getInstance().save(world, namespace, id, copy, plugin);
                player.print("Area saved as '" + id + "' under the '" + namespace + "' namespace.");
            } catch (IOException e) {
                player.printError("Could not save area: " + e.getMessage());
            } catch (DataException e) {
                player.print(e.getMessage());
            }
        } catch (NoClassDefFoundError e) {
            throw new CommandException("WorldEdit.jar does not exist in plugins/.");
        }
    }

    @Command(aliases = { "list" }, desc = "Lists the areas of the given namespace or lists all areas.", usage = "[-n namespace] [page #]",
            flags = "an:")
    public void list (CommandContext context, CommandSender sender) throws CommandException {

        final MechanismsConfiguration.AreaSettings config = plugin.getLocalConfiguration().areaSettings;

        if (!(sender instanceof Player)) return;
        LocalPlayer player = plugin.wrap((Player) sender);

        String namespace = "~" + player.getName();

        // get the namespace from the flag (if set)
        if (context.hasFlag('n') && player.hasPermission("craftbook.mech.area.list." + context.getFlag('n'))) {
            namespace = context.getFlag('n');
        } else if (context.hasFlag('a') && player.hasPermission("craftbook.mech.area.list.all")) {
            namespace = "";
        } else if (!player.hasPermission("craftbook.mech.area.list.self")) throw new CommandPermissionsException();

        int page = 1;
        try {
            page = context.getInteger(0);
        } catch (Exception ignored) {
            // use default page: 1
        }

        // get the areas for the defined namespace
        File areas = new File(plugin.getDataFolder(), "areas");

        if (!areas.exists()) throw new CommandException("There are no saved areas.");

        File folder = null;
        if (!namespace.isEmpty()) {
            folder = new File(areas, namespace);
        }

        if (folder != null && !folder.exists()) throw new CommandException("The namespace '" + namespace + "' does not exist.");

        List<String> areaList = new ArrayList<String>();

        FilenameFilter fnf = new FilenameFilter() {

            @Override
            public boolean accept (File dir, String name) {

                return config.useSchematics ? name.endsWith(".schematic") : name.endsWith(".cbcopy");
            }
        };

        if (folder != null && folder.exists()) {
            for (File area : folder.listFiles(fnf)) {
                String areaName = area.getName();
                areaName = areaName.replace(".schematic", "");
                areaName = areaName.replace(".cbcopy", "");
                areaList.add(ChatColor.AQUA + folder.getName() + "   :   " + ChatColor.YELLOW + areaName);
            }
        } else {
            for (File file : areas.listFiles())
                if (file.isDirectory()) {
                    for (File area : file.listFiles(fnf)) {
                        String areaName = area.getName();
                        areaName = areaName.replace(".schematic", "");
                        areaName = areaName.replace(".cbcopy", "");
                        areaList.add(ChatColor.AQUA + folder.getName() + "   :   " + ChatColor.YELLOW + areaName);
                    }
                }
        }

        // now lets list the areas with a nice pagination
        if (!areaList.isEmpty()) {
            String tmp = namespace.isEmpty() ? "All Areas " : "Areas for " + namespace;
            player.print(ChatColor.GREEN + tmp + " - Page " + Math.abs(page) + " of " + (areaList.size() / 8 + 1));
            // list the areas one by one
            for (String str : ArrayUtil.getArrayPage(areaList, page))
                if (str != null && !str.isEmpty()) {
                    player.print(str);
                }
        } else {
            player.printError("There are no saved areas in the '" + namespace + "' namespace.");
        }
    }

    @Command(aliases = { "delete" }, desc = "Lists the areas of the given namespace or lists all areas.", usage = "[-n namespace] [area]",
            flags = "an:")
    public void delete (CommandContext context, CommandSender sender) throws CommandException {

        final MechanismsConfiguration.AreaSettings config = plugin.getLocalConfiguration().areaSettings;

        if (!(sender instanceof Player)) return;
        LocalPlayer player = plugin.wrap((Player) sender);

        String namespace = "~" + player.getName();
        String areaId = null;

        // Get the namespace
        if (context.hasFlag('n') && player.hasPermission("craftbook.mech.area.delete." + context.getFlag('n'))) {
            namespace = context.getFlag('n');
        } else if (!player.hasPermission("craftbook.mech.area.delete.self")) throw new CommandPermissionsException();

        boolean deleteAll = false;
        if (context.argsLength() > 0 && !context.hasFlag('a')) {
            areaId = context.getString(0);
        } else if (context.hasFlag('a') && player.hasPermission("craftbook.mech.area.delete." + namespace + ".all")) {
            deleteAll = true;
        } else throw new CommandException("You need to define an area or -a to delete all areas.");

        // add the area suffix
        areaId = areaId + (config.useSchematics ? ".schematic" : ".cbcopy");

        File areas = null;
        try {
            areas = new File(plugin.getDataFolder(), "areas/" + namespace);
        } catch (Exception ignored) {
        }

        if (areas == null || !areas.exists()) throw new CommandException("The namespace " + namespace + " does not exist.");

        if (deleteAll) {
            if (deleteDir(areas)) {
                player.print("All areas in the namespace " + namespace + " have been deleted.");
            }
        } else {
            File file = new File(areas, areaId);
            if (file.delete()) {
                player.print("The area '" + areaId + " in the namespace '" + namespace + "' has been deleted.");
            }
        }
    }

    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    private boolean deleteDir (File dir) {

        final MechanismsConfiguration.AreaSettings config = plugin.getLocalConfiguration().areaSettings;

        FilenameFilter fnf = new FilenameFilter() {

            @Override
            public boolean accept (File dir, String name) {

                return config.useSchematics ? name.endsWith(".schematic") : name.endsWith(".cbcopy");
            }
        };

        if (dir.isDirectory()) {
            for (File aChild : dir.listFiles(fnf))
                if (!aChild.delete()) return false;
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
}
