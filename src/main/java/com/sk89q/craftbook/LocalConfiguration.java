package com.sk89q.craftbook;
import java.util.HashMap;
import java.util.List;

import com.sk89q.craftbook.util.ICUtil.LocationCheckType;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.TernaryState;

/**
 * A implementation of Configuration based off of {@link com.sk89q.worldedit.LocalConfiguration} for CraftBook.
 */
public abstract class LocalConfiguration {


    // Common
    // Common - Variables
    public boolean variablesEnabled;
    public boolean variablesDefaultGlobal;
    public boolean variablesCommandBlockOverride;
    public boolean variablesPlayerCommandOverride;
    public boolean variablesPlayerChatOverride;

    // Circuits
    // Circuits - IC
    public boolean ICEnabled;
    public boolean ICCached;
    public boolean ICShortHandEnabled;
    public int ICMaxRange;
    public List<String> ICsDisabled;
    public boolean ICKeepLoaded;
    public LocationCheckType ICdefaultCoordinate;
    public boolean ICSavePersistentData;
    public boolean ICMidiUsePercussion;
    public boolean ICBreakOnError;
    // Circuits - Wiring
    public boolean netherrackEnabled;
    public boolean pumpkinsEnabled;
    public boolean glowstoneEnabled;
    public ItemInfo glowstoneOffBlock;
    // Circuits - Pipes
    public boolean pipesEnabled;
    public boolean pipesDiagonal;
    public ItemInfo pipeInsulator;
    public boolean pipeStackPerPull;
    public boolean pipeRequireSign;
    // Circuits - Redstone Jukebox
    public boolean jukeboxEnabled;

