package com.sk89q.craftbook.util.config;

/**
 * @author Turtle9598
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import com.sk89q.craftbook.LocalConfiguration;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mech.CustomDropManager;
import com.sk89q.craftbook.util.ICUtil.LocationCheckType;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;

/**
 * A implementation of YAML based off of {@link com.sk89q.worldedit.util.YAMLConfiguration} for CraftBook.
 */
public class YAMLConfiguration extends LocalConfiguration {

    public final YAMLProcessor config;
    protected final Logger logger;

    public YAMLConfiguration(YAMLProcessor config, Logger logger) {

        this.config = config;
        this.logger = logger;
    }

    @Override
    public void load() {

        /* Circuits Configuration */

        // IC Configuration Listener.
        ICEnabled = config.getBoolean("circuits.ics.enable", true);
        ICCached = config.getBoolean("circuits.ics.cache", true);
        ICMaxRange = config.getInt("circuits.ics.max-radius", 15);
        ICShortHandEnabled = config.getBoolean("circuits.ics.allow-short-hand", true);
        ICKeepLoaded = config.getBoolean("circuits.ics.keep-loaded", false);
        ICsDisabled = config.getStringList("circuits.ics.disallowed-ics", new ArrayList<String>());
        ICdefaultCoordinate = LocationCheckType.getTypeFromName(config.getString("circuits.ics.default-coordinate-system", "RELATIVE"));
        ICSavePersistentData = config.getBoolean("circuits.ics.save-persistent-data", true);
        ICMidiUsePercussion = config.getBoolean("circuits.ics.midi-use-percussion", false);

        // Circuits Configuration Listener
        netherrackEnabled = config.getBoolean("circuits.wiring.netherrack-enabled", false);
        pumpkinsEnabled = config.getBoolean("circuits.wiring.pumpkins-enabled", false);
        glowstoneEnabled = config.getBoolean("circuits.wiring.glowstone-enabled", false);
        glowstoneOffBlock = config.getInt("circuits.wiring.glowstone-off-block", BlockID.GLASS);

        // Pipes Configuration Listener
        pipesEnabled = config.getBoolean("circuits.pipes.enable", true);
        pipesDiagonal = config.getBoolean("circuits.pipes.allow-diagonal", false);
        pipeInsulator = config.getInt("circuits.pipes.insulator-block", BlockID.CLOTH);
        pipeStackPerPull = config.getBoolean("circuits.pipes.stack-per-move", true);
        pipeRequireSign = config.getBoolean("circuits.pipes.require-sign", false);

        /* Mechanism Configuration */

        // AI Configuration Listener
        aiEnabled = config.getBoolean("mechanics.ai.enable", true);
        aiVisionEnabled = config.getStringList("mechanics.ai.vision-enable", Arrays.asList("Zombie","PigZombie"));
        aiCritBowEnabled = config.getStringList("mechanics.ai.crit-bow-enable", Arrays.asList("Skeleton"));

        // Ammeter Configuration Listener
        ammeterEnabled = config.getBoolean("mechanics.ammeter.enable", true);
        ammeterItem = config.getInt("mechanics.ammeter.item", ItemID.COAL);

        // Area Configuration Listener
        areaEnabled = config.getBoolean("mechanics.area.enable", true);
        areaAllowRedstone = config.getBoolean("mechanics.area.allow-redstone", true);
        areaUseSchematics = config.getBoolean("mechanics.area.use-schematics", true);
        areaShortenNames = config.getBoolean("mechanics.area.shorten-long-names", true);
        areaMaxAreaSize = config.getInt("mechanics.area.max-size", 5000);
        areaMaxAreaPerUser = config.getInt("mechanics.area.max-per-user", 30);

        // Better Physics Configuration Listener
        physicsEnabled = config.getBoolean("mechanics.better-physics.enable", false);
        physicsLadders = config.getBoolean("mechanics.better-physics.falling-ladders", false);

        // Better Pistons Configuration Listener
        pistonsEnabled = config.getBoolean("mechanics.better-pistons.enable", true);
        pistonsCrusher = config.getBoolean("mechanics.better-pistons.crushers", true);
        pistonsCrusherInstaKill = config.getBoolean("mechanics.better-pistons.crushers-kill-mobs", false);
        pistonsCrusherBlacklist = config.getIntList("mechanics.better-pistons.crusher-blacklist", new ArrayList<Integer>());
        pistonsSuperSticky = config.getBoolean("mechanics.better-pistons.super-sticky", true);
        pistonsBounce = config.getBoolean("mechanics.better-pistons.bounce", true);
        pistonsBounceBlacklist = config.getIntList("mechanics.better-pistons.bounce-blacklist", new ArrayList<Integer>());
        pistonsSuperPush = config.getBoolean("mechanics.better-pistons.super-push", true);
        pistonMaxDistance = config.getInt("mechanics.better-pistons.max-distance", 12);

        // Bookcase Configuration Listener
        bookcaseEnabled = config.getBoolean("mechanics.bookcase.enable", true);
        bookcaseReadWhenSneaking = config.getBoolean("mechanics.bookcase.read-when-sneaking", false);
        bookcaseReadLine = config.getString("mechanics.bookcase.read-line", "You pick up a book...");

        // Bridge Configuration Listener
        bridgeEnabled = config.getBoolean("mechanics.bridge.enable", true);
        bridgeAllowRedstone = config.getBoolean("mechanics.bridge.allow-redstone", true);
        bridgeMaxLength = config.getInt("mechanics.bridge.max-length", 30);
        bridgeMaxWidth = config.getInt("mechanics.bridge.max-width", 5);
        bridgeBlocks = config.getIntList("mechanics.bridge.blocks", Arrays.asList(4, 5, 20, 43));

        // Cauldron Configuration Listener
        cauldronEnabled = config.getBoolean("mechanics.cauldron.enable", true);
        cauldronUseSpoons = config.getBoolean("mechanics.cauldron.spoons", true);

        // Chair Configuration Listener
        chairEnabled = config.getBoolean("mechanics.chair.enable", true);
        chairSneak = config.getBoolean("mechanics.chair.require-sneak", true);
        chairHealth = config.getBoolean("mechanics.chair.regen-health", true);
        chairBlocks = config.getIntList("mechanics.chair.blocks", Arrays.asList(53, 67, 108, 109, 114, 128, 134, 135, 136, 156));
        chairFacing = config.getBoolean("mechanics.chair.face-correct-direction", true);

        // Chunk Anchor Configuration Listener
        chunkAnchorEnabled = config.getBoolean("mechanics.chunk-anchor.enable", true);
        chunkAnchorRedstone = config.getBoolean("mechanics.chunk-anchor.enable-redstone", true);
        chunkAnchorCheck = config.getBoolean("mechanics.chunk-anchor.check-chunks", true);

        // Command Items Configuration Listener
        commandItemsEnabled = config.getBoolean("mechanics.command-items.enable", true);

        // Command Sign Configuration Listener
        commandSignEnabled = config.getBoolean("mechanics.command-sign.enable", true);

        // Cooking Pot Configuration Listener
        cookingPotEnabled = config.getBoolean("mechanics.cooking-pot.enable", true);
        cookingPotFuel = config.getBoolean("mechanics.cooking-pot.require-fuel", true);
        cookingPotOres = config.getBoolean("mechanics.cooking-pot.cook-ores", false);
        cookingPotSignOpen = config.getBoolean("mechanics.cooking-pot.sign-click-open", true);
        cookingPotDestroyBuckets = config.getBoolean("mechanics.cooking-pot.take-buckets", false);
        cookingPotSuperFast = config.getBoolean("mechanics.cooking-pot.super-fast-cooking", false);

        // Custom Crafting Configuration Listener
        customCraftingEnabled = config.getBoolean("mechanics.custom-crafting.enable", true);

        // Custom Dispensing Configuration Listener
        customDispensingEnabled = config.getBoolean("mechanics.dispenser-recipes.enable", true);

        // Custom Drops Configuration Listener
        customDropEnabled = config.getBoolean("mechanics.custom-drops.enable", true);
        customDropPermissions = config.getBoolean("mechanics.custom-drops.require-permissions", false);
        customDrops = new CustomDropManager(CraftBookPlugin.inst().getDataFolder());

        // Door Configuration Listener
        doorEnabled = config.getBoolean("mechanics.door.enable", true);
        doorAllowRedstone = config.getBoolean("mechanics.door.allow-redstone", true);
        doorMaxLength = config.getInt("mechanics.door.max-length", 30);
        doorMaxWidth = config.getInt("mechanics.door.max-width", 5);
        doorBlocks = config.getIntList("mechanics.door.blocks", Arrays.asList(4, 5, 20, 43));

        // Elevator Configuration Listener
        elevatorEnabled = config.getBoolean("mechanics.elevator.enable", true);
        elevatorButtonEnabled = config.getBoolean("mechanics.elevator.enable-buttons", true);
        elevatorLoop = config.getBoolean("mechanics.elevator.allow-looping", false);
        elevatorSlowMove = config.getBoolean("mechanics.elevator.smooth-movement", false);
        elevatorMoveSpeed = config.getDouble("mechanics.elevator.smooth-movement-speed", 0.5);

        // Footprints Configuration Listener
        footprintsEnabled = config.getBoolean("mechanics.footprints.enable", false);
        footprintsBlocks = config.getIntList("mechanics.footprints.blocks", Arrays.asList(12, 78, 80, 3));

        // Gate Configuration Listener
        gateEnabled = config.getBoolean("mechanics.gate.enable", true);
        gateAllowRedstone = config.getBoolean("mechanics.gate.allow-redstone", true);
        gateLimitColumns = config.getBoolean("mechanics.gate.limit-columns", true);
        gateColumnLimit = config.getInt("mechanics.gate.max-columns", 14);
        gateBlocks = config.getIntList("mechanics.gate.blocks", Arrays.asList(85, 101, 102, 113));
        gateEnforceType = config.getBoolean("mechanics.gate.enforce-type", true);

        // Head Drops Configuration Listener
        headDropsEnabled = config.getBoolean("mechanics.head-drops.enable", false);
        headDropsMobs = config.getBoolean("mechanics.head-drops.drop-mob-heads", true);
        headDropsPlayers = config.getBoolean("mechanics.head-drops.drop-player-heads", true);
        headDropsPlayerKillOnly = config.getBoolean("mechanics.head-drops.require-player-killed", true);
        headDropsMiningDrops = config.getBoolean("mechanics.head-drops.drop-head-when-mined", true);
        headDropsDropRate = config.getDouble("mechanics.head-drops.drop-rate", 0.05);
        headDropsLootingRateModifier = config.getDouble("mechanics.head-drops.looting-rate-modifier", 0.05);

        // Hidden Switch Configuration Listener
        hiddenSwitchEnabled = config.getBoolean("mechanics.hidden-switch.enable", true);
        hiddenSwitchAnyside = config.getBoolean("mechanics.hidden-switch.any-side", true);

        // Legacy Cauldron Configuration Listener
        legacyCauldronEnabled = config.getBoolean("mechanics.legacy-cauldron.enable", true);
        legacyCauldronBlock = config.getInt("mechanics.legacy-cauldron.block", BlockID.STONE);

        // Lightstone Configuration Listener
        lightstoneEnabled = config.getBoolean("mechanics.lightstone.enable", true);
        lightstoneItem = config.getInt("mechanics.lightstone.item", ItemID.LIGHTSTONE_DUST);

        // Light Switch Configuration Listener
        lightSwitchEnabled = config.getBoolean("mechanics.light-switch.enable", true);
        lightSwitchMaxRange = config.getInt("mechanics.light-switch.max-range", 10);
        lightSwitchMaxLights = config.getInt("mechanics.light-switch.max-lights", 20);

        // Map Changer Configuration Listener
        mapChangerEnabled = config.getBoolean("mechanics.map-changer.enable", true);

        // Painting Switcher Configuration Listener
        paintingsEnabled = config.getBoolean("mechanics.paintings.enable", true);

        // Payment Configuration Listener
        paymentEnabled = config.getBoolean("mechanics.payment.enable", true);

        // SignCopy Configuration Listener
        signCopyEnabled = config.getBoolean("mechanics.sign-copy.enable", true);
        signCopyItem = config.getInt("mechanics.sign-copy.item", ItemID.INK_SACK);

        // Snow Configuration Listener
        snowPiling = config.getBoolean("mechanics.snow.piling", false);
        snowTrample = config.getBoolean("mechanics.snow.trample", false);
        snowPlace = config.getBoolean("mechanics.snow.place", false);
        snowSlowdown = config.getBoolean("mechanics.snow.slowdown", false);
        snowRealistic = config.getBoolean("mechanics.snow.realistic", false);
        snowHighPiles = config.getBoolean("mechanics.snow.high-piling", false);
        snowJumpTrample = config.getBoolean("mechanics.snow.jump-trample", false);
        snowRealisticReplacables = config.getIntList("mechanics.snow.replacable-blocks", Arrays.asList(BlockID.LONG_GRASS, BlockID.DEAD_BUSH, BlockID.FIRE, BlockID.RED_FLOWER, BlockID.YELLOW_FLOWER, BlockID.BROWN_MUSHROOM, BlockID.RED_MUSHROOM, BlockID.TRIPWIRE));

        // Teleporter Configuration Listener
        teleporterEnabled = config.getBoolean("mechanics.teleporter.enable", true);
        teleporterRequireSign = config.getBoolean("mechanics.teleporter.require-sign", false);
        teleporterMaxRange = config.getInt("mechanics.teleporter.max-range", 0);

        // TreeLopper Configuration Listener
        treeLopperEnabled = config.getBoolean("mechanics.tree-lopper.enable", false);
        treeLopperBlocks = config.getIntList("mechanics.tree-lopper.block-list", Arrays.asList(BlockID.LOG));
        treeLopperItems = config.getIntList("mechanics.tree-lopper.tool-list", Arrays.asList(ItemID.WOOD_AXE, ItemID.STONE_AXE, ItemID.IRON_AXE, ItemID.GOLD_AXE, ItemID.DIAMOND_AXE));
        treeLopperMaxSize = config.getInt("mechanics.tree-lopper.max-size", 30);
        treeLopperAllowDiagonals = config.getBoolean("mechanics.tree-lopper.allow-diagonals", false);

        // XPStorer Configuration Listener
        xpStorerEnabled = config.getBoolean("mechanics.xp-storer.enable", true);
        xpStorerBlock = config.getInt("mechanics.xp-storer.block", BlockID.MOB_SPAWNER);

        /* Vehicle Configuration */

        // Vehicle Material Configuration Listener
        matBoostMax = ItemInfo.parseFromString(config.getString("vehicles.minecart.max-boost-block", "41:0"));
        matBoost25x = ItemInfo.parseFromString(config.getString("vehicles.minecart.25x-boost-block", "14:0"));
        matSlow50x = ItemInfo.parseFromString(config.getString("vehicles.minecart.50x-slow-block", "88:0"));
        matSlow20x = ItemInfo.parseFromString(config.getString("vehicles.minecart.20x-slow-block", "13:0"));
        matReverse = ItemInfo.parseFromString(config.getString("vehicles.minecart.reverse-block", "35:0"));
        matStation = ItemInfo.parseFromString(config.getString("vehicles.minecart.station-block", "49:0"));
        matSorter = ItemInfo.parseFromString(config.getString("vehicles.minecart.sort-block", "87:0"));
        matEjector = ItemInfo.parseFromString(config.getString("vehicles.minecart.eject-block", "42:0"));
        matDeposit = ItemInfo.parseFromString(config.getString("vehicles.minecart.deposit-block", "15:0"));
        matTeleport = ItemInfo.parseFromString(config.getString("vehicles.minecart.teleport-block", "133:0"));
        matLift = ItemInfo.parseFromString(config.getString("vehicles.minecart.lift-block", "112:0"));
        matDispenser = ItemInfo.parseFromString(config.getString("vehicles.minecart.dispenser-block", "129:0"));
        matMessager = ItemInfo.parseFromString(config.getString("vehicles.minecart.messager-block", "121:0"));

        // Vehicles Minecart Configuration Listener
        minecartEnterOnImpact = config.getBoolean("vehicles.minecart.enter-on-impact", true);
        minecartSlowWhenEmpty = config.getBoolean("vehicles.minecart.slow-when-empty", true);
        minecartDecayWhenEmpty = config.getBoolean("vehicles.minecart.decay-when-empty", false);
        minecartRemoveOnExit = config.getBoolean("vehicles.minecart.remove-on-exit", false);
        minecartRemoveEntities = config.getBoolean("vehicles.minecart.remove-entities", false);
        minecartRemoveEntitiesOtherCarts = config.getBoolean("vehicles.minecart.remove-entities-othercarts", false);
        minecartMaxSpeedModifier = config.getDouble("vehicles.minecart.max-speed-modifier", 1);
        minecartOffRailSpeedModifier = config.getDouble("vehicles.minecart.off-rail-speed-modifier", 0);
        minecartMessengerEnabled = config.getBoolean("vehicles.minecart.track-messages", true);
        minecartDecayTime = config.getInt("vehicles.minecart.decay-time", 20);
        minecartConstantSpeed = config.getDouble("vehicles.minecart.constant-speed", 0);
        minecartPickupItemsOnCollision = config.getBoolean("vehicles.minecart.item-pickup-collision", false);
        minecartPressurePlateIntersection = config.getBoolean("vehicles.minecart.pressure-plate-intersection", false);
        minecartStoragePlaceRails = config.getBoolean("vehicles.minecart.storage-place-rails", false);
        minecartBlockAnimalEntry = config.getBoolean("vehicles.minecart.block-animal-entry", false);

        // Vehicles Minecart Fall Speed Listener
        minecartFallModifierEnabled = config.getBoolean("vehicles.minecart.fall-speed.enable", false);
        minecartFallVerticalSpeed = config.getDouble("vehicles.minecart.fall-speed.vertical-fall-speed", 0.9D);
        minecartFallHorizontalSpeed = config.getDouble("vehicles.minecart.fall-speed.horizontal-fall-speed", 1.1D);

        // Vehicles - Boat Options
        boatRemoveEntities = config.getBoolean("vehicles.boat.remove-entities", false);
        boatNoCrash = config.getBoolean("vehicles.boat.no-crash", false);
        boatRemoveEntitiesOtherBoats = config.getBoolean("vehicles.boat.remove-entities-otherboats", false);
        boatBreakReturn = config.getBoolean("vehicles.boat.break-return-boat", false);

        config.save(); //Save all the added values.

    }
}