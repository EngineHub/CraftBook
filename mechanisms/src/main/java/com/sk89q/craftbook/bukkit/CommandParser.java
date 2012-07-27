package com.sk89q.craftbook.bukkit;

import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.mech.area.CopyManager;
import com.sk89q.craftbook.mech.area.CuboidCopy;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

/**
 * @author Me4502
 */
public class CommandParser implements CommandExecutor {

    final MechanismsPlugin plugin;

    public CommandParser(MechanismsPlugin plugin) {

        this.plugin = plugin;
    }

    //TODO Change to fancier command system
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("cbmech") && sender.hasPermission("craftbook.mech.cbmech")) {
            if (args.length > 0)
                if (args[0].equalsIgnoreCase("reload")) return plugin.reloadLocalConfiguration(sender);
            return true;
        } else if (command.getName().equalsIgnoreCase("savearea")) {

            if (!(sender instanceof Player)) return false;
            LocalPlayer player = plugin.wrap((Player) sender);

            if (!player.hasPermission("craftbook.mech.savearea")) {
                player.printError("You don't have permissions to use this command!");
                return true;
            }
            String id;
            String namespace = "~" + player.getName();

            id = args[0];

            if (!CopyManager.isValidName(id)) {
                player.printError("Invalid area name.");
                return true;
            }

            try {
                WorldEditPlugin worldEdit = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin
                        ("WorldEdit");

                Selection sel = worldEdit.getSelection(((Player) sender));
                Vector min = BukkitUtil.toVector(sel.getMinimumPoint());
                Vector max = BukkitUtil.toVector(sel.getMaximumPoint());
                Vector size = max.subtract(min).add(1, 1, 1);

                // Check maximum size
                if (size.getBlockX() * size.getBlockY() * size.getBlockZ() > plugin.getLocalConfiguration()
                        .areaSettings.maxSizePerArea) {
                    player.printError("Area is larger than allowed "
                            + plugin.getLocalConfiguration().areaSettings.maxSizePerArea + " blocks.");
                    return true;
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
                        return true;
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
            return true;
        } else if (command.getName().equalsIgnoreCase("savensarea")) {

            if (!(sender instanceof Player)) return false;
            LocalPlayer player = plugin.wrap((Player) sender);

            if (!player.hasPermission("craftbook.mech.savensarea")) {
                player.printError("You don't have permissions to use this command!");
                return true;
            }

            String id;
            String namespace;

            id = args[1];
            namespace = args[0];

            if (namespace.equalsIgnoreCase("@")) {
                namespace = "global";
            } else {
                if (!CopyManager.isValidNamespace(namespace)) {
                    player.printError("Invalid namespace name. For the global namespace, use @");
                    return true;
                }
                namespace = "~" + namespace;
            }

            if (!CopyManager.isValidName(id)) {
                player.printError("Invalid area name.");
                return true;
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
                    return true;
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
                        return true;
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
                    player.print(ChatColor.GOLD + "Area saved as '" + id + "' under the specified namespace.");
                } catch (IOException e) {
                    player.printError("Could not save area: " + e.getMessage());
                }
            } catch (NoClassDefFoundError e) {
                player.printError("WorldEdit.jar does not exist in plugins/.");
            }
            return true;
        }
        return false;
    }
}
