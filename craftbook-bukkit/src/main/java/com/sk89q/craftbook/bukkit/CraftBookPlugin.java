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

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.google.common.collect.ImmutableList;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.craftbook.CraftBookManifest;
import com.sk89q.craftbook.CraftBookMechanic;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.commands.TopLevelCommands;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.core.LanguageManager;
import com.sk89q.craftbook.core.mechanic.MechanicCategory;
import com.sk89q.craftbook.core.mechanic.MechanicType;
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
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.UUIDMappings;
import com.sk89q.craftbook.util.companion.CompanionPlugins;
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
import com.sk89q.worldedit.bukkit.BukkitCommandSender;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.task.SimpleSupervisor;
import com.sk89q.worldedit.util.task.Supervisor;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
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
    private static String version;

    /**
     * The language manager
     */
    private LanguageManager languageManager;

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
    private List<CraftBookMechanic> loadedMechanics;

    /**
     * The manager for SelfTriggering components.
     */
    private SelfTriggeringManager selfTriggerManager;

    /**
     * The Supervisor for CraftBook tasks.
     */
    private final Supervisor supervisor = new SimpleSupervisor();

    private static final int BSTATS_ID = 3319;

    @Deprecated
    private static void registerMechanic(String name, Class<? extends CraftBookMechanic> mechanicClass, MechanicCategory category) {
        Converter<String, String> camelToSnake = CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);

        MechanicType<?> mechanicType = MechanicType.Builder
                .create()
                .id(camelToSnake.convert(name))
                .name(name)
                .className(mechanicClass.getName())
                .category(category)
                .build();

        registerMechanic(mechanicType);
    }

    public static void registerMechanic(MechanicType<?> mechanicType) {
        MechanicType.REGISTRY.register(mechanicType.getId(), mechanicType);
    }

    static {
        registerMechanic("Variables", VariableManager.class, MechanicCategory.GENERAL);
        registerMechanic("CommandItems", CommandItems.class, MechanicCategory.CUSTOMISATION);
        registerMechanic("CustomCrafting", CustomCrafting.class, MechanicCategory.CUSTOMISATION);
        registerMechanic("DispenserRecipes", DispenserRecipes.class, MechanicCategory.GENERAL);
        registerMechanic("CustomDrops", CustomDrops.class, MechanicCategory.CUSTOMISATION);
        registerMechanic("BetterAi", AIMechanic.class, MechanicCategory.GENERAL);
        registerMechanic("PaintingSwitcher", PaintingSwitch.class, MechanicCategory.GENERAL);
        registerMechanic("BetterPhysics", BetterPhysics.class, MechanicCategory.GENERAL);
        registerMechanic("HeadDrops", HeadDrops.class, MechanicCategory.GENERAL);
        registerMechanic("BetterLeads", BetterLeads.class, MechanicCategory.GENERAL);
        registerMechanic("Marquee", Marquee.class, MechanicCategory.GENERAL);
        registerMechanic("TreeLopper", TreeLopper.class, MechanicCategory.GENERAL);
        registerMechanic("MapChanger", MapChanger.class, MechanicCategory.GENERAL);
        registerMechanic("XpStorer", XPStorer.class, MechanicCategory.GENERAL);
        registerMechanic("LightStone", LightStone.class, MechanicCategory.TOOL);
        registerMechanic("CommandSigns", CommandSigns.class, MechanicCategory.GENERAL);
        registerMechanic("LightSwitch", LightSwitch.class, MechanicCategory.GENERAL);
        registerMechanic("ChunkAnchor", ChunkAnchor.class, MechanicCategory.GENERAL);
        registerMechanic("Ammeter", Ammeter.class, MechanicCategory.TOOL);
        registerMechanic("HiddenSwitch", HiddenSwitch.class, MechanicCategory.GENERAL);
        registerMechanic("Bookcase", Bookcase.class, MechanicCategory.GENERAL);
        registerMechanic("SignCopier", SignCopier.class, MechanicCategory.TOOL);
        registerMechanic("Bridge", Bridge.class, MechanicCategory.GENERAL);
        registerMechanic("Door", Door.class, MechanicCategory.GENERAL);
        registerMechanic("Elevator", Elevator.class, MechanicCategory.GENERAL);
        registerMechanic("Teleporter", Teleporter.class, MechanicCategory.GENERAL);
        registerMechanic("ToggleArea", Area.class, MechanicCategory.GENERAL);
        registerMechanic("Cauldron", ImprovedCauldron.class, MechanicCategory.CUSTOMISATION);
        registerMechanic("LegacyCauldron", Cauldron.class, MechanicCategory.CUSTOMISATION);
        registerMechanic("Gate", Gate.class, MechanicCategory.GENERAL);
        registerMechanic("BetterPistons", BetterPistons.class, MechanicCategory.GENERAL);
        registerMechanic("CookingPot", CookingPot.class, MechanicCategory.GENERAL);
        registerMechanic("Sponge", Sponge.class, MechanicCategory.GENERAL);
        registerMechanic("BetterPlants", BetterPlants.class, MechanicCategory.GENERAL);
        registerMechanic("Chairs", Chair.class, MechanicCategory.GENERAL);
        registerMechanic("Pay", Payment.class, MechanicCategory.CIRCUIT);
        registerMechanic("Jukebox", RedstoneJukebox.class, MechanicCategory.CIRCUIT);
        registerMechanic("Glowstone", GlowStone.class, MechanicCategory.CIRCUIT);
        registerMechanic("Netherrack", Netherrack.class, MechanicCategory.CIRCUIT);
        registerMechanic("JackOLantern", JackOLantern.class, MechanicCategory.CIRCUIT);
        registerMechanic("Pipes", Pipes.class, MechanicCategory.CIRCUIT);
        registerMechanic("BounceBlocks", BounceBlocks.class, MechanicCategory.GENERAL);
        registerMechanic("IntegratedCircuits", ICMechanic.class, MechanicCategory.CIRCUIT);
        registerMechanic("MinecartBooster", CartBooster.class, MechanicCategory.MINECART);
        registerMechanic("MinecartReverser", CartReverser.class, MechanicCategory.MINECART);
        registerMechanic("MinecartSorter", CartSorter.class, MechanicCategory.MINECART);
        registerMechanic("MinecartStation", CartStation.class, MechanicCategory.MINECART);
        registerMechanic("MinecartEjector", CartEjector.class, MechanicCategory.MINECART);
        registerMechanic("MinecartDeposit", CartDeposit.class, MechanicCategory.MINECART);
        registerMechanic("MinecartTeleporter", CartTeleporter.class, MechanicCategory.MINECART);
        registerMechanic("MinecartElevator", CartLift.class, MechanicCategory.MINECART);
        registerMechanic("MinecartDispenser", CartDispenser.class, MechanicCategory.MINECART);
        registerMechanic("MinecartMessenger", CartMessenger.class, MechanicCategory.MINECART);
        registerMechanic("MinecartMaxSpeed", CartMaxSpeed.class, MechanicCategory.MINECART);
        registerMechanic("MinecartMoreRails", MoreRails.class, MechanicCategory.MINECART);
        registerMechanic("MinecartRemoveEntities", com.sk89q.craftbook.mechanics.minecart.RemoveEntities.class, MechanicCategory.MINECART);
        registerMechanic("MinecartVisionSteering", VisionSteering.class, MechanicCategory.MINECART);
        registerMechanic("MinecartDecay", EmptyDecay.class, MechanicCategory.MINECART);
        registerMechanic("MinecartMobBlocker", MobBlocker.class, MechanicCategory.MINECART);
        registerMechanic("MinecartExitRemover", com.sk89q.craftbook.mechanics.minecart.ExitRemover.class, MechanicCategory.MINECART);
        registerMechanic("MinecartCollisionEntry", CollisionEntry.class, MechanicCategory.MINECART);
        registerMechanic("MinecartItemPickup", ItemPickup.class, MechanicCategory.MINECART);
        registerMechanic("MinecartFallModifier", FallModifier.class, MechanicCategory.MINECART);
        registerMechanic("MinecartConstantSpeed", ConstantSpeed.class, MechanicCategory.MINECART);
        registerMechanic("MinecartRailPlacer", RailPlacer.class, MechanicCategory.MINECART);
        registerMechanic("MinecartSpeedModifiers", com.sk89q.craftbook.mechanics.minecart.SpeedModifiers.class, MechanicCategory.MINECART);
        registerMechanic("MinecartEmptySlowdown", EmptySlowdown.class, MechanicCategory.MINECART);
        registerMechanic("MinecartNoCollide", NoCollide.class, MechanicCategory.MINECART);
        registerMechanic("MinecartPlaceAnywhere", PlaceAnywhere.class, MechanicCategory.MINECART);
        registerMechanic("MinecartTemporaryCart", TemporaryCart.class, MechanicCategory.MINECART);
        registerMechanic("BoatRemoveEntities", com.sk89q.craftbook.mechanics.boat.RemoveEntities.class, MechanicCategory.BOAT);
        registerMechanic("BoatUncrashable", Uncrashable.class, MechanicCategory.BOAT);
        registerMechanic("BoatDrops", Drops.class, MechanicCategory.BOAT);
        registerMechanic("BoatDecay", com.sk89q.craftbook.mechanics.boat.EmptyDecay.class, MechanicCategory.BOAT);
        registerMechanic("BoatSpeedModifiers", com.sk89q.craftbook.mechanics.boat.SpeedModifiers.class, MechanicCategory.BOAT);
        registerMechanic("LandBoats", LandBoats.class, MechanicCategory.BOAT);
        registerMechanic("BoatExitRemover", com.sk89q.craftbook.mechanics.boat.ExitRemover.class, MechanicCategory.BOAT);
        registerMechanic("BoatWaterPlaceOnly", WaterPlaceOnly.class, MechanicCategory.BOAT);

        registerMechanic(MechanicType.Builder
                .create()
                .id("snow")
                .name("Snow")
                .className("com.sk89q.craftbook.mechanics.Snow")
                .category(MechanicCategory.GENERAL)
                .build()
        );
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

    /**
     * Gets a list of loaded mechanics.
     *
     * <p>
     *     This list is immutable
     * </p>
     *
     * @return A list of loaded mechanics
     */
    public List<CraftBookMechanic> getLoadedMechanics() {
        return ImmutableList.copyOf(this.loadedMechanics);
    }

    public boolean isMechanicEnabled(@Nullable MechanicType<?> mechanicType) {
        if (mechanicType == null) {
            return false;
        }
        return this.loadedMechanics.stream().anyMatch(mechanicType::isInstance);
    }

    public Optional<CraftBookMechanic> getMechanic(@Nullable MechanicType<?> mechanicType) {
        if (mechanicType == null) {
            return Optional.empty();
        }
        return this.loadedMechanics.stream().filter(mechanicType::isInstance).findAny();
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

                for(CraftBookMechanic mech : getLoadedMechanics())
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

        for(CraftBookMechanic mech : loadedMechanics)
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

        loadedMechanics = new ArrayList<>();

        logDebugMessage("Initializing Mechanisms!", "startup");
        try {
            new File(CraftBookPlugin.inst().getDataFolder(), "mechanics").mkdirs();
        } catch (Exception ignored) {}

        MechanicType.REGISTRY.keySet()
                .stream()
                .filter(mechanic -> config.enabledMechanics.contains(mechanic))
                .forEach(this::enableMechanic);

        setupSelfTriggered();
    }

    /**
     * Enables the mechanic with the specified name.
     * 
     * @param mechanic The name of the mechanic.
     * @return If the mechanic could be found and enabled.
     */
    public boolean enableMechanic(String mechanic) {
        MechanicType<?> mechanicType = MechanicType.REGISTRY.get(mechanic);
        if(mechanicType != null) {
            try {
                CraftBookMechanic mech = mechanicType.create();
                mech.loadConfiguration(new File(new File(CraftBookPlugin.inst().getDataFolder(), "mechanics"), mechanicType.getName() + ".yml"));
                loadedMechanics.add(mech);

                if(!mech.enable()) {
                    getLogger().warning("Failed to enable mechanic: " + mechanicType.getName());
                    mech.disable();
                    return false;
                }
                getServer().getPluginManager().registerEvents(mech, this);
            } catch (Throwable t) {
                getLogger().log(Level.WARNING, "Failed to load mechanic: " + mechanicType.getName(), t);
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    /**
     * Disables the mechanic with the specified name.
     * 
     * @param mechanic The name of the mechanic.
     * @return If the mechanic could be found and disabled.
     */
    public boolean disableMechanic(String mechanic) {
        MechanicType<?> mechanicType = MechanicType.REGISTRY.get(mechanic);

        if(mechanicType == null) {
            return false;
        }

        boolean found = false;

        for (CraftBookMechanic mech : loadedMechanics) {
            if (mechanicType.isInstance(mech)) {
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

        try {
            logDebugMessage("Initializing Metrics!", "startup");
            org.bstats.bukkit.Metrics metrics = new org.bstats.bukkit.Metrics(this, BSTATS_ID);

            metrics.addCustomChart(new org.bstats.bukkit.Metrics.AdvancedPie("language",
                    () -> languageManager.getLanguages().stream().collect(Collectors.toMap(Function.identity(), o -> 1))));
            metrics.addCustomChart(new org.bstats.bukkit.Metrics.SimpleBarChart("enabled_mechanics",
                    () -> loadedMechanics.stream().collect(Collectors.toMap(mech -> mech.getClass().getSimpleName(), o -> 1))));
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
        if(loadedMechanics != null) {
            for(CraftBookMechanic mech : loadedMechanics)
                mech.disable();
            loadedMechanics = null;
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

    /**
     * This method is used to get the CraftBook {@link LanguageManager}.
     *
     * @return The CraftBook {@link LanguageManager}
     */
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
     * This method is used to get CraftBook's {@link Random}.
     *
     * @deprecated Use ThreadLocalRandom.current()
     * @return CraftBook's {@link Random}
     */
    @Deprecated
    public Random getRandom() {
        return ThreadLocalRandom.current();
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

        return selfTriggerManager;
    }

    /**
     * Reload configuration
     */
    public void reloadConfiguration() throws Throwable {

        if(loadedMechanics != null)
            for(CraftBookMechanic mech : loadedMechanics)
                mech.disable();
        loadedMechanics = null;
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