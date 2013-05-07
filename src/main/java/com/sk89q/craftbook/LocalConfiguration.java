package com.sk89q.craftbook;
import java.io.File;
import java.util.List;

import com.sk89q.craftbook.mech.CustomDropManager;
import com.sk89q.craftbook.util.ICUtil.LocationCheckType;
import com.sk89q.craftbook.util.ItemInfo;

/**
 * A implementation of Configuration based off of {@link com.sk89q.worldedit.LocalConfiguration} for CraftBook.
 */
public abstract class LocalConfiguration {


    // Circuits
    // Circuits - IC
    public boolean ICEnabled;
    public boolean ICCached;
    public boolean ICShortHandEnabled;
    public int ICMaxRange;
    public List<String> disabledICs;
    public boolean ICKeepLoaded;
    public LocationCheckType ICdefaultCoordinate;

    // Circuits - Wiring
    public boolean netherrackEnabled;
    public boolean pumpkinsEnabled;
    public boolean glowstoneEnabled;
    public int glowstoneOffBlock;

    // Circuits - Pipes
    public boolean pipesEnabled;
    public boolean pipesDiagonal;
    public int pipeInsulator;
    public boolean pipeStackPerPull;
    public boolean pipeRequireSign;

    // Mechanics
    // Mechanics - AI
    public boolean aiEnabled;
    public List<String> aiVisionEnabled;
    public List<String> aiCritBowEnabled;
    // Mechanics - Ammeter
    public boolean ammeterEnabled;
    public int ammeterItem;
    // Mechanics - Area
    public boolean areaEnabled;
    public boolean areaAllowRedstone;
    public boolean areaUseSchematics;
    public boolean areaShortenNames;
    public int areaMaxAreaSize;
    public int areaMaxAreaPerUser;
    // Mechanics - BetterPhysics
    public boolean physicsEnabled;
    public boolean physicsLadders;
    public boolean physicsPots;
    // Mechanics - BetterPistons
    public int pistonMaxDistance;
    public boolean pistonsEnabled;
    public boolean pistonsCrusher;
    public boolean pistonsCrusherInstaKill;
    public List<Integer> pistonsCrusherBlacklist;
    public boolean pistonsSuperSticky;
    public boolean pistonsBounce;
    public List<Integer> pistonsBounceBlacklist;
    public boolean pistonsSuperPush;
    // Mechanics - Bookcase
    public boolean bookcaseEnabled;
    public boolean bookcaseReadWhenSneaking;
    public String bookcaseReadLine;
    // Mechanics - Bridge
    public boolean bridgeEnabled;
    public boolean bridgeAllowRedstone;
    public int bridgeMaxLength;
    public int bridgeMaxWidth;
    public List<Integer> bridgeBlocks;
    // Mechanics - Cauldron
    public boolean cauldronEnabled;
    public boolean cauldronUseSpoons;
    // Mechanics - Chair
    public boolean chairEnabled;
    public boolean chairSneak;
    public boolean chairHealth;
    public List<Integer> chairBlocks;
    // Mechanics - Chunk Anchor
    public boolean chunkAnchorEnabled;
    public boolean chunkAnchorRedstone;
    public boolean chunkAnchorCheck;
    // Mechanics - Command Signs
    public boolean commandSignEnabled;
    // Mechanics - Cooking Pot
    public boolean cookingPotEnabled;
    public boolean cookingPotFuel;
    public boolean cookingPotOres;
    public boolean cookingPotSignOpen;
    public boolean cookingPotDestroyBuckets;
    public boolean cookingPotSuperFast;
    // Mechanics - Custom Crafting
    public boolean customCraftingEnabled;
    // Mechanics - Custom Dispensing
    public boolean customDispensingEnabled;
    // Mechanics - Custom Drops
    public boolean customDropEnabled;
    public boolean customDropPermissions;
    public CustomDropManager customDrops;
    // Mechanics - Door
    public boolean doorEnabled;
    public boolean doorAllowRedstone;
    public int doorMaxLength;
    public int doorMaxWidth;
    public List<Integer> doorBlocks;
    // Mechanics - Elevator
    public boolean elevatorEnabled;
    public boolean elevatorButtonEnabled;
    public boolean elevatorLoop;
    // Mechanics - Footprints
    public boolean footprintsEnabled;
    public List<Integer> footprintsBlocks;
    // Mechanics - Gate
    public boolean gateEnabled;
    public boolean gateAllowRedstone;
    public boolean gateLimitColumns;
    public int gateColumnLimit;
    public List<Integer> gateBlocks;
    public boolean gateEnforceType;
    // Mechanics - Hidden Switch
    public boolean hiddenSwitchEnabled;
    public boolean hiddenSwitchAnyside;
    // Mechanics - Legacy Cauldron
    public boolean legacyCauldronEnabled;
    public int legacyCauldronBlock;
    // Mechanics - Lightstone
    public boolean lightstoneEnabled;
    public int lightstoneItem;
    // Mechanics - Light Switch
    public boolean lightSwitchEnabled;
    public int lightSwitchMaxRange;
    public int lightSwitchMaxLights;
    // Mechanics - Map Changer
    public boolean mapChangerEnabled;
    // Mechanics - Paintings
    public boolean paintingsEnabled;
    // Mechanics - Payment
    public boolean paymentEnabled;
    // Mechanics - Lightstone
    public boolean signCopyEnabled;
    public int signCopyItem;
    // Mechanics - Snow
    public boolean snowPiling;
    public boolean snowTrample;
    public boolean snowPlace;
    public boolean snowSlowdown;
    public boolean snowRealistic;
    public boolean snowHighPiles;
    public boolean snowJumpTrample;
    public List<Integer> snowRealisticReplacables;
    // Mechanics - Teleporter
    public boolean teleporterEnabled;
    public boolean teleporterRequireSign;
    public int teleporterMaxRange;
    // Mechanics - XPStorer
    public boolean xpStorerEnabled;
    public int xpStorerBlock;

    // Vehicles
    // Vehicles - Materials
    public ItemInfo matBoostMax;
    public ItemInfo matBoost25x ;
    public ItemInfo matSlow50x;
    public ItemInfo matSlow20x;
    public ItemInfo matReverse;
    public ItemInfo matStation;
    public ItemInfo matSorter;
    public ItemInfo matEjector;
    public ItemInfo matDeposit;
    public ItemInfo matTeleport;
    public ItemInfo matLift;
    public ItemInfo matDispenser;
    public ItemInfo matMessager;
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
    public boolean minecartPickupItemsOnCollision;
    public boolean minecartPressurePlateIntersection;
    public boolean minecartStoragePlaceRails;
    // Vehicles - Minecart Fall Modifier
    public boolean minecartFallModifierEnabled;
    public double minecartFallVerticalSpeed;
    public double minecartFallHorizontalSpeed;
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
