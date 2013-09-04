package com.sk89q.craftbook.bukkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.craftbook.LanguageManager;
import com.sk89q.craftbook.LocalComponent;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.Mechanic;
import com.sk89q.craftbook.MechanicClock;
import com.sk89q.craftbook.MechanicFactory;
import com.sk89q.craftbook.MechanicManager;
import com.sk89q.craftbook.bukkit.Metrics.Graph;
import com.sk89q.craftbook.bukkit.Metrics.Plotter;
import com.sk89q.craftbook.bukkit.commands.TopLevelCommands;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.Tuple2;
import com.sk89q.craftbook.util.config.VariableConfiguration;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.SimpleInjector;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.flags.DefaultFlag;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;

public class CraftBookPlugin extends JavaPlugin {

    /**
     * Required dependencies
     */
    private WorldEditPlugin worldEditPlugin;

    /**
     * Optional dependencies
     */
    private Economy economy;
    private ProtocolLibrary protocolLib;
    private WorldGuardPlugin worldGuardPlugin;
    private boolean hasNoCheatPlus;

    /**
     * The instance for CraftBook
     */
    private static CraftBookPlugin instance;

    /**
     * The language manager
     */
    private LanguageManager languageManager;

    /**
     * The mechanic manager
     */
    private MechanicManager manager;

    /**
     * The random
     */
    private Random random;

    /**
     * Manager for commands. This automatically handles nested commands,
     * permissions checking, and a number of other fancy command things.
     * We just set it up and register commands against it.
     */
    private CommandsManager<CommandSender> commands;

    /**
     * Handles all configuration.
     */
    private BukkitConfiguration config;
    private VariableConfiguration variableConfiguration;

    /**
     * Used for backwards compatability of block faces.
     */
    private Boolean useOldBlockFace;

    /**
     * The currently enabled LocalComponents
     */
    private List<LocalComponent> components = new ArrayList<LocalComponent>();

    /**
     * The adapter for events to the manager.
     */
    MechanicListenerAdapter managerAdapter;

    /**
     * The MechanicClock that manages all Self-Triggering Components.
     */
    private MechanicClock mechanicClock;

    /**
     * Stores the variables used in VariableStore.
     */
    protected HashMap<Tuple2<String, String>, String> variableStore = new HashMap<Tuple2<String, String>, String>();

    /**
     * Construct objects. Actual loading occurs when the plugin is enabled, so
     * this merely instantiates the objects.
     */
    public CraftBookPlugin() {

        // Set the instance
        instance = this;
    }

    public static String getVersion() {

        return "3.7.4";
    }

    /**
     * Gets the build equivalent of the last stable version.
     * 
     * @return the build number
     */
    public static String getStableBuild() {

        return "3210";
    }

    /**
     * Called on plugin enable.
     */
    @Override
    public void onEnable() {

        // Check plugin for checking the active states of a plugin
        Plugin checkPlugin;

        // Check for WorldEdit
        checkPlugin = getServer().getPluginManager().getPlugin("WorldEdit");
        if (checkPlugin != null && checkPlugin instanceof WorldEditPlugin) {
            worldEditPlugin = (WorldEditPlugin) checkPlugin;
        } else {
            try {
                //noinspection UnusedDeclaration
                @SuppressWarnings("unused")
                String s = WorldEditPlugin.CUI_PLUGIN_CHANNEL;
            } catch (Throwable t) {
                logger().severe("WorldEdit detection has failed!");
                logger().severe("WorldEdit is a required dependency, Craftbook disabled!");
                return;
            }
        }

        // Resolve ProtocolLib
        try {
            checkPlugin = getServer().getPluginManager().getPlugin("ProtocolLib");
            if (checkPlugin != null && checkPlugin instanceof ProtocolLibrary) {
                protocolLib = (ProtocolLibrary) checkPlugin;
            } else protocolLib = null;
        } catch(Throwable e){
            protocolLib = null;
            getLogger().severe("You have a corrupt version of ProtocolLib! Please redownload it!");
            BukkitUtil.printStacktrace(e);
        }

        // Resolve WorldGuard
        checkPlugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (checkPlugin != null && checkPlugin instanceof WorldGuardPlugin) {
            worldGuardPlugin = (WorldGuardPlugin) checkPlugin;
        } else worldGuardPlugin = null;

        // Resolve Vault
        try {
            RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) economy = economyProvider.getProvider();
            else economy = null;
        } catch (Throwable e) {
            economy = null;
        }

