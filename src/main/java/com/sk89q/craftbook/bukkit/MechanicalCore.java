package com.sk89q.craftbook.bukkit;
import com.sk89q.craftbook.*;
import com.sk89q.craftbook.bukkit.commands.MechanismCommands;
import com.sk89q.craftbook.mech.*;
import com.sk89q.craftbook.mech.area.Area;
import com.sk89q.craftbook.mech.area.CopyManager;
import com.sk89q.craftbook.mech.cauldron.ImprovedCauldron;
import com.sk89q.craftbook.mech.crafting.CustomCrafting;
import com.sk89q.craftbook.mech.dispenser.DispenserRecipes;
import com.sk89q.craftbook.mech.dispenser.Recipe;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;

import java.util.Iterator;

/**
 * Author: Turtle9598
 */
@SuppressWarnings("deprecation")
public class MechanicalCore implements LocalComponent {

    private static MechanicalCore instance;

    private CraftBookPlugin plugin = CraftBookPlugin.inst();
    private final CopyManager copyManager = new CopyManager();
    private MechanicManager manager;

    public MechanicalCore() {

        instance = this;
    }

    public static MechanicalCore inst() {

        return instance;
    }

    @Override
    public void enable() {

        plugin.registerCommands(MechanismCommands.class);

        manager = new MechanicManager();
        MechanicListenerAdapter adapter = new MechanicListenerAdapter();
        adapter.register(manager);

        registerMechanics();
    }

    @Override
    public void disable() {

        // Nothing to do at the current time
    }

    public CopyManager getCopyManager() {

        return copyManager;
    }

    private void registerMechanics() {

        BukkitConfiguration config = plugin.getConfiguration();

        // Let's register mechanics!
        if (config.ammeterEnabled) registerMechanic(new Ammeter.Factory());
        if (config.bookcaseEnabled) registerMechanic(new Bookcase.Factory());
        if (config.gateEnabled) registerMechanic(new Gate.Factory());
        if (config.bridgeEnabled) registerMechanic(new Bridge.Factory());
        if (config.doorEnabled) registerMechanic(new Door.Factory());
        if (config.elevatorEnabled) registerMechanic(new Elevator.Factory());
        if (config.teleporterEnabled) registerMechanic(new Teleporter.Factory());
        if (config.areaEnabled) registerMechanic(new Area.Factory());
        if (config.commandSignEnabled) registerMechanic(new Command.Factory());
        if (config.chunkAnchorEnabled) registerMechanic(new ChunkAnchor.Factory());
        if (config.lightstoneEnabled) registerMechanic(new LightStone.Factory());
        if (config.lightSwitchEnabled) registerMechanic(new LightSwitch.Factory());
        if (config.hiddenSwitchEnabled) registerMechanic(new HiddenSwitch.Factory());
        if (config.cookingPotEnabled) registerMechanic(new CookingPot.Factory());
        if (config.legacyCauldronEnabled) registerMechanic(new Cauldron.Factory());
        if (config.cauldronEnabled) registerMechanic(new ImprovedCauldron.Factory());
        if (config.xpStorerEnabled) registerMechanic(new XPStorer.Factory());
        if (config.mapChangerEnabled) registerMechanic(new MapChanger.Factory());

        if (config.customCraftingEnabled) new CustomCrafting();

        // Special mechanics.
        if (plugin.getEconomy() != null && config.paymentEnabled) {
            registerMechanic(new Payment.Factory());
        }

        setupSelfTriggered(manager);
    }

    /**
     * Setup the required components of INSTANCE-triggered Mechanics..
     */
    private void setupSelfTriggered(MechanicManager manager) {

        plugin.getLogger().info("Enumerating chunks for INSTANCE-triggered components...");

        long start = System.currentTimeMillis();
        int numWorlds = 0;
        int numChunks = 0;

        for (World world : plugin.getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                manager.enumerate(chunk);
                numChunks++;
            }

            numWorlds++;
        }

        long time = System.currentTimeMillis() - start;

        plugin.getLogger().info(numChunks + " chunk(s) for "
                + numWorlds + " world(s) processed " + "(" + time / 1000.0 * 10 / 10 + "s elapsed)");

        // Set up the clock for INSTANCE-triggered Mechanics.
        plugin.getServer().getScheduler().runTaskTimer(plugin, new MechanicClock(manager), 0, 2);
    }

    protected void registerEvents() {

        Server server = plugin.getServer();
        BukkitConfiguration config = plugin.getConfiguration();

        if (config.customDispensingEnabled) {
            server.getPluginManager().registerEvents(new DispenserRecipes(), plugin);
        }
        if (config.snowEnabled || config.snowPlace) {
            server.getPluginManager().registerEvents(new Snow(), plugin);
        }
        if (config.customDropEnabled) {
            server.getPluginManager().registerEvents(new CustomDrops(), plugin);
        }
        if (config.aiEnabled) {
            server.getPluginManager().registerEvents(new AIMechanic(), plugin);
        }
        if (config.chairEnabled) {
            if (plugin.hasProtocolLib()) server.getPluginManager().registerEvents(new Chair(), plugin);
            else plugin.getLogger().warning("Chairs require ProtocolLib! They will not function without it!");
        }
        if (config.paintingsEnabled) {
            server.getPluginManager().registerEvents(new PaintingSwitch(), plugin);
        }
        /*
         * TODO if (getLocalConfiguration().elementalArrowSettings.enable) { getServer().getPluginManager()
         * .registerEvents(new
         * ElementalArrowsMechanic(this), this); }
         */
    }

    /**
     * Register a mechanic if possible
     *
     * @param factory
     */
    private void registerMechanic(MechanicFactory<? extends Mechanic> factory) {

        manager.register(factory);
    }

    /**
     * Unregister a mechanic if possible TODO Ensure no remnants are left behind
     *
     * @param factory
     *
     * @return true if the mechanic was successfully unregistered.
     */
    private boolean unregisterMechanic(MechanicFactory<? extends Mechanic> factory) {

        return manager.unregister(factory);
    }

    @SuppressWarnings("unused")
    private boolean unregisterAllMechanics() {

        boolean ret = true;

        Iterator<MechanicFactory<? extends Mechanic>> iterator = manager.factories.iterator();

        while (iterator.hasNext()) {
            if (!unregisterMechanic(iterator.next())) ret = false;
        }

        return ret;
    }

    /**
     * Register a Dispenser Recipe
     *
     * @param recipe
     *
     * @return if successfully added.
     */
    public boolean registerDispenserRecipe(Recipe recipe) {

        return plugin.getConfiguration().customDispensingEnabled && DispenserRecipes.inst().addRecipe(recipe);
    }
}
