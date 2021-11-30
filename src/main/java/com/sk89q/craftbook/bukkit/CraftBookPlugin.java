package com.sk89q.craftbook.bukkit;

import com.google.common.collect.Sets;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.craftbook.CraftBookMechanic;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.commands.TopLevelCommands;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.core.LanguageManager;
import com.sk89q.craftbook.core.st.MechanicClock;
import com.sk89q.craftbook.core.st.SelfTriggeringManager;
import com.sk89q.craftbook.mechanics.AIMechanic;
import com.sk89q.craftbook.mechanics.Ammeter;
import com.sk89q.craftbook.mechanics.BetterLeads;
import com.sk89q.craftbook.mechanics.BetterPhysics;
import com.sk89q.craftbook.mechanics.BetterPistons;
import com.sk89q.craftbook.mechanics.BetterPlants;
import com.sk89q.craftbook.mechanics.Bookcase;
import com.sk89q.craftbook.mechanics.BounceBlocks;
import com.sk89q.craftbook.mechanics.Chair;
import com.sk89q.craftbook.mechanics.ChunkAnchor;
import com.sk89q.craftbook.mechanics.CommandSigns;
import com.sk89q.craftbook.mechanics.CookingPot;
import com.sk89q.craftbook.mechanics.Elevator;
import com.sk89q.craftbook.mechanics.Footprints;
import com.sk89q.craftbook.mechanics.GlowStone;
import com.sk89q.craftbook.mechanics.HiddenSwitch;
import com.sk89q.craftbook.mechanics.JackOLantern;
import com.sk89q.craftbook.mechanics.LightStone;
import com.sk89q.craftbook.mechanics.LightSwitch;
import com.sk89q.craftbook.mechanics.MapChanger;
import com.sk89q.craftbook.mechanics.Marquee;
import com.sk89q.craftbook.mechanics.Netherrack;
import com.sk89q.craftbook.mechanics.PaintingSwitch;
import com.sk89q.craftbook.mechanics.Payment;
import com.sk89q.craftbook.mechanics.RedstoneJukebox;
import com.sk89q.craftbook.mechanics.Snow;
import com.sk89q.craftbook.mechanics.Sponge;
import com.sk89q.craftbook.mechanics.Teleporter;
import com.sk89q.craftbook.mechanics.TreeLopper;
import com.sk89q.craftbook.mechanics.XPStorer;
import com.sk89q.craftbook.mechanics.area.Area;
import com.sk89q.craftbook.mechanics.area.simple.Bridge;
import com.sk89q.craftbook.mechanics.area.simple.Door;
import com.sk89q.craftbook.mechanics.area.simple.Gate;
import com.sk89q.craftbook.mechanics.boat.Drops;
import com.sk89q.craftbook.mechanics.boat.LandBoats;
import com.sk89q.craftbook.mechanics.boat.Uncrashable;
import com.sk89q.craftbook.mechanics.boat.WaterPlaceOnly;
import com.sk89q.craftbook.mechanics.cauldron.ImprovedCauldron;
import com.sk89q.craftbook.mechanics.cauldron.legacy.Cauldron;
import com.sk89q.craftbook.mechanics.crafting.CustomCrafting;
import com.sk89q.craftbook.mechanics.dispenser.DispenserRecipes;
import com.sk89q.craftbook.mechanics.drops.CustomDrops;
import com.sk89q.craftbook.mechanics.drops.legacy.LegacyCustomDrops;
import com.sk89q.craftbook.mechanics.headdrops.HeadDrops;
import com.sk89q.craftbook.mechanics.ic.ICMechanic;
import com.sk89q.craftbook.mechanics.items.CommandItemDefinition;
import com.sk89q.craftbook.mechanics.items.CommandItems;
import com.sk89q.craftbook.mechanics.minecart.CollisionEntry;
import com.sk89q.craftbook.mechanics.minecart.ConstantSpeed;
import com.sk89q.craftbook.mechanics.minecart.EmptyDecay;
import com.sk89q.craftbook.mechanics.minecart.EmptySlowdown;
import com.sk89q.craftbook.mechanics.minecart.FallModifier;
import com.sk89q.craftbook.mechanics.minecart.ItemPickup;
import com.sk89q.craftbook.mechanics.minecart.MobBlocker;
import com.sk89q.craftbook.mechanics.minecart.MoreRails;
import com.sk89q.craftbook.mechanics.minecart.NoCollide;
import com.sk89q.craftbook.mechanics.minecart.PlaceAnywhere;
import com.sk89q.craftbook.mechanics.minecart.RailPlacer;
import com.sk89q.craftbook.mechanics.minecart.TemporaryCart;
import com.sk89q.craftbook.mechanics.minecart.VisionSteering;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartBlockMechanism;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartBooster;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartDeposit;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartDispenser;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartEjector;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartLift;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartMaxSpeed;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartMessenger;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartReverser;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartSorter;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartStation;
import com.sk89q.craftbook.mechanics.minecart.blocks.CartTeleporter;
import com.sk89q.craftbook.mechanics.pipe.Pipes;
import com.sk89q.craftbook.mechanics.signcopier.SignCopier;
import com.sk89q.craftbook.mechanics.variables.VariableManager;
import com.sk89q.craftbook.util.ArrayUtil;
import com.sk89q.craftbook.util.CompatabilityUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.UUIDMappings;
import com.sk89q.craftbook.util.compat.companion.CompanionPlugins;
import com.sk89q.craftbook.util.compat.nms.NMSAdapter;
import com.sk89q.craftbook.util.persistent.PersistentStorage;
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
import io.papermc.lib.PaperLib;
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
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import javax.annotation.Nullable;

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
     * List of common mechanics.
     */
    private List<CraftBookMechanic> mechanics;

    /**
     * The manager for SelfTriggering components.
     */
    private SelfTriggeringManager selfTriggerManager;

    /**
     * The NMS Adapter.
     */
    private NMSAdapter nmsAdapter;

    public static final Map<String, Class<? extends CraftBookMechanic>> availableMechanics;

    public boolean useLegacyCartSystem = false;

    static {
        availableMechanics = new TreeMap<>();

        availableMechanics.put("Variables", VariableManager.class);
        availableMechanics.put("CommandItems", CommandItems.class);
        availableMechanics.put("CustomCrafting", CustomCrafting.class);
        availableMechanics.put("DispenserRecipes", DispenserRecipes.class);
        availableMechanics.put("Snow", Snow.class);
        availableMechanics.put("CustomDrops", CustomDrops.class);
        availableMechanics.put("LegacyCustomDrops", LegacyCustomDrops.class);
        availableMechanics.put("AI", AIMechanic.class);
        availableMechanics.put("PaintingSwitcher", PaintingSwitch.class);
        availableMechanics.put("BetterPhysics", BetterPhysics.class);
        availableMechanics.put("HeadDrops", HeadDrops.class);
        availableMechanics.put("BetterLeads", BetterLeads.class);
        availableMechanics.put("Marquee", Marquee.class);
        availableMechanics.put("TreeLopper", TreeLopper.class);
        availableMechanics.put("MapChanger", MapChanger.class);
        availableMechanics.put("XPStorer", XPStorer.class);
        availableMechanics.put("LightStone", LightStone.class);
        availableMechanics.put("CommandSigns", CommandSigns.class);
        availableMechanics.put("LightSwitch", LightSwitch.class);
        availableMechanics.put("ChunkAnchor", ChunkAnchor.class);
        availableMechanics.put("Ammeter", Ammeter.class);
        availableMechanics.put("HiddenSwitch", HiddenSwitch.class);
        availableMechanics.put("Bookcase", Bookcase.class);
        availableMechanics.put("SignCopier", SignCopier.class);
        availableMechanics.put("Bridge", Bridge.class);
        availableMechanics.put("Door", Door.class);
        availableMechanics.put("Elevator", Elevator.class);
        availableMechanics.put("Teleporter", Teleporter.class);
        availableMechanics.put("ToggleArea", Area.class);
        availableMechanics.put("Cauldron", ImprovedCauldron.class);
        availableMechanics.put("LegacyCauldron", Cauldron.class);
        availableMechanics.put("Gate", Gate.class);
        availableMechanics.put("BetterPistons", BetterPistons.class);
        availableMechanics.put("CookingPot", CookingPot.class);
        availableMechanics.put("Sponge", Sponge.class);
        availableMechanics.put("BetterPlants", BetterPlants.class);
        availableMechanics.put("Chairs", Chair.class);
        availableMechanics.put("Footprints", Footprints.class);
        availableMechanics.put("Pay", Payment.class);
        availableMechanics.put("Jukebox", RedstoneJukebox.class);
        availableMechanics.put("Glowstone", GlowStone.class);
        availableMechanics.put("Netherrack", Netherrack.class);
        availableMechanics.put("JackOLantern", JackOLantern.class);
        availableMechanics.put("Pipes", Pipes.class);
        availableMechanics.put("BounceBlocks", BounceBlocks.class);
        availableMechanics.put("ICs", ICMechanic.class);
        availableMechanics.put("MinecartBooster", CartBooster.class);
        availableMechanics.put("MinecartReverser", CartReverser.class);
        availableMechanics.put("MinecartSorter", CartSorter.class);
        availableMechanics.put("MinecartStation", CartStation.class);
        availableMechanics.put("MinecartEjector", CartEjector.class);
        availableMechanics.put("MinecartDeposit", CartDeposit.class);
        availableMechanics.put("MinecartTeleporter", CartTeleporter.class);
        availableMechanics.put("MinecartElevator", CartLift.class);
        availableMechanics.put("MinecartDispenser", CartDispenser.class);
        availableMechanics.put("MinecartMessenger", CartMessenger.class);
        availableMechanics.put("MinecartMaxSpeed", CartMaxSpeed.class);
        availableMechanics.put("MinecartMoreRails", MoreRails.class);
        availableMechanics.put("MinecartRemoveEntities", com.sk89q.craftbook.mechanics.minecart.RemoveEntities.class);
        availableMechanics.put("MinecartVisionSteering", VisionSteering.class);
        availableMechanics.put("MinecartDecay", EmptyDecay.class);
        availableMechanics.put("MinecartMobBlocker", MobBlocker.class);
        availableMechanics.put("MinecartExitRemover", com.sk89q.craftbook.mechanics.minecart.ExitRemover.class);
        availableMechanics.put("MinecartCollisionEntry", CollisionEntry.class);
        availableMechanics.put("MinecartItemPickup", ItemPickup.class);
        availableMechanics.put("MinecartFallModifier", FallModifier.class);
        availableMechanics.put("MinecartConstantSpeed", ConstantSpeed.class);
        availableMechanics.put("MinecartRailPlacer", RailPlacer.class);
        availableMechanics.put("MinecartSpeedModifiers", com.sk89q.craftbook.mechanics.minecart.SpeedModifiers.class);
        availableMechanics.put("MinecartEmptySlowdown", EmptySlowdown.class);
        availableMechanics.put("MinecartNoCollide", NoCollide.class);
        availableMechanics.put("MinecartPlaceAnywhere", PlaceAnywhere.class);
        availableMechanics.put("MinecartTemporaryCart", TemporaryCart.class);
        availableMechanics.put("BoatRemoveEntities", com.sk89q.craftbook.mechanics.boat.RemoveEntities.class);
        availableMechanics.put("BoatUncrashable", Uncrashable.class);
        availableMechanics.put("BoatDrops", Drops.class);
        availableMechanics.put("BoatDecay", com.sk89q.craftbook.mechanics.boat.EmptyDecay.class);
        availableMechanics.put("BoatSpeedModifiers", com.sk89q.craftbook.mechanics.boat.SpeedModifiers.class);
        availableMechanics.put("LandBoats", LandBoats.class);
        availableMechanics.put("BoatExitRemover", com.sk89q.craftbook.mechanics.boat.ExitRemover.class);
        availableMechanics.put("BoatWaterPlaceOnly", WaterPlaceOnly.class);
    }

    /**
     * Construct objects. Actual loading occurs when the plugin is enabled, so
     * this merely instantiates the objects.
     */
    public CraftBookPlugin() {

        super();
        // Set the instance
        instance = this;
    }

    @Nullable
    public static String getVersion() {
        return null;
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
     * Retrieve the NMS Adapter.
     *
     * <p>
     *     Note: This may not actually be using NMS.
     * </p>
     *
     * @return The NMS Adapter
     */
    public NMSAdapter getNmsAdapter() {
        return this.nmsAdapter;
    }

    /**
     * Sets the NMS Adapter.
     *
     * @param nmsAdapter The NMS Adapter
     */
    public void setNmsAdapter(NMSAdapter nmsAdapter) {
        this.nmsAdapter = nmsAdapter;
    }

    /**
     * Called on plugin enable.
     */
    @Override
    public void onEnable() {

        ItemSyntax.plugin = this;

        nmsAdapter = new NMSAdapter();

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

        persistentStorage = PersistentStorage.createFromType(config.persistentStorageType);

        if(persistentStorage != null)
            persistentStorage.open();

        uuidMappings = new UUIDMappings();
        uuidMappings.enable();

        logDebugMessage("Initializing Managers!", "startup");
        managerAdapter = new MechanicListenerAdapter();

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
            }

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
                    if(!(mech instanceof VariableManager)) {
                        foundAMech = true;
                        break;
                    }

                if(!foundAMech) {
                    event.getPlayer().sendMessage(ChatColor.RED + "[CraftBook] Warning! You have no mechanics enabled, the plugin will appear to do nothing until a feature is enabled!");
                }
            }
        }, this);

        boolean foundAMech = false;

        for(CraftBookMechanic mech : mechanics)
            if(!(mech instanceof VariableManager)) {
                foundAMech = true;
                break;
            }

        if(!foundAMech) {
            Bukkit.getScheduler().runTaskTimer(this,
                    () -> getLogger().warning(ChatColor.RED + "Warning! You have no mechanics enabled, the plugin will appear to do nothing until a feature is enabled!"), 20L, 20*60*5);
        }

        PaperLib.suggestPaper(this);
    }

    private YAMLProcessor mechanismsConfig;

    /**
     * Register basic things to the plugin. For example, languages.
     */
    public void setupCraftBook() {

        if(config.debugLogToFile) {
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

        getServer().getScheduler().runTask(this, CompatabilityUtil::init);

        mechanics = new ArrayList<>();

        logDebugMessage("Initializing Mechanisms!", "startup");

        createDefaultConfiguration(new File(getDataFolder(), "mechanisms.yml"), "mechanisms.yml");
        mechanismsConfig = new YAMLProcessor(new File(getDataFolder(), "mechanisms.yml"), true, YAMLFormat.EXTENDED);

        try {
            mechanismsConfig.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mechanismsConfig.setWriteDefaults(true);

        mechanismsConfig.setHeader(
                "# CraftBook Mechanism Configuration. Generated for version: " + (CraftBookPlugin.inst() == null ? CraftBookPlugin.getVersion() : CraftBookPlugin.inst().getDescription().getVersion()),
                "# This configuration will automatically add new configuration options for you,",
                "# So there is no need to regenerate this configuration unless you need to.",
                "# More information about these features are available at...",
                "# " + CraftBookPlugin.getWikiDomain() + "/Usage",
                "#",
                "# NOTE! MAKE SURE TO ENABLE FEATURES IN THE config.yml FILE!",
                "");

        for(String enabled : Sets.newHashSet(config.enabledMechanics)) {

            Class<? extends CraftBookMechanic> mechClass = availableMechanics.get(enabled);
            try {
                if(mechClass != null) {

                    CraftBookMechanic mech = mechClass.newInstance();
                    mech.loadConfiguration(mechanismsConfig, "mechanics." + enabled + '.');
                    mechanics.add(mech);
                }
            } catch (Throwable t) {
                getLogger().log(Level.WARNING, "Failed to load mechanic: " + enabled, t);
            }
        }

        mechanismsConfig.save();

        boolean hasSTMechanic = false;

        Iterator<CraftBookMechanic> iter = mechanics.iterator();
        while(iter.hasNext()) {
            CraftBookMechanic mech = iter.next();
            try {
                if(!mech.enable()) {
                    getLogger().warning("Failed to enable mechanic: " + mech.getClass().getSimpleName());
                    mech.disable();
                    iter.remove();
                    continue;
                }
                getServer().getPluginManager().registerEvents(mech, this);
                if(mech instanceof CookingPot || mech instanceof XPStorer || (mech instanceof ICMechanic && !((ICMechanic) mech).disableSelfTriggered)) {
                    //TODO make this a better check.
                    hasSTMechanic = true;
                }
                if(mech instanceof CartBlockMechanism)
                    useLegacyCartSystem = true;
            } catch(Throwable t) {
                getLogger().log(Level.WARNING, "Failed to enable mechanic: " + mech.getClass().getSimpleName(), t);
            }
        }

        if(hasSTMechanic)
            setupSelfTriggered();
    }

    /**
     * Enables the mechanic with the specified name.
     * 
     * @param mechanic The name of the mechanic.
     * @return If the mechanic could be found and enabled.
     */
    public boolean enableMechanic(String mechanic) {

        try {
            mechanismsConfig.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mechanismsConfig.setHeader(
                "# CraftBook Mechanism Configuration. Generated for version: " + (CraftBookPlugin.inst() == null ? CraftBookPlugin.getVersion() : CraftBookPlugin.inst().getDescription().getVersion()),
                "# This configuration will automatically add new configuration options for you,",
                "# So there is no need to regenerate this configuration unless you need to.",
                "# More information about these features are available at...",
                "# " + CraftBookPlugin.getWikiDomain() + "/Usage",
                "#",
                "# NOTE! MAKE SURE TO ENABLE FEATURES IN THE config.yml FILE!",
                "");

        Class<? extends CraftBookMechanic> mechClass = availableMechanics.get(mechanic);
        try {
            if(mechClass != null) {

                CraftBookMechanic mech = mechClass.newInstance();
                mech.loadConfiguration(mechanismsConfig, "mechanics." + mechanic + '.');
                mechanics.add(mech);

                if(!mech.enable()) {
                    getLogger().warning("Failed to enable mechanic: " + mech.getClass().getSimpleName());
                    mech.disable();
                    return false;
                }
                getServer().getPluginManager().registerEvents(mech, this);
            } else
                return false;
        } catch (Throwable t) {
            getLogger().log(Level.WARNING, "Failed to load mechanic: " + mechanic, t);
            return false;
        }

        mechanismsConfig.save();
        config.save();

        return true;
    }

    /**
     * Disables the mechanic with the specified name.
     * 
     * @param mechanic The name of the mechanic.
     * @return If the mechanic could be found and disabled.
     */
    public boolean disableMechanic(String mechanic) {

        Class<? extends CraftBookMechanic> mechClass = availableMechanics.get(mechanic);

        if(mechClass == null) return false;

        boolean found = false;

        for(CraftBookMechanic mech : mechanics) {
            if(mech.getClass().equals(mechClass)) {
                found = true;
                break;
            }
        }

        if(!found) return false;

        config.enabledMechanics.remove(mechanic);
        config.save();

        return true;
    }

    /**
     * Registers events used by the main CraftBook plugin. Also registers PluginMetrics
     */
    public void registerGlobalEvents() {

        logDebugMessage("Registring managers!", "startup");
        getServer().getPluginManager().registerEvents(managerAdapter, inst());

        if(config.easterEggs) {
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
            org.bstats.bukkit.Metrics metrics = new org.bstats.bukkit.Metrics(this, 3319);

            metrics.addCustomChart(new org.bstats.charts.AdvancedPie("language",
                    () -> languageManager.getLanguages().stream().collect(Collectors.toMap(Function.identity(), o -> 1))));
            metrics.addCustomChart(new org.bstats.charts.SimpleBarChart("enabled_mechanics",
                    () -> mechanics.stream().collect(Collectors.toMap(mech -> mech.getClass().getSimpleName(), o -> 1))));
        } catch (Throwable e1) {
            CraftBookBukkitUtil.printStacktrace(e1);
        }
    }

    /**
     * Called on plugin disable.
     */
    @Override
    public void onDisable() {

        if(languageManager != null)
            languageManager.close();
        if(mechanics != null) {
            for(CraftBookMechanic mech : mechanics)
                mech.disable();
            mechanics = null;
        }

        if(hasPersistentStorage()) {

            persistentStorage.close();
        }

        if(uuidMappings != null)
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

        mechanicClock = new MechanicClock();
        selfTriggerManager = new SelfTriggeringManager();

        getLogger().info("Enumerating chunks for self-triggered components...");

        long start = System.currentTimeMillis();
        int numWorlds = 0;
        int numChunks = 0;

        for (World world : getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {

                selfTriggerManager.registerSelfTrigger(chunk);
                numChunks++;
            }

            numWorlds++;
        }

        long time = System.currentTimeMillis() - start;

        getLogger().info(numChunks + " chunk(s) for " + numWorlds + " world(s) processed " + "(" + time + "ms elapsed)");

        // Set up the clock for self-triggered ICs.

        getServer().getScheduler().runTaskTimer(this, mechanicClock, 0, config.stThinkRate);

        getServer().getPluginManager().registerEvents(selfTriggerManager, this);
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

    public YAMLProcessor getMechanismsConfig() {
        return this.mechanismsConfig;
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
            return ThreadLocalRandom.current(); // If none is set, use a thread local random.
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

                if (!config.noOpPermissions) return true;
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
    public static Player checkPlayer(CommandSender sender)
            throws CommandException {

        if (sender instanceof Player) {
            return (Player) sender;
        } else {
            throw new CommandException("A player is expected.");
        }
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
     * @param actual      The destination file
     * @param defaultName The name of the file inside the jar's defaults folder
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

        if(!inst().config.debugMode || inst().config.debugFlags == null || inst().config.debugFlags.isEmpty())
            return false;

        String[] flagBits = RegexUtil.PERIOD_PATTERN.split(flag);

        String tempFlag = "";

        for(int i = 0; i < flagBits.length; i++) {

            if(i == 0)
                tempFlag = flagBits[i];
            else
                tempFlag = tempFlag + "." + flagBits[i];

            for(String testflag : inst().config.debugFlags) {

                if(testflag.toLowerCase(Locale.ENGLISH).equals(tempFlag))
                    return true;
            }
        }

        return false;
    }

    private static PrintWriter debugLogger;

    public static void logDebugMessage(String message, String code) {

        if(!isDebugFlagEnabled(code))
            return;

        logger().info("[Debug][" + code + "] " + message);

        if(CraftBookPlugin.inst().config.debugLogToFile)
            debugLogger.println("[" + code + "] " + message);
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

    /**
     * Parses more advanced portions of the Item Syntax.
     * 
     * @param item The item to parse
     * @return The parsed string. (Can be the same, and should be if nothing found)
     */
    @SuppressWarnings({"MethodMayBeStatic", "unused"})
    public final String parseItemSyntax(String item) {

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