        // Resolve NoCheatPlus
        hasNoCheatPlus = getServer().getPluginManager().getPlugin("NoCheatPlus") != null;

        // Need to create the plugins/CraftBook folder
        getDataFolder().mkdirs();

        // Setup Config and the Commands Manager
        createDefaultConfiguration(new File(getDataFolder(), "config.yml"), "config.yml");
        config = new BukkitConfiguration(new YAMLProcessor(new File(getDataFolder(), "config.yml"), true, YAMLFormat.EXTENDED), logger());
        // Load the configuration
        try {
            config.load();
        } catch (Throwable e) {
            getLogger().severe("Failed to load CraftBook Configuration File! Is it corrupt?");
            getLogger().severe(getStackTrace(e));
            getLogger().severe("Disabling CraftBook due to invalid Configuration File!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        logDebugMessage("Initializing Managers!", "startup");
        manager = new MechanicManager();
        managerAdapter = new MechanicListenerAdapter();
        mechanicClock = new MechanicClock();

        logDebugMessage("Initializing Permission!", "startup");
        PermissionsResolverManager.initialize(this);

        // Register command classes
        logDebugMessage("Initializing Commands!", "startup");
        commands = new CommandsManager<CommandSender>() {

            @Override
            public boolean hasPermission(CommandSender player, String perm) {

                return CraftBookPlugin.inst().hasPermission(player, perm);
            }
        };
        // Set the proper command injector
        commands.setInjector(new SimpleInjector(this));

        final CommandsManagerRegistration reg = new CommandsManagerRegistration(this, commands);
        reg.register(TopLevelCommands.class);

        if(config.realisticRandoms)
            try {
                random = SecureRandom.getInstance("SHA1PRNG");
            } catch (NoSuchAlgorithmException e1) {
                getLogger().severe(getStackTrace(e1));
                random = new Random();
            }
        else
            random = new Random();

        // Let's start the show
        setupCraftBook();
        registerGlobalEvents();
        startComponents();
    }

    public boolean updateAvailable = false;
    String latestVersion = null;
    long updateSize = 0;

    public String getLatestVersion() {

        return latestVersion;
    }

    public boolean isUpdateAvailable() {

        return updateAvailable;
    }

    /**
     * Register basic things to the plugin. For example, languages.
     */
    public void setupCraftBook() {

        // Initialize the language manager.
        logDebugMessage("Initializing Languages!", "startup");
        createDefaultConfiguration(new File(getDataFolder(), "en_US.yml"), "en_US.yml");
        languageManager = new LanguageManager();
        languageManager.init();
    }

    /**
     * Get the mechanic manager.
     * 
     * @return The mechanic manager.
     */
    public MechanicManager getManager() {

        return manager;
    }

    public boolean hasVariable(String variable, String namespace) {

        if(!config.variablesEnabled)
            return false;
        return variableStore.containsKey(new Tuple2<String, String>(variable, namespace));
    }

    public String getVariable(String variable, String namespace) {

        if(!config.variablesEnabled)
            return "Variables Are Disabled!";
        return variableStore.get(new Tuple2<String, String>(variable, namespace));
    }

    public String setVariable(String variable, String namespace, String value) {

        if(!config.variablesEnabled)
            return "Variables Are Disabled!";
        return variableStore.put(new Tuple2<String, String>(variable, namespace), value);
    }

    public String removeVariable(String variable, String namespace) {

        if(!config.variablesEnabled)
            return "Variables Are Disabled!";
        return variableStore.remove(new Tuple2<String, String>(variable, namespace));
    }

    public HashMap<Tuple2<String, String>, String> getVariableStore() {

        return variableStore;
    }

    /**
     * Registers events used by the main CraftBook plugin. Also registers PluginMetrics
     */
    public void registerGlobalEvents() {

        logDebugMessage("Registring managers!", "startup");
        getServer().getPluginManager().registerEvents(managerAdapter, inst());

        if(getConfiguration().updateNotifier) {

            logDebugMessage("Performing update checks!", "startup");
            checkForUpdates();
        }

        if(getConfiguration().easterEggs) {
            Bukkit.getScheduler().runTaskLater(this, new Runnable() {

                @Override
                public void run () {

                    logDebugMessage("Checking easter eggs!", "startup");
                    Calendar date = Calendar.getInstance();

                    if(date.get(Calendar.MONTH) == Calendar.JUNE && date.get(Calendar.DAY_OF_MONTH) == 22) //Me4502 reddit cakeday
                        getLogger().info("Happy " + (date.get(Calendar.YEAR) - 2012) + "th reddit cakeday me4502!");
                    else if(date.get(Calendar.MONTH) == Calendar.OCTOBER && date.get(Calendar.DAY_OF_MONTH) == 16) //Me4502 birthday
                        getLogger().info("Happy birthday me4502!");
                    else if(date.get(Calendar.MONTH) == Calendar.JANUARY && date.get(Calendar.DAY_OF_MONTH) == 1) //New Years
                        getLogger().info("Happy new years! Happy " + date.get(Calendar.YEAR) + "!!!");
                    else if(date.get(Calendar.MONTH) == Calendar.OCTOBER && date.get(Calendar.DAY_OF_MONTH) == 22) //CraftBook birthday
                        getLogger().info("Happy " + (date.get(Calendar.YEAR) - 2010) + "th birthday CraftBook!");
                    else if(date.get(Calendar.MONTH) == Calendar.APRIL && date.get(Calendar.DAY_OF_MONTH) == 24) //Me4502ian CraftBook birthday
                        getLogger().info("CraftBook has been under Me4502's 'harsh dictatorship :P' for " + (date.get(Calendar.YEAR) - 2012) + " year(s) today!");
                }
            }, 20L);
        }

        try {
            logDebugMessage("Initializing Metrics!", "startup");
            Metrics metrics = new Metrics(this);
            metrics.start();

            Graph g = metrics.createGraph("Language");
            for (String language : languageManager.getLanguages()) {
                g.addPlotter(new Plotter(language) {

                    @Override
                    public int getValue () {
                        return 1;
                    }
                });
            }
            g.addPlotter(new Plotter("Total") {

                @Override
                public int getValue () {
                    return languageManager.getLanguages().size();
                }
            });

            Graph comp = metrics.createGraph("Components");
            if (config.enableCircuits)
                comp.addPlotter(new Plotter("Circuits") {

                    @Override
                    public int getValue () {
                        return 1;
                    }
                });
            if (config.enableMechanisms)
                comp.addPlotter(new Plotter("Mechanisms") {

                    @Override
                    public int getValue () {
                        return 1;
                    }
                });
            if (config.enableVehicles)
                comp.addPlotter(new Plotter("Vehicles") {

                    @Override
                    public int getValue () {
                        return 1;
                    }
                });
        } catch (Throwable e1) {
            BukkitUtil.printStacktrace(e1);
        }
    }

    public void checkForUpdates() {

        boolean exempt = false;

        try {
            int ver = Integer.parseInt(getDescription().getVersion().split("-")[0]);
            if (ver < 1541) //Not valid prior to this version.
                exempt = true;
        }
        catch(Exception e) {
            exempt = true;
        }

        if(!exempt) {
            final Updater updater = new Updater(this, "CraftBook", getFile(), Updater.UpdateType.NO_DOWNLOAD, false); // Start Updater but just do a version check
            updateAvailable = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE; // Determine if there is an update ready for us
            latestVersion = updater.getLatestVersionString(); // Get the latest version
            getLogger().info(latestVersion + " is the latest version available, and the updatability of it is: " + updater.getResult().name() + ". You currently have version " + updater.getCurrentVersionString() + " installed.");
            updateSize = updater.getFileSize(); // Get latest size

            if(updateAvailable) {

                for (Player player : getServer().getOnlinePlayers()) {
                    if (hasPermission(player, "craftbook.update")) {
                        player.sendMessage(ChatColor.YELLOW + "An update is available: " + latestVersion + "(" + updateSize + " bytes)");
                        player.sendMessage(ChatColor.YELLOW + "Type /cb update if you would like to update.");
                    }
                }

                getServer().getPluginManager().registerEvents(new Listener() {
                    @EventHandler
                    public void onPlayerJoin (PlayerJoinEvent event) {
                        Player player = event.getPlayer();
                        if (hasPermission(player, "craftbook.update")) {
                            player.sendMessage(ChatColor.YELLOW + "An update is available: " + latestVersion + "(" + updateSize + " bytes)");
                            player.sendMessage(ChatColor.YELLOW + "Type /cb update if you would like to update.");
                        }
                    }
                }, CraftBookPlugin.inst());
            }
        } else {
            getLogger().info("The Auto-Updater is disabled for your version!");
        }
    }

    public void startComponents() {

        // VariableStore
        if(config.variablesEnabled) {
            logDebugMessage("Initializing Variables!", "startup.variables");
            try {
                File varFile = new File(getDataFolder(), "variables.yml");
                if(!varFile.exists())
                    varFile.createNewFile();
                variableConfiguration = new VariableConfiguration(new YAMLProcessor(varFile, true, YAMLFormat.EXTENDED), logger());
                variableConfiguration.load();
            } catch(Exception ignored){}
        }

        // Mechanics
        if (config.enableMechanisms) {
            logDebugMessage("Initializing Mechanisms!", "startup.mechanisms");
            MechanicalCore mechanicalCore = new MechanicalCore();
            mechanicalCore.enable();
            components.add(mechanicalCore);
        }
        // Circuits
        if (config.enableCircuits) {
            logDebugMessage("Initializing Circuits!", "startup.circuits");
            CircuitCore circuitCore = new CircuitCore();
            circuitCore.enable();
            components.add(circuitCore);
        }
        // Vehicles
        if (config.enableVehicles) {
            logDebugMessage("Initializing Vehicles!", "startup.vehicles");
            VehicleCore vehicleCore = new VehicleCore();
            vehicleCore.enable();
            components.add(vehicleCore);
        }

        setupSelfTriggered();
    }

    /**
     * Called on plugin disable.
     */
    @Override
    public void onDisable() {

        languageManager.close();
        for (LocalComponent component : components) {
            component.disable();
        }
        if(config.variablesEnabled)
            variableConfiguration.save();
        components.clear();
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

    /**
     * This retrieves the CraftBookPlugin instance for static access.
     *
     * @return Returns a CraftBookPlugin
     */
    public static CraftBookPlugin inst() {

        return instance;
    }

    public static void setInstance(CraftBookPlugin instance) throws IllegalArgumentException {

        if(CraftBookPlugin.instance != null)
            throw new IllegalArgumentException("Instance already set!");

        CraftBookPlugin.instance = instance;
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
     * This retrieves the CraftBookPlugin server.
     *
     * @return Returns the CraftBookPlugin {@link Server}
     */
    public static Server server() {

        return inst().getServer();
    }

    /**
     * Register a mechanic if possible
     *
     * @param factory
     */
    public void registerMechanic(MechanicFactory<? extends Mechanic> factory) {

        manager.register(factory);
    }

    /**
     * Register a array of mechanics if possible
     *
     * @param factories
     */
    protected void registerMechanic(MechanicFactory<? extends Mechanic>[] factories) {

        for (MechanicFactory<? extends Mechanic> aFactory : factories) {
            registerMechanic(aFactory);
        }
    }

    /**
     * Unregister a mechanic if possible TODO Ensure no remnants are left behind
     *
     * @param factory
     *
     * @return true if the mechanic was successfully unregistered.
     */
    protected boolean unregisterMechanic(MechanicFactory<? extends Mechanic> factory) {

        return manager.unregister(factory);
    }

    protected boolean unregisterAllMechanics() {

        Iterator<MechanicFactory<? extends Mechanic>> iterator = manager.factories.iterator();

        while (iterator.hasNext()) {
            iterator.next();
            manager.unregister(iterator);
        }

        return true;
    }

    /**
     * Setup the required components of self-triggered Mechanics.
     */
    private void setupSelfTriggered() {

        getLogger().info("Enumerating chunks for self-triggered components...");

        long start = System.currentTimeMillis();
        int numWorlds = 0;
        int numChunks = 0;

        for (World world : getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                manager.enumerate(chunk);
                numChunks++;
            }

            numWorlds++;
        }

        long time = System.currentTimeMillis() - start;

        getLogger().info(numChunks + " chunk(s) for " + numWorlds + " world(s) processed " + "(" + time + "ms " +
                "elapsed)");

        // Set up the clock for self-triggered ICs.
        getServer().getScheduler().runTaskTimer(this, mechanicClock, 0, getConfiguration().stThinkRate);
    }

    /**
     * This is a method used to register events for a class under CraftBook.
     */
    public static void registerEvents(Listener ... listeners) {

        for(Listener listener : listeners)
            inst().getServer().getPluginManager().registerEvents(listener, inst());
    }

    /**
     * This is a method used to register commands for a class.
     */
    public void registerCommands(Class<?> clazz) {

        final CommandsManagerRegistration reg = new CommandsManagerRegistration(this, commands);
        reg.register(clazz);
    }

    /**
     * Get the global ConfigurationManager.
     * Use this to access global configuration values and per-world configuration values.
     *
     * @return The global ConfigurationManager
     */
    public BukkitConfiguration getConfiguration() {

        return config;
    }

    /**
     * This method is used to get the CraftBook {@link LanguageManager}.
     *
     * @return The CraftBook {@link LanguageManager}
     */
    public LanguageManager getLanguageManager() {

        return languageManager;
    }

    /**
     * This method is used to get CraftBook's {@link Random}.
     *
     * @return CraftBook's {@link Random}
     */
    public Random getRandom() {

        if(random == null)
            return new Random(); //Use a temporary random whilst CraftBooks random is being set.
        return random;
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
            return new String[0];
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
                if (!getConfiguration().noOpPermissions) return true;
            } else {
                return true;
            }
        }

        // Invoke the permissions resolver
        if (sender instanceof Player) {
            Player player = (Player) sender;
            return PermissionsResolverManager.getInstance().hasPermission(player.getWorld().getName(), player.getName(), perm);
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
            throws CommandPermissionsException {

        if (!hasPermission(sender, perm)) {
            throw new CommandPermissionsException();
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
    public Player checkPlayer(CommandSender sender)
            throws CommandException {

        if (sender instanceof Player) {
            return (Player) sender;
        } else {
            throw new CommandException("A player is expected.");
        }
    }

    /**
     * Wrap a player as a LocalPlayer.
     *
     * @param player The player to wrap
     *
     * @return The wrapped player
     */
    public LocalPlayer wrapPlayer(Player player) {

        return new BukkitPlayer(this, player);
    }

    /**
     * Reload configuration
     */
    public void reloadConfiguration() throws Throwable {

        unregisterAllMechanics();
        for (LocalComponent component : components) {
            component.disable();
        }
        if(config.variablesEnabled)
            variableConfiguration.save();
        components.clear();
        getServer().getScheduler().cancelTasks(inst());
        HandlerList.unregisterAll(inst());
        config.load();
        manager = new MechanicManager();
        managerAdapter = new MechanicListenerAdapter();
        mechanicClock = new MechanicClock();
        setupCraftBook();
        registerGlobalEvents();
        startComponents();
    }

    /**
     * Create a default configuration file from the .jar.
     *
     * @param actual      The destination file
     * @param defaultName The name of the file inside the jar's defaults folder
     * @param force       If it should make the file even if it already exists
     */
    public void createDefaultConfiguration(File actual, String defaultName) {

        // Make parent directories
        File parent = actual.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        if (actual.exists()) {
            return;
        }

        InputStream input = null;
        JarFile file = null;
        try {
            file = new JarFile(getFile());
            ZipEntry copy = file.getEntry("defaults/" + defaultName);
            if (copy == null) {
                file.close();
                throw new FileNotFoundException();
            }
            input = file.getInputStream(copy);
        } catch (IOException e) {
            getLogger().severe("Unable to read default configuration: " + defaultName);
        }

        if (input != null) {
            FileOutputStream output = null;

            try {
                output = new FileOutputStream(actual);
                byte[] buf = new byte[8192];
                int length = 0;
                while ((length = input.read(buf)) > 0) {
                    output.write(buf, 0, length);
                }

                getLogger().info("Default configuration file written: " + actual.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                try {
                    file.close();
                } catch (IOException ignored) {
                }

                try {
                    input.close();
                } catch (IOException ignore) {
                }

                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException ignore) {
                }
            }
        } else if (file != null)
            try {
                file.close();
            } catch (IOException ignored) {
            }
    }

    /**
     * Gets the Vault {@link Economy} service if it exists, this method should be used for economic actions.
     *
     * This method can return null.
     *
     * @return The vault {@link Economy} service
     */
    public Economy getEconomy() {

        return economy;
    }

    /**
     * This method is used to determine whether ProtocolLib is
     * enabled on the server.
     *
     * @return True if ProtocolLib was found
     */
    public boolean hasProtocolLib() {

        return protocolLib != null;
    }

    /**
     * Gets a copy of {@link ProtocolLibrary}.
     *
     * @return The {@link ProtocolLibrary} instance
     */
    public ProtocolLibrary getProtocolLib() {

        return protocolLib;
    }

    /**
     * Gets a copy of the {@link WorldEditPlugin}.
     *
     * This method cannot return null.
     *
     * @return The {@link WorldEditPlugin} instance
     */
    public WorldEditPlugin getWorldEdit() {

        return worldEditPlugin;
    }

    /**
     * Gets the {@link WorldGuardPlugin} for non-build checks.
     *
     * This method can return null.
     *
     * @return {@link WorldGuardPlugin}
     */
    public WorldGuardPlugin getWorldGuard() {

        return worldGuardPlugin;
    }

    /**
     * Checks to see if a player can build at a location. This will return
     * true if region protection is disabled.
     *
     * @param player The player to check.
     * @param loc    The location to check at.
     *
     * @return whether {@code player} can build at {@code loc}
     *
     * @see GlobalRegionManager#canBuild(org.bukkit.entity.Player, org.bukkit.Location)
     */
    public boolean canBuild(Player player, Location loc, boolean build) {

        return canBuild(player,loc.getBlock(), build);
    }

    /**
     * Checks to see if a player can build at a location. This will return
     * true if region protection is disabled or WorldGuard is not found.
     *
     * @param player The player to check
     * @param block  The block to check at.
     * @param build True for build, false for break
     *
     * @return whether {@code player} can build at {@code block}'s location
     *
     * @see GlobalRegionManager#canBuild(org.bukkit.entity.Player, org.bukkit.block.Block)
     */
    public boolean canBuild(Player player, Block block, boolean build) {

        if (config.advancedBlockChecks) {

            boolean unexempt = false;
            if(hasNoCheatPlus) {
                if(!NCPExemptionManager.isExempted(player, CheckType.BLOCKBREAK_NOSWING)) {
                    NCPExemptionManager.exemptPermanently(player, CheckType.BLOCKBREAK_NOSWING);
                    unexempt = true;
                }
            }
            BlockEvent event;
            if(build)
                event = new BlockPlaceEvent(block, block.getState(), block.getRelative(0, -1, 0), player.getItemInHand(), player, true);
            else
                event = new BlockBreakEvent(block, player);
            MechanicListenerAdapter.ignoreEvent(event);
            getServer().getPluginManager().callEvent(event);
            if(unexempt)
                NCPExemptionManager.unexempt(player, CheckType.BLOCKBREAK_NOSWING);
            return !(((Cancellable) event).isCancelled() || event instanceof BlockPlaceEvent && !((BlockPlaceEvent) event).canBuild());
        }
        if (!config.obeyWorldguard) return true;
        return worldGuardPlugin == null || worldGuardPlugin.canBuild(player, block);
    }

    /**
     * Checks to see if a player can use at a location. This will return
     * true if region protection is disabled or WorldGuard is not found.
     *
     * @param player The player to check.
     * @param loc    The location to check at.
     *
     * @return whether {@code player} can build at {@code loc}
     *
     * @see GlobalRegionManager#canBuild(org.bukkit.entity.Player, org.bukkit.Location)
     */
    public boolean canUse(Player player, Location loc, BlockFace face, Action action) {

        if (config.advancedBlockChecks) {

            PlayerInteractEvent event = new PlayerInteractEvent(player, action == null ? Action.RIGHT_CLICK_BLOCK : action, player.getItemInHand(), loc.getBlock(), face == null ? BlockFace.SELF : face);
            MechanicListenerAdapter.ignoreEvent(event);
            getServer().getPluginManager().callEvent(event);
            return !event.isCancelled();
        }
        if (!config.obeyWorldguard) return true;
        return worldGuardPlugin == null || worldGuardPlugin.getGlobalRegionManager().allows(DefaultFlag.USE, loc, worldGuardPlugin.wrapPlayer(player));
    }

    /**
     * Check to see if it should use the old block face methods.
     *
     * @return whether it should use the old BlockFace methods.
     */
    public boolean useOldBlockFace() {

        if (useOldBlockFace == null) {
            Location loc = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
            useOldBlockFace = loc.getBlock().getRelative(BlockFace.WEST).getX() == 0;
        }

        return useOldBlockFace;
    }

    @Override
    public File getFile() {

        return super.getFile();
    }

    public static String getStackTrace(Throwable ex) {

        Writer out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        ex.printStackTrace(pw);
        return out.toString();
    }

    public static boolean isDebugFlagEnabled(String flag) {

        if(inst() == null) return false;

        if(!inst().getConfiguration().debugMode || inst().getConfiguration().debugFlags == null || inst().getConfiguration().debugFlags.isEmpty())
            return false;

        String[] flagBits = RegexUtil.PERIOD_PATTERN.split(flag);

        String tempFlag = "";

        for(int i = 0; i < flagBits.length; i++) {

            if(i == 0)
                tempFlag = flagBits[i];
            else
                tempFlag = tempFlag + "." + flagBits[i];

            for(String testflag : inst().getConfiguration().debugFlags) {

                if(testflag.toLowerCase(Locale.ENGLISH).equals(tempFlag))
                    return true;
            }
        }

        return false;
    }

    public static void logDebugMessage(String message, String code) {

        if(!isDebugFlagEnabled(code))
            return;

        logger().info("[Debug][" + code + "] " + message);
    }
}
