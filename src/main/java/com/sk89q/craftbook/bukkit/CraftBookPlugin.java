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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
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

import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.craftbook.CraftBookMechanic;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.Metrics.Graph;
import com.sk89q.craftbook.bukkit.Metrics.Plotter;
import com.sk89q.craftbook.bukkit.commands.CircuitCommands;
import com.sk89q.craftbook.bukkit.commands.MechanismCommands;
import com.sk89q.craftbook.bukkit.commands.TopLevelCommands;
import com.sk89q.craftbook.bukkit.commands.VehicleCommands;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.GlowStone;
import com.sk89q.craftbook.circuits.JackOLantern;
import com.sk89q.craftbook.circuits.Netherrack;
import com.sk89q.craftbook.circuits.RedstoneJukebox;
import com.sk89q.craftbook.circuits.ic.ICManager;
import com.sk89q.craftbook.circuits.ic.ICMechanic;
import com.sk89q.craftbook.circuits.pipe.Pipes;
import com.sk89q.craftbook.common.LanguageManager;
import com.sk89q.craftbook.common.UUIDMappings;
import com.sk89q.craftbook.common.st.MechanicClock;
import com.sk89q.craftbook.common.st.SelfTriggeringManager;
import com.sk89q.craftbook.common.variables.VariableManager;
import com.sk89q.craftbook.mech.AIMechanic;
import com.sk89q.craftbook.mech.Ammeter;
import com.sk89q.craftbook.mech.BetterLeads;
import com.sk89q.craftbook.mech.BetterPhysics;
import com.sk89q.craftbook.mech.BetterPistons;
import com.sk89q.craftbook.mech.BetterPlants;
import com.sk89q.craftbook.mech.Bookcase;
import com.sk89q.craftbook.mech.Chair;
import com.sk89q.craftbook.mech.ChunkAnchor;
import com.sk89q.craftbook.mech.CommandSigns;
import com.sk89q.craftbook.mech.CookingPot;
import com.sk89q.craftbook.mech.Elevator;
import com.sk89q.craftbook.mech.Footprints;
import com.sk89q.craftbook.mech.HeadDrops;
import com.sk89q.craftbook.mech.HiddenSwitch;
import com.sk89q.craftbook.mech.LightStone;
import com.sk89q.craftbook.mech.LightSwitch;
import com.sk89q.craftbook.mech.MapChanger;
import com.sk89q.craftbook.mech.Marquee;
import com.sk89q.craftbook.mech.PaintingSwitch;
import com.sk89q.craftbook.mech.Payment;
import com.sk89q.craftbook.mech.SignCopier;
import com.sk89q.craftbook.mech.Snow;
import com.sk89q.craftbook.mech.Sponge;
import com.sk89q.craftbook.mech.Teleporter;
import com.sk89q.craftbook.mech.TreeLopper;
import com.sk89q.craftbook.mech.XPStorer;
import com.sk89q.craftbook.mech.area.Area;
import com.sk89q.craftbook.mech.area.simple.Bridge;
import com.sk89q.craftbook.mech.area.simple.Door;
import com.sk89q.craftbook.mech.area.simple.Gate;
import com.sk89q.craftbook.mech.cauldron.ImprovedCauldron;
import com.sk89q.craftbook.mech.cauldron.legacy.Cauldron;
import com.sk89q.craftbook.mech.crafting.CustomCrafting;
import com.sk89q.craftbook.mech.dispenser.DispenserRecipes;
import com.sk89q.craftbook.mech.drops.CustomDrops;
import com.sk89q.craftbook.mech.drops.legacy.LegacyCustomDrops;
import com.sk89q.craftbook.mech.items.CommandItemDefinition;
import com.sk89q.craftbook.mech.items.CommandItems;
import com.sk89q.craftbook.util.CompatabilityUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.compat.companion.CompanionPlugins;
import com.sk89q.craftbook.util.persistent.PersistentStorage;
import com.sk89q.craftbook.vehicles.boat.Drops;
import com.sk89q.craftbook.vehicles.boat.ExitRemover;
import com.sk89q.craftbook.vehicles.boat.LandBoats;
import com.sk89q.craftbook.vehicles.boat.RemoveEntities;
import com.sk89q.craftbook.vehicles.boat.SpeedModifiers;
import com.sk89q.craftbook.vehicles.boat.Uncrashable;
import com.sk89q.craftbook.vehicles.boat.WaterPlaceOnly;
import com.sk89q.craftbook.vehicles.cart.CollisionEntry;
import com.sk89q.craftbook.vehicles.cart.ConstantSpeed;
import com.sk89q.craftbook.vehicles.cart.EmptyDecay;
import com.sk89q.craftbook.vehicles.cart.EmptySlowdown;
import com.sk89q.craftbook.vehicles.cart.FallModifier;
import com.sk89q.craftbook.vehicles.cart.ItemPickup;
import com.sk89q.craftbook.vehicles.cart.MobBlocker;
import com.sk89q.craftbook.vehicles.cart.MoreRails;
import com.sk89q.craftbook.vehicles.cart.NoCollide;
import com.sk89q.craftbook.vehicles.cart.PlaceAnywhere;
import com.sk89q.craftbook.vehicles.cart.RailPlacer;
import com.sk89q.craftbook.vehicles.cart.TemporaryCart;
import com.sk89q.craftbook.vehicles.cart.VisionSteering;
import com.sk89q.craftbook.vehicles.cart.blocks.CartBlockManager;
import com.sk89q.craftbook.vehicles.cart.blocks.CartBlockMechanism;
import com.sk89q.craftbook.vehicles.cart.blocks.CartBooster;
import com.sk89q.craftbook.vehicles.cart.blocks.CartDeposit;
import com.sk89q.craftbook.vehicles.cart.blocks.CartDispenser;
import com.sk89q.craftbook.vehicles.cart.blocks.CartEjector;
import com.sk89q.craftbook.vehicles.cart.blocks.CartLift;
import com.sk89q.craftbook.vehicles.cart.blocks.CartMaxSpeed;
import com.sk89q.craftbook.vehicles.cart.blocks.CartMessenger;
import com.sk89q.craftbook.vehicles.cart.blocks.CartReverser;
import com.sk89q.craftbook.vehicles.cart.blocks.CartSorter;
import com.sk89q.craftbook.vehicles.cart.blocks.CartStation;
import com.sk89q.craftbook.vehicles.cart.blocks.CartTeleporter;
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