    // Mechanics
    // Mechanics - AI
    public boolean aiEnabled;
    public List<String> aiVisionEnabled;
    public List<String> aiCritBowEnabled;
    public List<String> aiAttackPassiveEnabled;
    // Mechanics - Ammeter
    public boolean ammeterEnabled;
    public ItemInfo ammeterItem;
    // Mechanics - Area
    public boolean areaEnabled;
    public boolean areaAllowRedstone;
    public boolean areaUseSchematics;
    public boolean areaShortenNames;
    public int areaMaxAreaSize;
    public int areaMaxAreaPerUser;
    // Mechanics - BetterLeads
    public boolean leadsEnabled;
    public boolean leadsStopTarget;
    public boolean leadsOwnerBreakOnly;
    public boolean leadsHitchPersists;
    public boolean leadsMobRepellant;
    public List<String> leadsAllowedMobs;
    // Mechanics - BetterPhysics
    public boolean physicsEnabled;
    public boolean physicsLadders;
    // Mechanics - BetterPistons
    public int pistonMaxDistance;
    public boolean pistonsEnabled;
    public boolean pistonsCrusher;
    public boolean pistonsCrusherInstaKill;
    public List<ItemInfo> pistonsCrusherBlacklist;
    public boolean pistonsSuperPush;
    public boolean pistonsSuperSticky;
    public List<ItemInfo> pistonsMovementBlacklist;
    public boolean pistonsBounce;
    public List<ItemInfo> pistonsBounceBlacklist;
    // Mechanics - Bookcase
    public boolean bookcaseEnabled;
    public boolean bookcaseReadHoldingBlock;
    public boolean bookcaseReadWhenSneaking;
    // Mechanics - Bridge
    public boolean bridgeEnabled;
    public boolean bridgeAllowRedstone;
    public int bridgeMaxLength;
    public int bridgeMaxWidth;
    public List<ItemInfo> bridgeBlocks;
    // Mechanics - Cauldron
    public boolean cauldronEnabled;
    public boolean cauldronUseSpoons;
    // Mechanics - Chair
    public boolean chairEnabled;
    public boolean chairAllowHeldBlock;
    public boolean chairHealth;
    public List<ItemInfo> chairBlocks;
    public boolean chairFacing;
    public boolean chairRequireSign;
    // Mechanics - Chunk Anchor
    public boolean chunkAnchorEnabled;
    public boolean chunkAnchorRedstone;
    public boolean chunkAnchorCheck;
    // Mechanics - Command Items
    public boolean commandItemsEnabled;
    // Mechanics - Command Signs
    public boolean commandSignEnabled;
    public boolean commandSignAllowRedstone;
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
    public boolean customDispensingCannon;
    public boolean customDispensingFan;
    public boolean customDispensingFireArrows;
    public boolean customDispensingSnowShooter;
    public boolean customDispensingXPShooter;
    // Mechanics - Custom Drops
    public boolean customDropEnabled;
    public boolean customDropPermissions;
    // Mechanics - Door
    public boolean doorEnabled;
    public boolean doorAllowRedstone;
    public int doorMaxLength;
    public int doorMaxWidth;
    public List<ItemInfo> doorBlocks;
    // Mechanics - Elevator
    public boolean elevatorEnabled;
    public boolean elevatorButtonEnabled;
    public boolean elevatorLoop;
    public boolean elevatorSlowMove;
    public double elevatorMoveSpeed;
    // Mechanics - Footprints
    public boolean footprintsEnabled;
    public List<ItemInfo> footprintsBlocks;
    // Mechanics - Gate
    public boolean gateEnabled;
    public boolean gateAllowRedstone;
    public boolean gateLimitColumns;
    public int gateColumnLimit;
    public List<ItemInfo> gateBlocks;
    public boolean gateEnforceType;
    public int gateColumnHeight;
    public int gateSearchRadius;
    // Mechanics - Head Drops
    public boolean headDropsEnabled;
    public boolean headDropsMobs;
    public boolean headDropsPlayers;
    public boolean headDropsPlayerKillOnly;
    public boolean headDropsMiningDrops;
    public boolean headDropsDropOverrideNatural;
    public double headDropsDropRate;
    public double headDropsLootingRateModifier;
    public boolean headDropsShowNameClick;
    public HashMap<String, Double> headDropsCustomDropRate;
    public HashMap<String, String> headDropsCustomSkins;
    // Mechanics - Hidden Switch
    public boolean hiddenSwitchEnabled;
    public boolean hiddenSwitchAnyside;
    // Mechanics - Legacy Cauldron
    public boolean legacyCauldronEnabled;
    public ItemInfo legacyCauldronBlock;
    // Mechanics - Lightstone
    public boolean lightstoneEnabled;
    public ItemInfo lightstoneItem;
    // Mechanics - Light Switch
    public boolean lightSwitchEnabled;
    public int lightSwitchMaxRange;
    public int lightSwitchMaxLights;
    // Mechanics - Map Changer
    public boolean mapChangerEnabled;
    // Mechanics - Marquee
    public boolean marqueeEnabled;
    // Mechanics - Paintings
    public boolean paintingsEnabled;
    // Mechanics - Payment
    public boolean paymentEnabled;
    // Mechanics - Lightstone
    public boolean signCopyEnabled;
    public ItemInfo signCopyItem;
    // Mechanics - Snow
    public boolean snowEnable;
    public boolean snowPiling;
    public boolean snowTrample;
    public boolean snowPartialTrample;
    public boolean snowPlace;
    public boolean snowSlowdown;
    public boolean snowRealistic;
    public boolean snowHighPiles;
    public boolean snowJumpTrample;
    public List<ItemInfo> snowRealisticReplacables;
    public int snowFallAnimationSpeed;
    // Mechanics - Teleporter
    public boolean teleporterEnabled;
    public boolean teleporterRequireSign;
    public int teleporterMaxRange;
    // Mechanis - TreeLopper
    public boolean treeLopperEnabled;
    public List<ItemInfo> treeLopperBlocks;
    public List<ItemInfo> treeLopperItems;
    public int treeLopperMaxSize;
    public boolean treeLopperAllowDiagonals;
    public boolean treeLopperEnforceData;
    public boolean treeLopperPlaceSapling;
    public boolean treeLopperBreakLeaves;
    // Mechanics - XPStorer
    public boolean xpStorerEnabled;
    public ItemInfo xpStorerBlock;
    public TernaryState xpStorerSneaking;

