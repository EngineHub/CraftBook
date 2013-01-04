package com.sk89q.craftbook;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sk89q.craftbook.mech.CustomDropManager;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;

/**
 * A implementation of Configuration based off of {@link com.sk89q.worldedit.LocalConfiguration} for CraftBook.
 */
public abstract class LocalConfiguration {


    // Circuits
    // Circuits - IC
    public boolean ICEnabled = true;
    public boolean ICCached = true;
    public boolean ICShortHandEnabled = true;
    public Set<String> disabledICs = new HashSet<String>();

    // Circuits - Wiring
    public boolean netherrackEnabled = false;
    public boolean pumpkinsEnabled = false;
    public boolean glowstoneEnabled = false;
    public int glowstoneOffBlock = BlockID.GLASS;

    // Circuits - Pipes
    public boolean pipesEnabled = true;
    public boolean pipesDiagonal = false;
    public int pipeInsulator = BlockID.CLOTH;
    public boolean pipeStackPerPull = true;

    // Mechanics
    // Mechanics - AI
    public boolean aiEnabled = true;
    public boolean aiZombieEnabled = true;
    public boolean aiSkeletonEnabled = true;
    // Mechanics - Ammeter
    public boolean ammeterEnabled = true;
    public int ammeterItem = ItemID.COAL;
    // Mechanics - Area
    public boolean areaEnabled = true;
    public boolean areaAllowRedstone = true;
    public boolean areaUseSchematics = true;
    public int areaMaxAreaSize = 5000;
    public int areaMaxAreaPerUser = 30;
    // Mechanics - Bookcase
    public boolean bookcaseEnabled = true;
    public boolean bookcaseReadWhenSneaking = false;
    public String bookcaseReadLine = "You pick up a book...";
    // Mechanics - Bridge
    public boolean bridgeEnabled = true;
    public boolean bridgeAllowRedstone = true;
    public int bridgeMaxLength = 30;
    public int bridgeMaxWidth = 5;
    public List<Integer> bridgeBlocks = Arrays.asList(4, 5, 20, 43);
    // Mechanics - Cauldron
    public boolean cauldronEnabled = true;
    public boolean cauldronUseSpoons = true;
    // Mechanics - Chair
    public boolean chairEnabled = true;
    public boolean chairSneak = true;
    public boolean chairHealth = true;
    public List<Integer> chairBlocks = Arrays.asList(53, 67, 108, 109, 114, 128, 134, 135, 136);
    // Mechanics - Chunk Anchor
    public boolean chunkAnchorEnabled = true;
    // Mechanics - Command Signs
    public boolean commandSignEnabled = true;
    // Mechanics - Cooking Pot
    public boolean cookingPotEnabled = true;
    public boolean cookingPotFuel = true;
    public boolean cookingPotOres = false;
    public boolean cookingPotSignOpen = true;
    // Mechanics - Custom Crafting
    public boolean customCraftingEnabled = true;
    // Mechanics - Custom Dispensing
    public boolean customDispensingEnabled = true;
    // Mechanics - Custom Drops
    public boolean customDropEnabled = true;
    public boolean customDropPermissions = false;
    public CustomDropManager customDrops;
    // Mechanics - Door
    public boolean doorEnabled = true;
    public boolean doorAllowRedstone = true;
    public int doorMaxLength = 30;
    public int doorMaxWidth = 5;
    public List<Integer> doorBlocks = Arrays.asList(4, 5, 20, 43);
    // Mechanics - Elevator
    public boolean elevatorEnabled = true;
    public boolean elevatorButtonEnabled = true;
    public boolean elevatorLoop = false;
    // Mechanics - Gate
    public boolean gateEnabled = true;
    public boolean gateAllowRedstone = true;
    public boolean gateLimitColumns = true;
    public int gateColumnLimit = 14;
    public List<Integer> gateBlocks = Arrays.asList(85, 101, 102, 113);
    // Mechanics - Hidden Switch
    public boolean hiddenSwitchEnabled = true;
    public boolean hiddenSwitchAnyside = true;
    // Mechanics - Legacy Cauldron
    public boolean legacyCauldronEnabled = true;
    public int legacyCauldronBlock = BlockID.STONE;
    // Mechanics - Lightstone
    public boolean lightstoneEnabled = true;
    public int lightstoneItem = ItemID.LIGHTSTONE_DUST;
    // Mechanics - Light Switch
    public boolean lightSwitchEnabled = true;
    public int lightSwitchMaxRange = 10;
    public int lightSwitchMaxLights = 20;
    // Mechanics - Map Changer
    public boolean mapChangerEnabled = true;
    // Mechanics - Paintings
    public boolean paintingsEnabled = true;
    // Mechanics - Payment
    public boolean paymentEnabled = true;
    // Mechanics - Snow
    public boolean snowEnabled = true;
    public boolean snowTrample = true;
    public boolean snowPlace = true;
    public boolean snowRealistic = false;
    public boolean snowHighPiles = false;
    public boolean snowJumpTrample = false;
    // Mechanics - Teleporter
    public boolean teleporterEnabled = true;
    public boolean teleporterRequireSign = false;
    public int teleporterMaxRange = 0;
    // Mechanics - XPStorer
    public boolean xpStorerEnabled = true;
    public int xpStorerBlock = BlockID.MOB_SPAWNER;

    // Vehicles
    // Vehicles - Materials
    public ItemInfo matBoostMax = new ItemInfo(41, 0);
    public ItemInfo matBoost25x = new ItemInfo(14, 0);
    public ItemInfo matSlow50x = new ItemInfo(88, 0);
    public ItemInfo matSlow20x = new ItemInfo(13, 0);
    public ItemInfo matReverse = new ItemInfo(35, 0);
    public ItemInfo matStation = new ItemInfo(49, 0);
    public ItemInfo matSorter = new ItemInfo(87, 0);
    public ItemInfo matEjector = new ItemInfo(42, 0);
    public ItemInfo matDeposit = new ItemInfo(15, 0);
    public ItemInfo matTeleport = new ItemInfo(133, 0);
    public ItemInfo matLift = new ItemInfo(112, 0);
    public ItemInfo matDispenser = new ItemInfo(129, 0);
    public ItemInfo matMessager = new ItemInfo(121, 0);
    // Vehicles - Minecart Options
    public boolean minecartSlowWhenEmpty;
    public boolean minecartRemoveOnExit;
    public boolean minecartRemoveEntities;
    public boolean minecartRemoveEntitiesOtherCarts;
    public double minecartMaxSpeedModifier;
    public double minecartOffRailSpeedModifier;
    public boolean minecartDecayWhenEmpty;
    public boolean minecartEnterOnImpact;
    public boolean minecartMessengerEnabled = true;
    public int minecartDecayTime;
    public double minecartConstantSpeed;
    public double minecartPoweredRailModifier;
    public boolean minecartPickupItemsOnCollision;
    // Vehicles - Boat Options
    public boolean boatNoCrash;
    public boolean boatRemoveEntities;
    public boolean boatRemoveEntitiesOtherBoats;
    public boolean boatBreakReturn;


    /**
     * Loads the configuration.
     */
    public abstract void load();

    /**
     * Get the working directory to work from.
     *
     * @return
     */
    public File getWorkingDirectory() {

        return new File(".");
    }
}