public class CraftBookPlugin extends JavaPlugin {

    /**
     * Companion Plugins for CraftBook.
     */
    public static CompanionPlugins plugins;

    /**
     * The instance for CraftBook
     */
    private static CraftBookPlugin instance;

    /**
     * The language manager
     */
    private LanguageManager languageManager;

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

    /**
     * The adapter for events to the manager.
     */
    MechanicListenerAdapter managerAdapter;

    /**
     * The MechanicClock that manages all Self-Triggering Components.
     */
    private MechanicClock mechanicClock;

    /**
     * The persistent storage database of CraftBook.
     */
    protected PersistentStorage persistentStorage;

    /**
     * The UUID Mappings for CraftBook.
     */
    protected UUIDMappings uuidMappings;

    /**
     * List of common mechanics.
     */
    private List<CraftBookMechanic> mechanics;

    /**
     * The manager for SelfTriggering components.
     */
    private SelfTriggeringManager selfTriggerManager;

    /**
     * Construct objects. Actual loading occurs when the plugin is enabled, so
     * this merely instantiates the objects.
     */
    public CraftBookPlugin() {

        super();
        // Set the instance
        instance = this;
    }

    public static String getVersion() {

        return "3.8.9";
    }

    /**
     * Gets the build equivalent of the last stable version.
     * 
     * @return the build number
     */
    public static String getStableBuild() {

        return "3803";
    }

