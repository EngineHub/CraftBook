// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.bukkit;

import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.craftbook.BaseConfiguration;
import com.sk89q.craftbook.LanguageManager;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.minecraft.util.commands.*;
import com.sk89q.wepif.PermissionsResolverManager;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Base plugin class for CraftBook for child CraftBook plugins.
 *
 * @author sk89q
 */
public abstract class BaseBukkitPlugin extends JavaPlugin {

    public BaseConfiguration config;

    /**
     * The permissions resolver in use.
     */
    private PermissionsResolverManager perms;

    /**
     * The Language Manager
     */
    protected LanguageManager languageManager;

    protected final CommandsManager<CommandSender> commands;
    private final CommandsManagerRegistration commandManager;

    /**
     * Logger for messages.
     */
    protected static final Logger logger = Logger.getLogger("Minecraft.CraftBook");

    public BaseBukkitPlugin() {

        commands = new CommandsManager<CommandSender>() {

            @Override
            public boolean hasPermission(CommandSender player, String perm) {

                return player.hasPermission(perm);
            }
        };
        // create the command manager
        this.commandManager = new CommandsManagerRegistration(this, commands);
        // Set the proper command injector
        commands.setInjector(new SimpleInjector(this));
    }

    /**
     * Called on load.
     */
    @Override
    public void onLoad() {

    }

    /**
     * Called when the plugin is enabled. This is where configuration is loaded,
     * and the plugin is setup.
     */
    @Override
    public void onEnable() {

        // Make the data folder for the plugin where configuration files
        // and other data files will be stored
        getDataFolder().mkdirs();
        createDefaultConfiguration("en_US.txt", true);
        createDefaultConfiguration("config.yml", false);

        config = new BaseConfiguration(getConfig(), getDataFolder());
        saveConfig();

        logger.info(getDescription().getName() + " "
                + getDescription().getVersion() + " enabled.");


        // Prepare permissions
        PermissionsResolverManager.initialize(this);
        perms = PermissionsResolverManager.getInstance();
    }

    /**
     * Called when the plugin is disabled. Shutdown and clearing of any
     * temporary data occurs here.
     */
    @Override
    public void onDisable() {

    }

    /**
     * Register the events that are used.
     */
    protected abstract void registerEvents();

    /**
     * Register an event.
     *
     * @param listener
     */
    protected void registerEvents(Listener listener) {

        getServer().getPluginManager().registerEvents(listener, this);
    }

    protected void registerCommand(Class clazz) {

        commandManager.register(clazz);
    }

    /**
     * Create a default configuration file from the .jar.
     *
     * @param name
     */
    protected void createDefaultConfiguration(String name, boolean force) {

        File actual = new File(getDataFolder(), name);
        if (!actual.exists() || force) {

            InputStream input =
                    this.getClass().getResourceAsStream("/defaults/" + name);
            if (input != null) {
                FileOutputStream output = null;

                try {
                    output = new FileOutputStream(actual);
                    byte[] buf = new byte[8192];
                    int length;
                    while ((length = input.read(buf)) > 0) {
                        output.write(buf, 0, length);
                    }

                    logger.info(getDescription().getName()
                            + ": Default configuration file written: " + name);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        input.close();
                    } catch (IOException ignored) {
                    }

                    try {
                        if (output != null)
                            output.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    /**
     * Get a player.
     *
     * @param player Bukkit Player object
     *
     * @return a (new!) object wrapping Bukkit's player type with our own.
     */
    public LocalPlayer wrap(Player player) {

        return new BukkitPlayer(this, player);
    }

    /**
     * Checks permissions.
     *
     * @param sender
     * @param perm
     *
     * @return true if the sender has the requested permission, false otherwise
     */
    public boolean hasPermission(CommandSender sender, String perm) {

        if (!(sender instanceof Player))
            return sender.isOp() && (config.commonSettings.opPerms || sender instanceof ConsoleCommandSender)
                    || perms.hasPermission(sender.getName(), perm);
        return hasPermission(sender, ((Player) sender).getWorld(), perm);
    }

    public boolean hasPermission(CommandSender sender, World world, String perm) {

        if (sender.isOp() && config.commonSettings.opPerms || sender instanceof ConsoleCommandSender)
            return true;

        // Invoke the permissions resolver
        if (sender instanceof Player) {
            Player player = (Player) sender;
            return perms.hasPermission(world.getName(), player.getName(), perm);
        }

        return false;
    }

    public boolean isInGroup(String player, String group) {

        return perms.inGroup(player, group);
    }

    public LanguageManager getLanguageManager() {

        return languageManager;
    }

    public BaseConfiguration getLocalConfiguration() {

        return config;
    }

    /**
     * Handle a command.
     */
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label,
                             String[] args) {

        try {
            commands.execute(cmd.getName(), args, sender, sender);
        } catch (CommandPermissionsException e) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.sendMessage(ChatColor.RED + "Number expected, string received instead.");
            } else {
                sender.sendMessage(ChatColor.RED + "An error has occurred. See console.");
                e.printStackTrace();
            }
        } catch (CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }

        return true;
    }
}
