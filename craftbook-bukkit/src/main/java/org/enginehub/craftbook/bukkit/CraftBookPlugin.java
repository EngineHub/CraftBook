/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.bukkit;

import com.google.common.base.Joiner;
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.command.CommandUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlatform;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.PlatformCommandManager;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanics.variables.VariableManager;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.companion.CompanionPlugins;
import org.enginehub.craftbook.util.profile.Profile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CraftBookPlugin extends JavaPlugin {

    /**
     * Companion Plugins for CraftBook.
     */
    public static CompanionPlugins plugins;

    /**
     * The instance for CraftBook.
     */
    private static CraftBookPlugin instance;
    private static CraftBookPlatform platform;

    /**
     * Manager for commands. This automatically handles nested commands,
     * permissions checking, and a number of other fancy command things.
     * We just set it up and register commands against it.
     */
    private final PlatformCommandManager commandManager = new PlatformCommandManager();

    /**
     * The adapter for events to the manager.
     */
    private MechanicListenerAdapter managerAdapter;

    private static final int BSTATS_ID = 3319;

    /**
     * Construct objects. Actual loading occurs when the plugin is enabled, so
     * this merely instantiates the objects.
     */
    public CraftBookPlugin() {
        super();

        // Set the instance
        instance = this;
    }

    /**
     * Create a default configuration file from the .jar.
     *
     * @param name the filename
     */
    public void createDefaultConfiguration(String name) {
        Path actual = CraftBook.getInstance().getPlatform().getWorkingDirectory().resolve(name);

        if (Files.notExists(actual)) {
            try (InputStream stream = getResource("defaults/" + name)) {
                if (stream == null) {
                    throw new FileNotFoundException();
                }
                try {
                    Files.copy(stream, actual);
                    CraftBook.LOGGER.info("Default configuration file written: " + name);
                } catch (IOException e) {
                    CraftBook.LOGGER.warn("Failed to write default config file", e);
                }
            } catch (IOException e) {
                CraftBook.LOGGER.error("Unable to read default configuration: " + name);
            }
        }
    }

    @Override
    public void onEnable() {
        // Need to create the plugins/CraftBook folder
        getDataFolder().mkdirs();

        CraftBook.getInstance().setPlatform(platform = new BukkitCraftBookPlatform()); // Initialise CraftBook
        CraftBook.getInstance().setup();

        plugins = new CompanionPlugins();
        plugins.initiate(this);

        logDebugMessage("Initializing Managers!", "startup");
        managerAdapter = new MechanicListenerAdapter();

        logDebugMessage("Initializing Permission!", "startup");
        PermissionsResolverManager.initialize(this);

        // Let's start the show
        setupCraftBook();
        registerGlobalEvents();

        // Register command classes
        logDebugMessage("Initializing Commands!", "startup");
        commandManager.registerCommandsWith(platform);

        getServer().getPluginManager().registerEvents(new Listener() {
            /* Alerts */

            @EventHandler(priority = EventPriority.HIGH)
            public void playerJoin(PlayerJoinEvent event) {
                CraftBook.getInstance().getExecutorService().submit(() ->
                    CraftBook.getInstance().getProfileCache().put(new Profile(event.getPlayer().getUniqueId(), event.getPlayer().getName())));

                if (!event.getPlayer().isOp()) {
                    return;
                }

                boolean foundAMech = false;

                for (CraftBookMechanic mech : platform.getMechanicManager().getLoadedMechanics()) {
                    if (!(mech instanceof VariableManager)) {
                        foundAMech = true;
                        break;
                    }
                }

                if (!foundAMech) {
                    event.getPlayer().sendMessage(Component.text("[CraftBook] Warning! You have no mechanics enabled, the plugin will appear to do nothing until a feature is enabled!", NamedTextColor.RED));
                }
            }
        }, this);

        boolean foundAMech = false;

        for (CraftBookMechanic mech : platform.getMechanicManager().getLoadedMechanics()) {
            if (!(mech instanceof VariableManager)) {
                foundAMech = true;
                break;
            }
        }

        if (!foundAMech) {
            Bukkit.getScheduler().runTaskTimer(this,
                () -> getComponentLogger().warn(Component.text("Warning! You have no mechanics enabled, the plugin will appear to do nothing until a feature is enabled!", NamedTextColor.RED)), 20L, 20 * 60 * 5);
        }
    }

    /**
     * Register basic things to the plugin. For example, languages.
     */
    public void setupCraftBook() {
        if (platform.getConfiguration().debugLogToFile) {
            try {
                debugLogger = new PrintWriter(new File(getDataFolder(), "debug.log"));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
        }
        logDebugMessage("Initializing Mechanisms!", "startup");
        try {
            new File(CraftBookPlugin.inst().getDataFolder(), "mechanics").mkdirs();
        } catch (Exception ignored) {
        }

        platform.getMechanicManager().enableMechanics();
    }

    /**
     * Registers events used by the main CraftBook plugin. Also registers PluginMetrics
     */
    public void registerGlobalEvents() {
        logDebugMessage("Registring managers!", "startup");
        getServer().getPluginManager().registerEvents(managerAdapter, inst());

        try {
            logDebugMessage("Initializing Metrics!", "startup");
            org.bstats.bukkit.Metrics metrics = new org.bstats.bukkit.Metrics(this, BSTATS_ID);

            metrics.addCustomChart(new org.bstats.charts.SimpleBarChart("enabled_mechanics",
                () -> platform.getMechanicManager().getLoadedMechanics().stream().collect(Collectors.toMap(mech -> mech.getClass().getSimpleName(), o -> 1))));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Called on plugin disable.
     */
    @Override
    public void onDisable() {
        platform.unload();
    }

    // FIXME: Backport to WorldEdit/common lib
    private String rebuildArguments(String commandLabel, String[] args) {
        int plSep = commandLabel.indexOf(":");
        if (plSep >= 0 && plSep < commandLabel.length() + 1) {
            commandLabel = commandLabel.substring(plSep + 1);
        }

        StringBuilder sb = new StringBuilder("/").append(commandLabel);
        if (args.length > 0) {
            sb.append(" ");
        }

        return Joiner.on(" ").appendTo(sb, args).toString();
    }

    /**
     * Handle a command.
     */
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        commandManager.handleCommand(wrapCommandSender(sender), rebuildArguments(label, args));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String arguments = rebuildArguments(alias, args);
        return CommandUtil.fixSuggestions(arguments, commandManager.handleCommandSuggestion(wrapCommandSender(sender), arguments));
    }

    /**
     * This retrieves the CraftBookPlugin instance for static access.
     *
     * @return Returns a CraftBookPlugin
     */
    public static CraftBookPlugin inst() {
        return instance;
    }

    public PlatformCommandManager getCommandManager() {
        return this.commandManager;
    }

    /**
     * Check whether a player is in a group.
     * This calls the corresponding method in PermissionsResolverManager
     *
     * @param player The player to check
     * @param group The group
     * @return whether {@code player} is in {@code group}
     */
    public boolean inGroup(Player player, String group) {
        try {
            return PermissionsResolverManager.getInstance().inGroup(player, group);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    /**
     * Get the groups of a player.
     * This calls the corresponding method in PermissionsResolverManager.
     *
     * @param player The player to check
     * @return The names of each group the playe is in.
     */
    public String[] getGroups(Player player) {
        try {
            return PermissionsResolverManager.getInstance().getGroups(player);
        } catch (Throwable t) {
            t.printStackTrace();
            return new String[0];
        }
    }

    /**
     * Checks permissions.
     *
     * @param sender The sender to check the permission on.
     * @param perm The permission to check the permission on.
     * @return whether {@code sender} has {@code perm}
     */
    public boolean hasPermission(CommandSender sender, String perm) {
        if (sender.isOp()) {
            if (sender instanceof Player) {
                if (!platform.getConfiguration().noOpPermissions) {
                    return true;
                }
            } else {
                return true;
            }
        }

        // Invoke the permissions resolver
        if (sender instanceof Player player) {
            return PermissionsResolverManager.getInstance().hasPermission(player.getWorld().getName(), player, perm);
        }

        return false;
    }

    public Actor wrapCommandSender(CommandSender sender) {
        if (sender instanceof Player) {
            return wrapPlayer((Player) sender);
        }

        return new BukkitCraftBookCommandSender(plugins.getWorldEdit(), sender);
    }

    /**
     * Wrap a player as a CraftBookPlayer.
     *
     * @param player The player to wrap
     * @return The wrapped player
     */
    public CraftBookPlayer wrapPlayer(Player player) {
        return new BukkitCraftBookPlayer(this, player);
    }

    /**
     * Reload configuration.
     */
    public void reloadConfiguration() {
        platform.getMechanicManager().shutdown();
        // TODO Replace these with better handles systems
        getServer().getScheduler().cancelTasks(inst());
        HandlerList.unregisterAll(inst());

        if (platform.getConfiguration().debugLogToFile) {
            debugLogger.close();
            debugLogger = null;
        }

        platform.getConfiguration().load();
        managerAdapter = new MechanicListenerAdapter();
        setupCraftBook();
        registerGlobalEvents();
    }

    public static boolean isDebugFlagEnabled(String flag) {
        if (inst() == null) {
            return false;
        }

        if (!CraftBook.getInstance().getPlatform().getConfiguration().debugMode
            || CraftBook.getInstance().getPlatform().getConfiguration().debugFlags == null
            || CraftBook.getInstance().getPlatform().getConfiguration().debugFlags.isEmpty()) {
            return false;
        }

        String[] flagBits = RegexUtil.PERIOD_PATTERN.split(flag);

        String tempFlag = "";

        for (int i = 0; i < flagBits.length; i++) {
            if (i == 0) {
                tempFlag = flagBits[i];
            } else {
                tempFlag = tempFlag + "." + flagBits[i];
            }

            for (String testflag : CraftBook.getInstance().getPlatform().getConfiguration().debugFlags) {
                if (testflag.toLowerCase(Locale.ENGLISH).equals(tempFlag)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static PrintWriter debugLogger;

    public static void logDebugMessage(String message, String code) {
        if (!isDebugFlagEnabled(code)) {
            return;
        }

        CraftBook.LOGGER.info("[Debug][" + code + "] " + message);

        if (CraftBook.getInstance().getPlatform().getConfiguration().debugLogToFile) {
            debugLogger.println("[" + code + "] " + message);
        }
    }

}
