/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
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

package com.sk89q.craftbook.bukkit;

import com.google.common.base.Joiner;
import com.sk89q.craftbook.CraftBookManifest;
import com.sk89q.craftbook.CraftBookMechanic;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.PlatformCommandManager;
import com.sk89q.craftbook.core.CraftBookResourceLoader;
import com.sk89q.craftbook.core.LanguageManager;
import com.sk89q.craftbook.core.mechanic.MechanicManager;
import com.sk89q.craftbook.core.st.MechanicClock;
import com.sk89q.craftbook.core.st.SelfTriggeringManager;
import com.sk89q.craftbook.mechanics.variables.VariableManager;
import com.sk89q.craftbook.util.ArrayUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.UUIDMappings;
import com.sk89q.craftbook.util.companion.CompanionPlugins;
import com.sk89q.craftbook.util.persistent.PersistentStorage;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.worldedit.bukkit.BukkitCommandSender;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.command.CommandUtil;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.io.ResourceLoader;
import com.sk89q.worldedit.util.task.SimpleSupervisor;
import com.sk89q.worldedit.util.task.Supervisor;
import com.sk89q.worldedit.util.translation.TranslationManager;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CraftBookPlugin extends JavaPlugin {

    /**
     * Companion Plugins for CraftBook.
     */
    public static CompanionPlugins plugins;

    /**
     * The instance for CraftBook
     */
    private static CraftBookPlugin instance;
    private static String version;

    private final ResourceLoader resourceLoader = new CraftBookResourceLoader();

    /**
     * The mechanic manager
     */
    private final MechanicManager mechanicManager = new MechanicManager(this);

    /**
     * The language manager
     */
    @Deprecated
    private LanguageManager languageManager;

    /**
     * The translation manager
     */
    private final TranslationManager translationManager = new TranslationManager(resourceLoader);

    /**
     * Manager for commands. This automatically handles nested commands,
     * permissions checking, and a number of other fancy command things.
     * We just set it up and register commands against it.
     */
    private final PlatformCommandManager commandManager = new PlatformCommandManager(this);

    /**
     * Handles all configuration.
     */
    private BukkitConfiguration config;

    /**
     * The adapter for events to the manager.
     */
    private MechanicListenerAdapter managerAdapter;

    /**
     * The MechanicClock that manages all Self-Triggering Components.
     */
    private MechanicClock mechanicClock;

    /**
     * The persistent storage database of CraftBook.
     */
    private PersistentStorage persistentStorage;

    /**
     * The UUID Mappings for CraftBook.
     */
    private UUIDMappings uuidMappings;

    /**
     * The manager for SelfTriggering components.
     */
    private SelfTriggeringManager selfTriggerManager;

    /**
     * The Supervisor for CraftBook tasks.
     */
    private final Supervisor supervisor = new SimpleSupervisor();

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
     * Called on plugin enable.
     */
    @Override
    public void onEnable() {
        this.mechanicManager.setup();

        plugins = new CompanionPlugins();
        plugins.initiate(this);

        // Need to create the plugins/CraftBook folder
        getDataFolder().mkdirs();

        // Load the configuration
        createDefaultConfiguration("config.yml");
        config = new BukkitConfiguration(new YAMLProcessor(new File(getDataFolder(), "config.yml"), true, YAMLFormat.EXTENDED), logger());

        try {
            config.load();
        } catch (Throwable e) {
            getLogger().log(Level.SEVERE, "Failed to load CraftBook Configuration File! Is it corrupt?", e);
            getLogger().severe("Disabling CraftBook due to invalid Configuration File!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        persistentStorage = PersistentStorage.createFromType(config.persistentStorageType);

        if (persistentStorage != null) {
            persistentStorage.open();
        }

        uuidMappings = new UUIDMappings();
        uuidMappings.enable();

        logDebugMessage("Initializing Managers!", "startup");
        managerAdapter = new MechanicListenerAdapter();

        logDebugMessage("Initializing Permission!", "startup");
        PermissionsResolverManager.initialize(this);

        // Register command classes
        logDebugMessage("Initializing Commands!", "startup");
        commandManager.registerCommandsWith(this);

        // Let's start the show
        setupCraftBook();
        registerGlobalEvents();

        getServer().getPluginManager().registerEvents(new Listener() {

            /* Bukkit Bug Fixes */

            @EventHandler(priority = EventPriority.LOWEST)
            public void signChange(SignChangeEvent event) {
                for (int i = 0; i < event.getLines().length; i++) {
                    StringBuilder builder = new StringBuilder();
                    for (char c : event.getLine(i).toCharArray()) {
                        if (c < 0xF700 || c > 0xF747) {
                            builder.append(c);
                        }
                    }
                    String fixed = builder.toString();
                    if (!fixed.equals(event.getLine(i))) {
                        event.setLine(i, fixed);
                    }
                }
            }

            /* Alerts */

            @EventHandler(priority = EventPriority.HIGH)
            public void playerJoin(PlayerJoinEvent event) {
                if (!event.getPlayer().isOp()) {
                    return;
                }

                boolean foundAMech = false;

                for (CraftBookMechanic mech : mechanicManager.getLoadedMechanics())
                    if (!(mech instanceof VariableManager)) {
                        foundAMech = true;
                        break;
                    }

                if (!foundAMech) {
                    event.getPlayer().sendMessage(ChatColor.RED + "[CraftBook] Warning! You have no mechanics enabled, the plugin will appear to do nothing until a feature is enabled!");
                }
            }
        }, this);

        boolean foundAMech = false;

        for (CraftBookMechanic mech : mechanicManager.getLoadedMechanics()) {
            if (!(mech instanceof VariableManager)) {
                foundAMech = true;
                break;
            }
        }

        if (!foundAMech) {
            Bukkit.getScheduler().runTaskTimer(this,
                    () -> getLogger().warning(ChatColor.RED + "Warning! You have no mechanics enabled, the plugin will appear to do nothing until a feature is enabled!"), 20L, 20*60*5);
        }

        PaperLib.suggestPaper(this);
    }

    /**
     * Register basic things to the plugin. For example, languages.
     */
    public void setupCraftBook() {
        if (config.debugLogToFile) {
            try {
                debugLogger = new PrintWriter(new File(getDataFolder(), "debug.log"));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
        }

        // Initialize the language manager.
        logDebugMessage("Initializing Languages!", "startup");
        languageManager = new LanguageManager();
        languageManager.init();

        logDebugMessage("Initializing Mechanisms!", "startup");
        try {
            new File(CraftBookPlugin.inst().getDataFolder(), "mechanics").mkdirs();
        } catch (Exception ignored) {}

        this.mechanicManager.enableMechanics();
        setupSelfTriggered();
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

            metrics.addCustomChart(new org.bstats.bukkit.Metrics.AdvancedPie("language",
                    () -> languageManager.getLanguages().stream().collect(Collectors.toMap(Function.identity(), o -> 1))));
            metrics.addCustomChart(new org.bstats.bukkit.Metrics.SimpleBarChart("enabled_mechanics",
                    () -> mechanicManager.getLoadedMechanics().stream().collect(Collectors.toMap(mech -> mech.getClass().getSimpleName(), o -> 1))));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Called on plugin disable.
     */
    @Override
    public void onDisable() {
        this.mechanicManager.shutdown();

        if (languageManager != null) {
            languageManager.close();
        }

        if (hasPersistentStorage()) {
            persistentStorage.close();
        }

        if (uuidMappings != null) {
            uuidMappings.disable();
        }
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

    /**
     * This retrieves the CraftBookPlugin logger.
     *
     * @return Returns the CraftBookPlugin {@link Logger}
     */
    public static Logger logger() {
        return inst().getLogger();
    }

    /**
     * Setup the required components of self-triggered Mechanics.
     */
    private void setupSelfTriggered() {
        mechanicClock = new MechanicClock();
        selfTriggerManager = new SelfTriggeringManager();

        getLogger().info("Enumerating chunks for self-triggered components...");

        long start = System.currentTimeMillis();
        int numChunks = 0;

        for (World world : getServer().getWorlds()) {
            Chunk[] chunks = world.getLoadedChunks();
            for (Chunk chunk : chunks) {
                selfTriggerManager.registerSelfTrigger(chunk);
            }
            numChunks += chunks.length;
        }

        long time = System.currentTimeMillis() - start;

        getLogger().info(numChunks + " chunk(s) for " + getServer().getWorlds().size() + " world(s) processed " + "(" + time + "ms elapsed)");

        // Set up the clock for self-triggered ICs.
        getServer().getScheduler().runTaskTimer(this, mechanicClock, 0, config.stThinkRate);
        getServer().getPluginManager().registerEvents(selfTriggerManager, this);
    }

    /**
     * Get the global ConfigurationManager.
     * Use this to access global configuration values and per-world configuration values.
     *
     * @return The global ConfigurationManager
     */
    public BukkitConfiguration getConfiguration() {
        return this.config;
    }

    /**
     * Retrieve the UUID Mappings system of CraftBook.
     *
     * @return The UUID Mappings System.
     */
    public UUIDMappings getUUIDMappings() {
        return this.uuidMappings;
    }

    /**
     * Gets the Mechanic manager.
     *
     * @return The mechanic manager
     */
    public MechanicManager getMechanicManager() {
        return this.mechanicManager;
    }

    /**
     * Gets the Translation Manager.
     *
     * @return The translation manager
     */
    public TranslationManager getTranslationManager() {
        return this.translationManager;
    }

    /**
     * This method is used to get the CraftBook {@link LanguageManager}.
     *
     * @return The CraftBook {@link LanguageManager}
     */
    @Deprecated
    public LanguageManager getLanguageManager() {
        return this.languageManager;
    }

    /**
     * Gets the CraftBook {@link Supervisor}.
     *
     * @return The supervisor
     */
    public Supervisor getSupervisor() {
        return this.supervisor;
    }

    /**
     * Check whether a player is in a group.
     * This calls the corresponding method in PermissionsResolverManager
     *
     * @param player The player to check
     * @param group  The group
     *
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
     *
     * @return The names of each group the playe is in.
     */
    public String[] getGroups(Player player) {
        try {
            return PermissionsResolverManager.getInstance().getGroups(player);
        } catch (Throwable t) {
            t.printStackTrace();
            return ArrayUtil.EMPTY_STRINGS;
        }
    }

    /**
     * Gets the name of a command sender. This is a unique name and this
     * method should never return a "display name".
     *
     * @param sender The sender to get the name of
     *
     * @return The unique name of the sender.
     */
    public String toUniqueName(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            return "*Console*";
        } else {
            return sender.getName();
        }
    }

    /**
     * Gets the name of a command sender. This play be a display name.
     *
     * @param sender The CommandSender to get the name of.
     *
     * @return The name of the given sender
     */
    public String toName(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            return "*Console*";
        } else if (sender instanceof Player) {
            return ((Player) sender).getDisplayName();
        } else {
            return sender.getName();
        }
    }

    /**
     * Checks permissions.
     *
     * @param sender The sender to check the permission on.
     * @param perm   The permission to check the permission on.
     *
     * @return whether {@code sender} has {@code perm}
     */
    public boolean hasPermission(CommandSender sender, String perm) {
        if (sender.isOp()) {
            if (sender instanceof Player) {
                if (!config.noOpPermissions) {
                    return true;
                }
            } else {
                return true;
            }
        }

        // Invoke the permissions resolver
        if (sender instanceof Player) {
            Player player = (Player) sender;
            return PermissionsResolverManager.getInstance().hasPermission(player.getWorld().getName(), player, perm);
        }

        return false;
    }

    /**
     * Checks permissions and throws an exception if permission is not met.
     *
     * @param sender The sender to check the permission on.
     * @param perm   The permission to check the permission on.
     *
     * @throws CommandPermissionsException if {@code sender} doesn't have {@code perm}
     */
    public void checkPermission(CommandSender sender, String perm)
            throws AuthorizationException {
        if (!hasPermission(sender, perm)) {
            throw new AuthorizationException();
        }
    }

    /**
     * Checks to see if the sender is a player, otherwise throw an exception.
     *
     * @param sender The {@link CommandSender} to check
     *
     * @return {@code sender} casted to a player
     *
     * @throws CommandException if {@code sender} isn't a {@link Player}
     */
    public static Player checkPlayer(CommandSender sender)
            throws CommandException {
        if (sender instanceof Player) {
            return (Player) sender;
        } else {
            throw new CommandException("A player is expected.");
        }
    }

    public Actor wrapCommandSender(CommandSender sender) {
        if (sender instanceof Player) {
            return wrapPlayer((Player) sender);
        }

        return new BukkitCommandSender(plugins.getWorldEdit(), sender);
    }

    /**
     * Wrap a player as a CraftBookPlayer.
     *
     * @param player The player to wrap
     *
     * @return The wrapped player
     */
    public CraftBookPlayer wrapPlayer(Player player) {
        return new BukkitCraftBookPlayer(this, player);
    }

    /**
     * Grabs the manager for self triggered components.
     */
    public SelfTriggeringManager getSelfTriggerManager() {
        return this.selfTriggerManager;
    }

    /**
     * Reload configuration
     */
    public void reloadConfiguration() {
        mechanicManager.shutdown();
        // TODO Replace these with better handles systems
        getServer().getScheduler().cancelTasks(inst());
        HandlerList.unregisterAll(inst());

        if(config.debugLogToFile) {
            debugLogger.close();
            debugLogger = null;
        }

        config.load();
        managerAdapter = new MechanicListenerAdapter();
        mechanicClock = new MechanicClock();
        setupCraftBook();
        registerGlobalEvents();
    }

    /**
     * Create a default configuration file from the .jar.
     *
     * @param name the filename
     */
    public void createDefaultConfiguration(String name) {
        File actual = new File(getDataFolder(), name);
        if (!actual.exists()) {
            try (InputStream stream = getResource("defaults/" + name)) {
                if (stream == null) throw new FileNotFoundException();
                copyDefaultConfig(stream, actual, name);
            } catch (IOException e) {
                getLogger().severe("Unable to read default configuration: " + name);
            }
        }
    }

    private void copyDefaultConfig(InputStream input, File actual, String name) {
        try (FileOutputStream output = new FileOutputStream(actual)) {
            byte[] buf = new byte[8192];
            int length;
            while ((length = input.read(buf)) > 0) {
                output.write(buf, 0, length);
            }

            getLogger().info("Default configuration file written: " + name);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Failed to write default config file", e);
        }
    }

    public static boolean isDebugFlagEnabled(String flag) {
        if (inst() == null) {
            return false;
        }

        if (!inst().config.debugMode || inst().config.debugFlags == null || inst().config.debugFlags.isEmpty()) {
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

            for (String testflag : inst().config.debugFlags) {
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

        logger().info("[Debug][" + code + "] " + message);

        if (CraftBookPlugin.inst().config.debugLogToFile) {
            debugLogger.println("[" + code + "] " + message);
        }
    }

    public boolean hasPersistentStorage() {
        return persistentStorage != null && persistentStorage.isValid();
    }

    public PersistentStorage getPersistentStorage() {
        return persistentStorage;
    }

    public void setPersistentStorage(PersistentStorage storage) {
        persistentStorage = storage;

        config.persistentStorageType = storage.getType();

        config.config.setProperty("persistent-storage-type", storage.getType());

        config.config.save();
    }

    public static String getDocsDomain() {
        return "https://craftbook.enginehub.org/en/latest/";
    }

    /**
     * Get the version.
     *
     * @return the version of CraftBook
     */
    public static String getVersion() {
        if (version != null) {
            return version;
        }

        CraftBookManifest manifest = CraftBookManifest.load();

        return version = manifest.getCraftBookVersion();
    }
}