    public static int getUpdaterID() {

        return 31055;
    }

    public List<CraftBookMechanic> getMechanics() {

        return mechanics;
    }

    public boolean isMechanicEnabled(Class<? extends CraftBookMechanic> clazz) {

        for(CraftBookMechanic mech : mechanics) {
            if(mech.getClass().equals(clazz))
                return true;
        }

        return false;
    }

    public CraftBookMechanic getMechanic(Class<? extends CraftBookMechanic> clazz) {

        for(CraftBookMechanic mech : mechanics) {
            if(mech.getClass().equals(clazz))
                return mech;
        }

        return null;
    }

    /**
     * Retrieve the UUID Mappings system of CraftBook.
     * 
     * @return The UUID Mappings System.
     */
    public UUIDMappings getUUIDMappings() {

        return uuidMappings;
    }

    /**
     * Called on plugin enable.
     */
    @Override
    public void onEnable() {

        ItemSyntax.plugin = this;

        plugins = new CompanionPlugins();
        plugins.initiate(this);

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

        persistentStorage = PersistentStorage.createFromType(getConfiguration().persistentStorageType);

        if(persistentStorage != null)
            persistentStorage.open();

        uuidMappings = new UUIDMappings();
        uuidMappings.enable();

        logDebugMessage("Initializing Managers!", "startup");
        managerAdapter = new MechanicListenerAdapter();
        mechanicClock = new MechanicClock();
        selfTriggerManager = new SelfTriggeringManager();

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

        getServer().getPluginManager().registerEvents(new Listener() {

            /* Bukkit Bug Fixes */

            @EventHandler(priority = EventPriority.LOWEST)
            public void signChange(SignChangeEvent event) {
                for(int i = 0; i < event.getLines().length; i++) {
                    StringBuilder builder = new StringBuilder();
                    for (char c : event.getLine(i).toCharArray()) {
                        if (c < 0xF700 || c > 0xF747) {
                            builder.append(c);
                        }
                    }
                    String fixed = builder.toString();
                    if(!fixed.equals(event.getLine(i)))
                        event.setLine(i, fixed);
                }
            }

            /* Alerts */

            @EventHandler(priority = EventPriority.HIGH)
            public void playerJoin(PlayerJoinEvent event) {

                if(!event.getPlayer().isOp()) return;

                boolean foundAMech = false;

                for(CraftBookMechanic mech : getMechanics())
                    if(!(mech instanceof VariableManager) && !(mech instanceof CartBlockManager)) {
                        foundAMech = true;
                        break;
                    }

                if(!foundAMech) {
                    event.getPlayer().sendMessage(ChatColor.RED + "[CraftBook] Warning! You have no mechanics enabled, the plugin will appear to do nothing until a feature is enabled!");
                }
            }
        }, this);

        boolean foundAMech = false;

        for(CraftBookMechanic mech : getMechanics())
            if(!(mech instanceof VariableManager) && !(mech instanceof CartBlockManager)) {
                foundAMech = true;
                break;
            }

        if(!foundAMech) {
            Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                @Override
                public void run () {
                    getLogger().warning(ChatColor.RED + "Warning! You have no mechanics enabled, the plugin will appear to do nothing until a feature is enabled!");
                }
            }, 20L, 20*60*5);
        }
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
        languageManager = new LanguageManager();
        languageManager.init();

        getServer().getScheduler().runTask(this, new Runnable() {

            @Override
            public void run () {
                CompatabilityUtil.init();
            }
        });

        mechanics = new ArrayList<CraftBookMechanic>();

        // VariableStore
        if(config.variablesEnabled) mechanics.add(new VariableManager());

        // Mechanics
        if (config.enableMechanisms) {
            logDebugMessage("Initializing Mechanisms!", "startup.mechanisms");
            registerCommands(MechanismCommands.class);

            if (config.commandItemsEnabled) mechanics.add(new CommandItems());
            if (config.customCraftingEnabled) mechanics.add(new CustomCrafting());
            if (config.customDispensingEnabled) mechanics.add(new DispenserRecipes());
            if (config.snowEnable) mechanics.add(new Snow());
            if (config.customDropEnabled) {
                mechanics.add(new CustomDrops());
                mechanics.add(new LegacyCustomDrops());
            }
            if (config.aiEnabled) mechanics.add(new AIMechanic());
            if (config.paintingsEnabled) mechanics.add(new PaintingSwitch());
            if (config.physicsEnabled) mechanics.add(new BetterPhysics());
            if (config.headDropsEnabled) mechanics.add(new HeadDrops());
            if (config.leadsEnabled) mechanics.add(new BetterLeads());
            if (config.marqueeEnabled) mechanics.add(new Marquee());
            if (config.treeLopperEnabled) mechanics.add(new TreeLopper());
            if (config.mapChangerEnabled) mechanics.add(new MapChanger());
            if (config.xpStorerEnabled) mechanics.add(new XPStorer());
            if (config.lightstoneEnabled) mechanics.add(new LightStone());
            if (config.commandSignEnabled) mechanics.add(new CommandSigns());
            if (config.lightSwitchEnabled) mechanics.add(new LightSwitch());
            if (config.chunkAnchorEnabled) mechanics.add(new ChunkAnchor());
            if (config.ammeterEnabled) mechanics.add(new Ammeter());
            if (config.bookcaseEnabled) mechanics.add(new Bookcase());
            if (config.signCopyEnabled) mechanics.add(new SignCopier());
            if (config.bridgeEnabled) mechanics.add(new Bridge());
            if (config.doorEnabled) mechanics.add(new Door());
            if (config.hiddenSwitchEnabled) mechanics.add(new HiddenSwitch());
            if (config.elevatorEnabled) mechanics.add(new Elevator());
            if (config.teleporterEnabled) mechanics.add(new Teleporter());
            if (config.areaEnabled) mechanics.add(new Area());
            if (config.cauldronEnabled) mechanics.add(new ImprovedCauldron());
            if (config.legacyCauldronEnabled) mechanics.add(new Cauldron());
            if (config.gateEnabled) mechanics.add(new Gate());
            if (config.pistonsEnabled) mechanics.add(new BetterPistons());
            if (config.cookingPotEnabled) mechanics.add(new CookingPot());
            if (config.spongeEnabled) mechanics.add(new Sponge());
            if (config.betterPlantsEnabled) mechanics.add(new BetterPlants());

            if (config.chairEnabled) try {mechanics.add(new Chair()); } catch(Throwable e){getLogger().warning("Failed to initialize mechanic: Chairs. Make sure you have ProtocolLib!");}
            if (config.footprintsEnabled) try {mechanics.add(new Footprints()); } catch(Throwable e){getLogger().warning("Failed to initialize mechanic: Footprints. Make sure you have ProtocolLib!");}
            if (config.paymentEnabled) if(CraftBookPlugin.plugins.getEconomy() != null) mechanics.add(new Payment()); else getLogger().warning("Failed to initialize mechanic: Payment. Make sure you have Vault!");
        }

        // Circuits
        if (config.enableCircuits) {
            logDebugMessage("Initializing Circuits!", "startup.circuits");
            registerCommands(CircuitCommands.class);

            if (config.jukeboxEnabled) mechanics.add(new RedstoneJukebox());
            if (config.glowstoneEnabled) mechanics.add(new GlowStone());
            if (config.netherrackEnabled) mechanics.add(new Netherrack());
            if (config.pumpkinsEnabled) mechanics.add(new JackOLantern());
            if (config.pipesEnabled) mechanics.add(new Pipes());
            if (config.ICEnabled) {
                mechanics.add(new ICMechanic(new ICManager()));
                ICManager.inst().enable();
            }
        }

        // Vehicles
        if (config.enableVehicles) {
            logDebugMessage("Initializing Vehicles!", "startup.vehicles");
            registerCommands(VehicleCommands.class);

            mechanics.add(new CartBlockManager());

            if(config.minecartSpeedModEnabled) {
                CartBlockManager.inst().addMechanic(new CartBooster(config.minecartSpeedModMaxBoostBlock, 100));
                CartBlockManager.inst().addMechanic(new CartBooster(config.minecartSpeedMod25xBoostBlock, 1.25));
                CartBlockManager.inst().addMechanic(new CartBooster(config.minecartSpeedMod20xSlowBlock, 0.8));
                CartBlockManager.inst().addMechanic(new CartBooster(config.minecartSpeedMod50xSlowBlock, 0.5));
            }
            if(config.minecartReverseEnabled) CartBlockManager.inst().addMechanic(new CartReverser(config.minecartReverseBlock));
            if(config.minecartSorterEnabled) CartBlockManager.inst().addMechanic(new CartSorter(config.minecartSorterBlock));
            if(config.minecartStationEnabled) CartBlockManager.inst().addMechanic(new CartStation(config.minecartStationBlock));
            if(config.minecartEjectorEnabled) CartBlockManager.inst().addMechanic(new CartEjector(config.minecartEjectorBlock));
            if(config.minecartDepositEnabled) CartBlockManager.inst().addMechanic(new CartDeposit(config.minecartDepositBlock));
            if(config.minecartTeleportEnabled) CartBlockManager.inst().addMechanic(new CartTeleporter(config.minecartTeleportBlock));
            if(config.minecartElevatorEnabled) CartBlockManager.inst().addMechanic(new CartLift(config.minecartElevatorBlock));
            if(config.minecartDispenserEnabled) CartBlockManager.inst().addMechanic(new CartDispenser(config.minecartDispenserBlock));
            if(config.minecartMessagerEnabled) CartBlockManager.inst().addMechanic(new CartMessenger(config.minecartMessagerBlock));
            if(config.minecartMaxSpeedEnabled) CartBlockManager.inst().addMechanic(new CartMaxSpeed(config.minecartMaxSpeedBlock));

            for(CartBlockMechanism mech : CartBlockManager.inst().getMechanics()) mechanics.add(mech);

            if(config.minecartMoreRailsEnabled) mechanics.add(new MoreRails());
            if(config.minecartRemoveEntitiesEnabled) mechanics.add(new com.sk89q.craftbook.vehicles.cart.RemoveEntities());
            if(config.minecartVisionSteeringEnabled) mechanics.add(new VisionSteering());
            if(config.minecartDecayEnabled) mechanics.add(new EmptyDecay());
            if(config.minecartBlockMobEntryEnabled) mechanics.add(new MobBlocker());
            if(config.minecartRemoveOnExitEnabled) mechanics.add(new com.sk89q.craftbook.vehicles.cart.ExitRemover());
            if(config.minecartCollisionEntryEnabled) mechanics.add(new CollisionEntry());
            if(config.minecartItemPickupEnabled) mechanics.add(new ItemPickup());
            if(config.minecartFallModifierEnabled) mechanics.add(new FallModifier());
            if(config.minecartConstantSpeedEnable) mechanics.add(new ConstantSpeed());
            if(config.minecartRailPlacerEnable) mechanics.add(new RailPlacer());
            if(config.minecartSpeedModifierEnable) mechanics.add(new com.sk89q.craftbook.vehicles.cart.SpeedModifiers());
            if(config.minecartEmptySlowdownStopperEnable) mechanics.add(new EmptySlowdown());
            if(config.minecartNoCollideEnable) mechanics.add(new NoCollide());
            if(config.minecartPlaceAnywhereEnable) mechanics.add(new PlaceAnywhere());
            if(config.minecartTemporaryCartEnable) mechanics.add(new TemporaryCart());

            if(config.boatRemoveEntitiesEnabled) mechanics.add(new RemoveEntities());
            if(config.boatNoCrashEnabled) mechanics.add(new Uncrashable());
            if(config.boatDropsEnabled) mechanics.add(new Drops());
            if(config.boatSpeedModifierEnable) mechanics.add(new SpeedModifiers());
            if(config.boatLandBoatsEnable) mechanics.add(new LandBoats());
            if(config.boatRemoveOnExitEnabled) mechanics.add(new ExitRemover());
            if(config.boatWaterPlaceOnly) mechanics.add(new WaterPlaceOnly());
        }

        Iterator<CraftBookMechanic> iter = mechanics.iterator();
        while(iter.hasNext()) {
            CraftBookMechanic mech = iter.next();
            try {
                if(!mech.enable()) {
                    getLogger().warning("Failed to initialize mechanic: " + mech.getClass().getSimpleName());
                    mech.disable();
                    iter.remove();
                    continue;
                }
                getServer().getPluginManager().registerEvents(mech, this);
            } catch(Throwable t) {
                getLogger().log(Level.WARNING, "Failed to initialize mechanic: " + mech.getClass().getSimpleName(), t);
            }
        }

        setupSelfTriggered();
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
                        getLogger().info("Happy " + formatDate(date.get(Calendar.YEAR) - 2012) + " reddit cakeday me4502!");
                    else if(date.get(Calendar.MONTH) == Calendar.OCTOBER && date.get(Calendar.DAY_OF_MONTH) == 16) //Me4502 birthday
                        getLogger().info("Happy birthday me4502!");
                    else if(date.get(Calendar.MONTH) == Calendar.JANUARY && date.get(Calendar.DAY_OF_MONTH) == 1) //New Years
                        getLogger().info("Happy new years! Happy " + date.get(Calendar.YEAR) + "!!!");
                    else if(date.get(Calendar.MONTH) == Calendar.OCTOBER && date.get(Calendar.DAY_OF_MONTH) == 22) //CraftBook birthday
                        getLogger().info("Happy " + formatDate(date.get(Calendar.YEAR) - 2010) + " birthday CraftBook!");
                    else if(date.get(Calendar.MONTH) == Calendar.APRIL && date.get(Calendar.DAY_OF_MONTH) == 24) //Me4502ian CraftBook birthday
                        getLogger().info("CraftBook has been under Me4502's 'harsh dictatorship :P' for " + (date.get(Calendar.YEAR) - 2012) + " year(s) today!");
                }

                private String formatDate(int date) {
                    if (String.valueOf(date).endsWith("1"))
                        return date + "st";
                    else if (String.valueOf(date).endsWith("2"))
                        return date + "nd";
                    else if (String.valueOf(date).endsWith("3"))
                        return date + "rd";
                    else
                        return date + "th";
                }
            }, 20L);
        }

        try {
            logDebugMessage("Initializing Metrics!", "startup");
            Metrics metrics = new Metrics(this);
            metrics.start();

            Graph languagesGraph = metrics.createGraph("Language");
            for (String language : languageManager.getLanguages()) {
                languagesGraph.addPlotter(new Plotter(language) {

                    @Override
                    public int getValue () {
                        return 1;
                    }
                });
            }
            languagesGraph.addPlotter(new Plotter("Total") {

                @Override
                public int getValue () {
                    return languageManager.getLanguages().size();
                }
            });

            Graph componentsGraph = metrics.createGraph("Components");
            if (config.enableCircuits)
                componentsGraph.addPlotter(new Plotter("Circuits") {

                    @Override
                    public int getValue () {
                        return 1;
                    }
                });
            if (config.enableMechanisms)
                componentsGraph.addPlotter(new Plotter("Mechanisms") {

                    @Override
                    public int getValue () {
                        return 1;
                    }
                });
            if (config.enableVehicles)
                componentsGraph.addPlotter(new Plotter("Vehicles") {

                    @Override
                    public int getValue () {
                        return 1;
                    }
                });

            Graph mechanicsGraph = metrics.createGraph("Enabled Mechanics");
            for(CraftBookMechanic mech : getMechanics()) {
                mechanicsGraph.addPlotter(new Plotter(mech.getClass().getSimpleName()) {
                    @Override
                    public int getValue () {
                        return 1;
                    }
                });
            }
        } catch (Throwable e1) {
            BukkitUtil.printStacktrace(e1);
        }
    }

    public void checkForUpdates() {

        boolean exempt = false;

        try {
            int ver = Integer.parseInt(getDescription().getVersion().split(":")[1].split("-")[0]);
            if (ver < 1541) //Not valid prior to this version.
                exempt = true;
        }
        catch(Exception e) {
            exempt = true;
        }

        if(!exempt) {
            final Updater updater = new Updater(this, getUpdaterID(), getFile(), Updater.UpdateType.NO_DOWNLOAD, true); // Start Updater but just do a version check
            updateAvailable = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE; // Determine if there is an update ready for us
            latestVersion = updater.getLatestName();
            getLogger().info(latestVersion + " is the latest version available, and the updatability of it is: " + updater.getResult().name() + ". You currently have version " + getLatestVersion() + " installed.");

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

    /**
     * Called on plugin disable.
     */
    @Override
    public void onDisable() {

        languageManager.close();
        for(CraftBookMechanic mech : mechanics)
            mech.disable();
        mechanics = null;

        if(hasPersistentStorage())
            getPersistentStorage().close();

        uuidMappings.disable();
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
     * Setup the required components of self-triggered Mechanics.
     */
    private void setupSelfTriggered() {

        getLogger().info("Enumerating chunks for self-triggered components...");

        long start = System.currentTimeMillis();
        int numWorlds = 0;
        int numChunks = 0;

        for (World world : getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                getSelfTriggerManager().registerSelfTrigger(chunk);
                numChunks++;
            }

            numWorlds++;
        }

        long time = System.currentTimeMillis() - start;

        getLogger().info(numChunks + " chunk(s) for " + numWorlds + " world(s) processed " + "(" + time + "ms elapsed)");

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
     * Grabs the manager for self triggered components.
     */
    public SelfTriggeringManager getSelfTriggerManager() {

        return selfTriggerManager;
    }

    /**
     * Reload configuration
     */
    public void reloadConfiguration() throws Throwable {

        if(mechanics != null)
            for(CraftBookMechanic mech : mechanics)
                mech.disable();
        mechanics = null;
        getServer().getScheduler().cancelTasks(inst());
        HandlerList.unregisterAll(inst());
        config.load();
        managerAdapter = new MechanicListenerAdapter();
        mechanicClock = new MechanicClock();
        setupCraftBook();
        registerGlobalEvents();
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

    public boolean hasPersistentStorage() {

        return persistentStorage != null && persistentStorage.isValid();
    }

    public PersistentStorage getPersistentStorage() {

        return persistentStorage;
    }

    public void setPersistentStorage(PersistentStorage storage) {

        persistentStorage = storage;
        getConfiguration().persistentStorageType = storage.getType();
        getConfiguration().config.setProperty("persistent-storage-type", storage.getType());
        getConfiguration().config.save();
    }

    /**
     * Parses more advanced portions of the Item Syntax.
     * 
     * @param item The item to parse
     * @return The parsed string. (Can be the same, and should be if nothing found)
     */
    public String parseItemSyntax(String item) {

        if(CommandItems.INSTANCE != null)  {
            CommandItemDefinition def = CommandItems.INSTANCE.getDefinitionByName(item);
            if(def != null) {
                return ItemSyntax.getStringFromItem(def.getItem());
            }
        }
        return item;
    }

    public static String getWikiDomain() {
        return "http://wiki.sk89q.com/wiki/CraftBook";
    }
}