    // Vehicles
    // Vehicles - Minecart Decay Options
    public boolean minecartDecayEnabled;
    public int minecartDecayTime;
    // Vehicles - Minecart Station Options
    public boolean minecartStationEnabled;
    public ItemInfo minecartStationBlock;
    // Vehicles - Minecart Sorter Options
    public boolean minecartSorterEnabled;
    public ItemInfo minecartSorterBlock;
    // Vehicles - Minecart Ejector Options
    public boolean minecartEjectorEnabled;
    public ItemInfo minecartEjectorBlock;
    // Vehicles - Minecart Deposit Options
    public boolean minecartDepositEnabled;
    public ItemInfo minecartDepositBlock;
    // Vehicles - Minecart Teleport Options
    public boolean minecartTeleportEnabled;
    public ItemInfo minecartTeleportBlock;
    // Vehicles - Minecart Elevator Options
    public boolean minecartElevatorEnabled;
    public ItemInfo minecartElevatorBlock;
    // Vehicles - Minecart Messager Options
    public boolean minecartMessagerEnabled;
    public ItemInfo minecartMessagerBlock;
    // Vehicles - Minecart Reverse Options
    public boolean minecartReverseEnabled;
    public ItemInfo minecartReverseBlock;
    // Vehicles - Minecart SpeedMod Options
    public boolean minecartSpeedModEnabled;
    public ItemInfo minecartSpeedModMaxBoostBlock;
    public ItemInfo minecartSpeedMod25xBoostBlock;
    public ItemInfo minecartSpeedMod50xSlowBlock;
    public ItemInfo minecartSpeedMod20xSlowBlock;
    // Vehicles - Minecart Dispenser Options
    public boolean minecartDispenserEnabled;
    public ItemInfo minecartDispenserBlock;
    public boolean minecartDispenserLegacy;
    public boolean minecartDispenserAntiSpam;
    public boolean minecartDispenserPropel;
    // Vehicles - Minecart MaxSpeed Options
    public boolean minecartMaxSpeedEnabled;
    public ItemInfo minecartMaxSpeedBlock;
    // Vehicles - Minecart Fall Modifier Options
    public boolean minecartFallModifierEnabled;
    public double minecartFallVerticalSpeed;
    public double minecartFallHorizontalSpeed;
    // Vehicles - Minecart More Rails Options
    public boolean minecartMoreRailsEnabled;
    public boolean minecartMoreRailsLadder;
    public double minecartMoreRailsLadderVelocity;
    public boolean minecartMoreRailsPressurePlate;
    // Vehicles - Minecart Remove Entities Options
    public boolean minecartRemoveEntitiesEnabled;
    public boolean minecartRemoveEntitiesOtherCarts;
    // Vehicles - Minecart Vision Steering Options
    public boolean minecartVisionSteeringEnabled;
    public int minecartVisionSteeringMinimumSensitivity;
    // Vehicles - Minecart Block Mob Entry Options
    public boolean minecartBlockMobEntryEnabled;
    // Vehicles - Minecart Remove On Exit Options
    public boolean minecartRemoveOnExitEnabled;
    public boolean minecartRemoveOnExitGiveItem;
    // Vehicles - Minecart Collision Entry Options
    public boolean minecartCollisionEntryEnabled;
    // Vehicles - Minecart Item Pickup Options
    public boolean minecartItemPickupEnabled;
    // Vehicles - Minecart Constant Speed Options
    public boolean minecartConstantSpeedEnable;
    public double minecartConstantSpeedSpeed;
    // Vehicles - Minecart Rail Placer Options
    public boolean minecartRailPlacerEnable;
    // Vehicles - Minecart Speed Modifier Options
    public boolean minecartSpeedModifierEnable;
    public double minecartSpeedModifierMaxSpeed;
    public double minecartSpeedModifierOffRail;
    // Vehicles - Minecart Empty Slowdown Options
    public boolean minecartEmptySlowdownStopperEnable;
    // Vehicles - Minecart No Collide Options
    public boolean minecartNoCollideEnable;
    public boolean minecartNoCollideEmpty;
    public boolean minecartNoCollideFull;
    // Vehicles - Boat Options
    public boolean boatNoCrash;
    public boolean boatBreakReturn;
    // Vehicles - Boat Remove Entities Options
    public boolean boatRemoveEntitiesEnabled;
    public boolean boatRemoveEntitiesOtherBoats;
    // Vehicles - Boat Speed Modifier Options
    public boolean boatSpeedModifierEnable;
    public double boatSpeedModifierMaxSpeed;
    public double boatSpeedModifierUnnoccupiedDeceleration;
    public double boatSpeedModifierOccupiedDeceleration;
    // Vehicles - Boat Land Boats Options
    public boolean boatLandBoatsEnable;
    // Vehicles - Boat Remove On Exit Options
    public boolean boatRemoveOnExitEnabled;
    public boolean boatRemoveOnExitGiveItem;
    // Vehicles - Boat Water Place Only Options
    public boolean boatWaterPlaceOnly;


    /**
     * Loads the configuration.
     */
    public abstract void load();
}