package com.sk89q.craftbook.bukkit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sk89q.craftbook.CraftBookMechanic;
import com.sk89q.craftbook.LocalComponent;
import com.sk89q.craftbook.bukkit.commands.MechanismCommands;
import com.sk89q.craftbook.mech.AIMechanic;
import com.sk89q.craftbook.mech.Ammeter;
import com.sk89q.craftbook.mech.BetterLeads;
import com.sk89q.craftbook.mech.BetterPhysics;
import com.sk89q.craftbook.mech.BetterPistons;
import com.sk89q.craftbook.mech.Bookcase;
import com.sk89q.craftbook.mech.Cauldron;
import com.sk89q.craftbook.mech.Chair;
import com.sk89q.craftbook.mech.ChunkAnchor;
import com.sk89q.craftbook.mech.CommandItems;
import com.sk89q.craftbook.mech.CommandSigns;
import com.sk89q.craftbook.mech.CookingPot;
import com.sk89q.craftbook.mech.CustomDrops;
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
import com.sk89q.craftbook.mech.area.CopyManager;
import com.sk89q.craftbook.mech.area.simple.Bridge;
import com.sk89q.craftbook.mech.area.simple.Door;
import com.sk89q.craftbook.mech.area.simple.Gate;
import com.sk89q.craftbook.mech.cauldron.ImprovedCauldron;
import com.sk89q.craftbook.mech.crafting.CustomCrafting;
import com.sk89q.craftbook.mech.dispenser.DispenserRecipes;
import com.sk89q.craftbook.mech.dispenser.Recipe;

/**
 * Author: Turtle9598
 */
@SuppressWarnings("deprecation")
public class MechanicalCore implements LocalComponent {

    private static MechanicalCore instance;

    private CraftBookPlugin plugin = CraftBookPlugin.inst();
    private final CopyManager copyManager = new CopyManager();
    private CustomCrafting customCrafting;

    private List<CraftBookMechanic> mechanics;

    public static boolean isEnabled() {

        return instance != null;
    }

    public MechanicalCore() {

        instance = this;
    }

    public static MechanicalCore inst() {

        return instance;
    }

    @Override
    public void enable() {

        plugin.registerCommands(MechanismCommands.class);
        mechanics = new ArrayList<CraftBookMechanic>();

        registerMechanics();
    }

    @Override
    public void disable() {

        for(CraftBookMechanic mech : mechanics)
            mech.disable();
        mechanics = null;
        customCrafting = null;
        instance = null;
    }

    public CopyManager getCopyManager() {

        return copyManager;
    }

    public CustomCrafting getCustomCrafting() {

        return customCrafting;
    }

    private void registerMechanics() {

        BukkitConfiguration config = plugin.getConfiguration();

        // New System Mechanics
        if (config.commandItemsEnabled) mechanics.add(new CommandItems());
        if (config.customCraftingEnabled) mechanics.add(customCrafting = new CustomCrafting());
        if (config.customDispensingEnabled) mechanics.add(new DispenserRecipes());
        if (config.snowEnable) mechanics.add(new Snow());
        if (config.customDropEnabled) mechanics.add(new CustomDrops());
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

        if (config.chairEnabled) try {mechanics.add(new Chair()); } catch(Throwable e){plugin.getLogger().warning("Failed to initialize mechanic: Chairs. Make sure you have ProtocolLib!");}
        if (config.footprintsEnabled) try {mechanics.add(new Footprints()); } catch(Throwable e){plugin.getLogger().warning("Failed to initialize mechanic: Footprints. Make sure you have ProtocolLib!");}
        if (config.paymentEnabled) if(CraftBookPlugin.plugins.getEconomy() != null) mechanics.add(new Payment()); else plugin.getLogger().warning("Failed to initialize mechanic: Payment. Make sure you have Vault!");


        Iterator<CraftBookMechanic> iter = mechanics.iterator();
        while(iter.hasNext()) {
            CraftBookMechanic mech = iter.next();
            if(!mech.enable()) {
                plugin.getLogger().warning("Failed to initialize mechanic: " + mech.getClass().getSimpleName());
                mech.disable();
                iter.remove();
                continue;
            }
            plugin.getServer().getPluginManager().registerEvents(mech, plugin);
        }
    }

    /**
     * Register a Dispenser Recipe
     *
     * @param recipe
     *
     * @return if successfully added.
     * @deprecated Use DispenserRecipes.inst().addRecipe(recipe);
     */
    @Deprecated
    public boolean registerDispenserRecipe(Recipe recipe) {

        return plugin.getConfiguration().customDispensingEnabled && DispenserRecipes.inst().addRecipe(recipe);
    }

    @Override
    public List<CraftBookMechanic> getMechanics () {
        return mechanics;
    }
}
