package com.sk89q.craftbook.util.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

import com.sk89q.craftbook.LocalConfiguration;
import com.sk89q.craftbook.util.ICUtil.LocationCheckType;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.TernaryState;
import com.sk89q.util.yaml.YAMLProcessor;

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

        /* Common Configuration */

        // Variable Configuration Listener
        config.setComment("common.variables.enable", "Enables the variable system.");
        variablesEnabled = config.getBoolean("common.variables.enable", true);

        config.setComment("common.variables.default-to-global", "When a variable is accessed via command, if no namespace is provided... It will default to global. If this is false, it will use the players name.");
        variablesDefaultGlobal = config.getBoolean("common.variables.default-to-global", false);

        config.setComment("common.variables.enable-in-commandblocks", "Allows variables to work inside CommandBlocks and on the Console.");
        variablesCommandBlockOverride = config.getBoolean("common.variables.enable-in-commandblocks", false);

        config.setComment("common.variables.enable-in-player-commands", "Allows variables to work in any command a player performs.");
        variablesPlayerCommandOverride = config.getBoolean("common.variables.enable-in-player-commands", false);

        config.setComment("common.variables.enable-in-player-chat", "Allow variables to work in player chat.");
        variablesPlayerChatOverride = config.getBoolean("common.variables.enable-in-player-chat", false);


        /* Circuits Configuration */

        // IC Configuration Listener.
        config.setComment("circuits.ics.enable", "Enables IC mechanics.");
        ICEnabled = config.getBoolean("circuits.ics.enable", false);

        config.setComment("circuits.ics.cache", "Saves many CPU cycles with a VERY small cost to memory (Highly Recommended)");
        ICCached = config.getBoolean("circuits.ics.cache", true);

        config.setComment("circuits.ics.max-radius", "The max radius IC's with a radius setting can use. (WILL cause lag at higher values)");
        ICMaxRange = config.getInt("circuits.ics.max-radius", 10);

        config.setComment("circuits.ics.allow-short-hand", "Allows the usage of IC Shorthand, which is an easier way to create ICs.");
        ICShortHandEnabled = config.getBoolean("circuits.ics.allow-short-hand", true);

        config.setComment("circuits.ics.keep-loaded", "Keep any chunk with an ST IC in it loaded.");
        ICKeepLoaded = config.getBoolean("circuits.ics.keep-loaded", false);

        config.setComment("circuits.ics.disallowed-ics", "A list of IC's which are never loaded. They will not work or show up in /ic list.");
        ICsDisabled = config.getStringList("circuits.ics.disallowed-ics", new ArrayList<String>());

        config.setComment("circuits.ics.default-coordinate-system", "The default coordinate system for ICs. This changes the way IC offsets work. From RELATIVE, OFFSET and ABSOLUTE.");
        ICdefaultCoordinate = LocationCheckType.getTypeFromName(config.getString("circuits.ics.default-coordinate-system", "RELATIVE"));

        config.setComment("circuits.ics.save-persistent-data", "Saves extra data to the CraftBook folder that allows some ICs to work better on server restart.");
        ICSavePersistentData = config.getBoolean("circuits.ics.save-persistent-data", true);

        config.setComment("circuits.ics.midi-use-percussion", "Plays the MIDI percussion channel when using a MIDI playing IC. Note: This may sound horrible on some songs.");
        ICMidiUsePercussion = config.getBoolean("circuits.ics.midi-use-percussion", false);

        config.setComment("circuits.ics.break-on-error", "Break the IC sign when an error occurs from that specific IC.");
        ICBreakOnError = config.getBoolean("circuits.ics.break-on-error", false);


        // Circuits Configuration Listener
        config.setComment("circuits.wiring.netherrack-enabled", "Enables the redstone netherrack mechanic, which lights netherrack when it is powered.");
        netherrackEnabled = config.getBoolean("circuits.wiring.netherrack-enabled", false);

        config.setComment("circuits.wiring.pumpkins-enabled", "Enables the redstone pumpkins mechanic, which toggles pumpkins and Jack O Lanterns depending on power state.");
        pumpkinsEnabled = config.getBoolean("circuits.wiring.pumpkins-enabled", false);

        config.setComment("circuits.wiring.glowstone-enabled", "Enables the redstone glowstone mechanic, which toggles glowstone and a configurable block depending on power state.");
        glowstoneEnabled = config.getBoolean("circuits.wiring.glowstone-enabled", false);

        config.setComment("circuits.wiring.glowstone-off-block", "Sets the block that the redstone glowstone mechanic turns into when turned off.");
        glowstoneOffBlock = new ItemInfo(config.getString("circuits.wiring.glowstone-off-block", "GLASS"));


        // Pipes Configuration Listener
        config.setComment("circuits.pipes.enable", "Enables the pipe mechanic.");
        pipesEnabled = config.getBoolean("circuits.pipes.enable", false);

        config.setComment("circuits.pipes.allow-diagonal", "Allow pipes to work diagonally. Required for insulators to work.");
        pipesDiagonal = config.getBoolean("circuits.pipes.allow-diagonal", false);

        config.setComment("circuits.pipes.insulator-block", "When pipes work diagonally, this block allows the pipe to be insulated to not work diagonally.");
        pipeInsulator = new ItemInfo(config.getString("circuits.pipes.insulator-block", "WOOL"));

        config.setComment("circuits.pipes.stack-per-move", "This option stops the pipes taking the entire chest on power, and makes it just take a single stack.");
        pipeStackPerPull = config.getBoolean("circuits.pipes.stack-per-move", true);

        config.setComment("circuits.pipes.require-sign", "Requires pipes to have a [Pipe] sign connected to them. This is the only way to require permissions to make pipes.");
        pipeRequireSign = config.getBoolean("circuits.pipes.require-sign", false);

        // Redstone Jukebox Configuration Listener
        config.setComment("circuits.jukebox.enable", "Enables the redstone jukebox mechanic.");
        jukeboxEnabled = config.getBoolean("circuits.jukebox.enable", false);

        /* Mechanism Configuration */

        // AI Configuration Listener
        config.setComment("mechanics.ai.enable", "Enable AI Mechanics.");
        aiEnabled = config.getBoolean("mechanics.ai.enable", false);

        config.setComment("mechanics.ai.vision-enable", "The list of entities to enable vision AI mechanics for.");
        aiVisionEnabled = config.getStringList("mechanics.ai.vision-enable", Arrays.asList("Zombie","PigZombie"));

        config.setComment("mechanics.ai.crit-bow-enable", "The list of entities to enable bow critical AI mechanics for.");
        aiCritBowEnabled = config.getStringList("mechanics.ai.crit-bow-enable", Arrays.asList("Skeleton"));

        config.setComment("mechanics.ai.attack-passive-enable", "The list of entities to enable attack passive AI mechanics for.");
        aiAttackPassiveEnabled = config.getStringList("mechanics.ai.attack-passive-enable", Arrays.asList("Zombie"));


        // Ammeter Configuration Listener
        config.setComment("mechanics.ammeter.enable", "Enables the ammeter tool.");
        ammeterEnabled = config.getBoolean("mechanics.ammeter.enable", false);

        config.setComment("mechanics.ammeter.item", "Set the item that is the ammeter tool.");
        ammeterItem = new ItemInfo(config.getString("mechanics.ammeter.item", "COAL"));


        // Area Configuration Listener
        config.setComment("mechanics.area.enable", "Enables Toggle Areas.");
        areaEnabled = config.getBoolean("mechanics.area.enable", false);

        config.setComment("mechanics.area.allow-redstone", "Allow ToggleAreas to be toggled via redstone.");
        areaAllowRedstone = config.getBoolean("mechanics.area.allow-redstone", true);

        config.setComment("mechanics.area.use-schematics", "Use MCEdit Schematics for saving areas. This allows support of all blocks and chest/sign data.");
        areaUseSchematics = config.getBoolean("mechanics.area.use-schematics", true);

        config.setComment("mechanics.area.shorten-long-names", "If this is enabled, namespaces too long to fit on signs will be shortened.");
        areaShortenNames = config.getBoolean("mechanics.area.shorten-long-names", true);

        config.setComment("mechanics.area.max-size", "Sets the max amount of blocks that a ToggleArea can hold.");
        areaMaxAreaSize = config.getInt("mechanics.area.max-size", 5000);

        config.setComment("mechanics.area.max-per-user", "Sets the max amount of ToggleAreas that can be within one namespace.");
        areaMaxAreaPerUser = config.getInt("mechanics.area.max-per-user", 30);


        // Better Leads Configuration Listener
        config.setComment("mechanics.better-leads.enable", "Enables BetterLeads Mechanics.");
        leadsEnabled = config.getBoolean("mechanics.better-leads.enable", false);

        config.setComment("mechanics.better-leads.stop-mob-target", "Stop hostile mobs targeting you if you are holding them on a leash.");
        leadsStopTarget = config.getBoolean("mechanics.better-leads.stop-mob-target", false);

        config.setComment("mechanics.better-leads.owner-unleash-only", "Only allow the owner of tameable entities to unleash them from a leash hitch.");
        leadsOwnerBreakOnly = config.getBoolean("mechanics.better-leads.owner-unleash-only", false);

        config.setComment("mechanics.better-leads.hitch-persists", "Stop leash hitches breaking when clicked no entities are attached. This allows for a public horse hitch or similar.");
        leadsHitchPersists = config.getBoolean("mechanics.better-leads.hitch-persists", false);

        config.setComment("mechanics.better-leads.mob-repel", "If you have a mob tethered to you, mobs of that type will not target you.");
        leadsMobRepellant = config.getBoolean("mechanics.better-leads.mob-repel", false);

        config.setComment("mechanics.better-leads.allowed-mobs", "The list of mobs that can be tethered with a lead.");
        leadsAllowedMobs = config.getStringList("mechanics.better-leads.allowed-mobs", Arrays.asList("ZOMBIE","SPIDER"));


        // Better Physics Configuration Listener
        config.setComment("mechanics.better-physics.enable", "Enables BetterPhysics Mechanics. (This must be enabled for any sub-mechanic to work)");
        physicsEnabled = config.getBoolean("mechanics.better-physics.enable", false);

        config.setComment("mechanics.better-physics.falling-ladders", "Enables BetterPhysics Falling Ladders.");
        physicsLadders = config.getBoolean("mechanics.better-physics.falling-ladders", true);


        // Better Pistons Configuration Listener
        config.setComment("mechanics.better-pistons.enable", "Enables BetterPistons Mechanics. (This must be enabled for any sub-mechanic to work)");
        pistonsEnabled = config.getBoolean("mechanics.better-pistons.enable", false);

        config.setComment("mechanics.better-pistons.crushers", "Enables BetterPistons Crusher Mechanic.");
        pistonsCrusher = config.getBoolean("mechanics.better-pistons.crushers", true);

        config.setComment("mechanics.better-pistons.crushers-kill-mobs", "Causes crushers to kill mobs as well as break blocks. This includes players.");
        pistonsCrusherInstaKill = config.getBoolean("mechanics.better-pistons.crushers-kill-mobs", false);

        config.setComment("mechanics.better-pistons.crusher-blacklist", "A list of blocks that the Crusher piston can not break.");
        pistonsCrusherBlacklist = ItemInfo.parseListFromString(config.getStringList("mechanics.better-pistons.crusher-blacklist", Arrays.asList("OBSIDIAN", "BEDROCK")));

        config.setComment("mechanics.better-pistons.super-sticky", "Enables BetterPistons SuperSticky Mechanic.");
        pistonsSuperSticky = config.getBoolean("mechanics.better-pistons.super-sticky", true);

        config.setComment("mechanics.better-pistons.super-push", "Enables BetterPistons SuperPush Mechanic.");
        pistonsSuperPush = config.getBoolean("mechanics.better-pistons.super-push", true);

        config.setComment("mechanics.better-pistons.movement-blacklist", "A list of blocks that the movement related BetterPistons can not interact with.");
        pistonsMovementBlacklist = ItemInfo.parseListFromString(config.getStringList("mechanics.better-pistons.movement-blacklist", Arrays.asList("OBSIDIAN", "BEDROCK")));

        config.setComment("mechanics.better-pistons.bounce", "Enables BetterPistons Bounce Mechanic.");
        pistonsBounce = config.getBoolean("mechanics.better-pistons.bounce", true);

        config.setComment("mechanics.better-pistons.bounce-blacklist", "A list of blocks that the Bounce piston can not bounce.");
        pistonsBounceBlacklist = ItemInfo.parseListFromString(config.getStringList("mechanics.better-pistons.bounce-blacklist", Arrays.asList("OBSIDIAN", "BEDROCK")));

        config.setComment("mechanics.better-pistons.max-distance", "The maximum distance a BetterPiston can interact with blocks from.");
        pistonMaxDistance = config.getInt("mechanics.better-pistons.max-distance", 12);


        // Better Plants Configuration Listener
        config.setComment("mechanics.better-plants.enable", "Enables BetterPlants Mechanics. (This must be enabled for any sub-mechanic to work)");
        betterPlantsEnabled = config.getBoolean("mechanics.better-plants.enable", false);

        config.setComment("mechanics.better-plants.fern-farming", "Allows ferns to be farmed by breaking top half of a large fern. (And small ferns to grow)");
        betterPlantsFernFarming = config.getBoolean("mechanics.better-plants.fern-farming", true);


        // Bookcase Configuration Listener
        config.setComment("mechanics.bookcase.enable", "Enable readable bookshelves.");
        bookcaseEnabled = config.getBoolean("mechanics.bookcase.enable", false);

        config.setComment("mechanics.bookcase.read-when-sneaking", "Enable reading while sneaking.");
        bookcaseReadWhenSneaking = TernaryState.getFromString(config.getString("mechanics.bookcase.read-when-sneaking", "no"));

        config.setComment("mechanics.bookcase.read-when-holding-block", "Allow bookshelves to work when the player is holding a block.");
        bookcaseReadHoldingBlock = config.getBoolean("mechanics.bookcase.read-when-holding-block", false);


        // Bridge Configuration Listener
        config.setComment("mechanics.bridge.enable", "Enable bridges.");
        bridgeEnabled = config.getBoolean("mechanics.bridge.enable", false);

        config.setComment("mechanics.bridge.allow-redstone", "Enable bridges via redstone.");
        bridgeAllowRedstone = config.getBoolean("mechanics.bridge.allow-redstone", true);

        config.setComment("mechanics.bridge.max-length", "Max length of a bridge.");
        bridgeMaxLength = config.getInt("mechanics.bridge.max-length", 30);

        config.setComment("mechanics.bridge.max-width", "Max width either side. 5 = 11, 1 in middle, 5 on either side.");
        bridgeMaxWidth = config.getInt("mechanics.bridge.max-width", 5);

        config.setComment("mechanics.bridge.blocks", "Blocks bridges can use.");
        bridgeBlocks = ItemInfo.parseListFromString(config.getStringList("mechanics.bridge.blocks", Arrays.asList("COBBLESTONE", "WOOD", "GLASS", "DOUBLE_STEP", "WOOD_DOUBLE_STEP")));


        // Cauldron Configuration Listener
        config.setComment("mechanics.cauldron.enable", "Enable the cauldron mechanic.");
        cauldronEnabled = config.getBoolean("mechanics.cauldron.enable", false);

        config.setComment("mechanics.cauldron.spoons", "Require spoons to cook cauldron recipes.");
        cauldronUseSpoons = config.getBoolean("mechanics.cauldron.spoons", true);

        config.setComment("mechanics.cauldron.enable-redstone", "Allows use of cauldrons via redstone.");
        cauldronAllowRedstone = config.getBoolean("mechanics.cauldron.enable-redstone", false);

        config.setComment("mechanics.cauldron.item-tracking", "Tracks items and forces them to to tracked by the cauldron. Fixes mc bugs by holding item in place.");
        cauldronItemTracking = config.getBoolean("mechanics.cauldron.item-tracking", false);

        config.setComment("mechanics.cauldron.require-sign", "Requires a [Cauldron] sign to be on the side of a cauldron. Useful for requiring creation permissions.");
        cauldronRequireSign = config.getBoolean("mechanics.cauldron.require-sign", false);


        // Chair Configuration Listener
        config.setComment("mechanics.chair.enable", "Enable chair mechanic.");
        chairEnabled = config.getBoolean("mechanics.chair.enable", false);

        config.setComment("mechanics.chair.allow-holding-blocks", "Allow players to sit in chairs when holding blocks.");
        chairAllowHeldBlock = config.getBoolean("mechanics.chair.allow-holding-blocks", false);

        config.setComment("mechanics.chair.regen-health", "Regenerate health passively when sitting down.");
        chairHealth = config.getBoolean("mechanics.chair.regen-health", true);

        config.setComment("mechanics.chair.regen-health-amount", "The amount of health regenerated passively. (Can be decimal)");
        chairHealAmount = config.getDouble("mechanics.chair.regen-health-amount", 1);

        config.setComment("mechanics.chair.blocks", "A list of blocks that can be sat on.");
        chairBlocks = ItemInfo.parseListFromString(config.getStringList("mechanics.chair.blocks", Arrays.asList("WOOD_STAIRS", "COBBLESTONE_STAIRS", "BRICK_STAIRS", "SMOOTH_STAIRS", "NETHER_BRICK_STAIRS", "SANDSTONE_STAIRS", "SPRUCE_WOOD_STAIRS", "BIRCH_WOOD_STAIRS", "JUNGLE_WOOD_STAIRS", "QUARTZ_STAIRS", "ACACIA_STAIRS")));

        config.setComment("mechanics.chair.face-correct-direction", "When the player sits, automatically face them the direction of the chair. (If possible)");
        chairFacing = config.getBoolean("mechanics.chair.face-correct-direction", true);

        config.setComment("mechanics.chair.require-sign", "Require a sign to be attached to the chair in order to work!");
        chairRequireSign = config.getBoolean("mechanics.chair.require-sign", false);

        config.setComment("mechanics.chair.max-distance", "The maximum distance between the click point and the sign. (When require sign is on)");
        chairMaxDistance = config.getInt("mechanics.chair.max-distance", 3);

        config.setComment("mechanics.chair.max-click-radius", "The maximum distance the player can be from the sign.");
        chairMaxClickRadius = config.getInt("mechanics.chair.max-click-radius", 5);


        // Chunk Anchor Configuration Listener
        config.setComment("mechanics.chunk-anchor.enable", "Enable chunk anchors.");
        chunkAnchorEnabled = config.getBoolean("mechanics.chunk-anchor.enable", false);

        config.setComment("mechanics.chunk-anchor.enable-redstone", "Enable toggling with redstone.");
        chunkAnchorRedstone = config.getBoolean("mechanics.chunk-anchor.enable-redstone", true);

        config.setComment("mechanics.chunk-anchor.check-chunks", "On creation, check the chunk for already existing chunk anchors.");
        chunkAnchorCheck = config.getBoolean("mechanics.chunk-anchor.check-chunks", true);


        // Command Items Configuration Listener
        config.setComment("mechanics.command-items.enable", "Enables the CommandItems mechanic.");
        commandItemsEnabled = config.getBoolean("mechanics.command-items.enable", false);


        // Command Sign Configuration Listener
        config.setComment("mechanics.command-sign.enable", "Enable command signs.");
        commandSignEnabled = config.getBoolean("mechanics.command-sign.enable", false);

        config.setComment("mechanics.command-sign.allow-redstone", "Enable CommandSigns via redstone.");
        commandSignAllowRedstone = config.getBoolean("mechanics.command-sign.allow-redstone", true);


        // Cooking Pot Configuration Listener
        config.setComment("mechanics.cooking-pot.enable", "Enable cooking pots.");
        cookingPotEnabled = config.getBoolean("mechanics.cooking-pot.enable", false);

        config.setComment("mechanics.cooking-pot.allow-redstone", "Allows for redstone to be used as a fuel source.");
        cookingPotAllowRedstone = config.getBoolean("mechanics.cooking-pot.allow-redstone", true);

        config.setComment("mechanics.cooking-pot.require-fuel", "Require fuel to cook.");
        cookingPotFuel = config.getBoolean("mechanics.cooking-pot.require-fuel", true);

        config.setComment("mechanics.cooking-pot.cook-ores", "Allows the cooking pot to cook ores and other smeltable items.");
        cookingPotOres = config.getBoolean("mechanics.cooking-pot.cook-ores", false);

        config.setComment("mechanics.cooking-pot.sign-click-open", "When enabled, right clicking the [Cook] sign will open the cooking pot.");
        cookingPotSignOpen = config.getBoolean("mechanics.cooking-pot.sign-click-open", true);

        config.setComment("mechanics.cooking-pot.take-buckets", "When enabled, lava buckets being used as fuel will consume the bucket.");
        cookingPotDestroyBuckets = config.getBoolean("mechanics.cooking-pot.take-buckets", false);

        config.setComment("mechanics.cooking-pot.super-fast-cooking", "When enabled, cooking pots cook at incredibly fast speeds. Useful for semi-instant cooking systems.");
        cookingPotSuperFast = config.getBoolean("mechanics.cooking-pot.super-fast-cooking", false);


        // Custom Crafting Configuration Listener
        config.setComment("mechanics.custom-crafting.enable", "Enable custom crafting.");
        customCraftingEnabled = config.getBoolean("mechanics.custom-crafting.enable", false);


        // Custom Dispensing Configuration Listener
        config.setComment("mechanics.dispenser-recipes.enable", "Enables Dispenser Recipes.");
        customDispensingEnabled = config.getBoolean("mechanics.dispenser-recipes.enable", false);

        config.setComment("mechanics.dispenser-recipes.cannon-enable", "Enables Cannon Dispenser Recipe.");
        customDispensingCannon = config.getBoolean("mechanics.dispenser-recipes.cannon-enable", true);

        config.setComment("mechanics.dispenser-recipes.fan-enable", "Enables Fan Dispenser Recipe.");
        customDispensingFan = config.getBoolean("mechanics.dispenser-recipes.fan-enable", true);

        config.setComment("mechanics.dispenser-recipes.fire-arrows-enable", "Enables Fire Arrows Dispenser Recipe.");
        customDispensingFireArrows = config.getBoolean("mechanics.dispenser-recipes.fire-arrows-enable", true);

        config.setComment("mechanics.dispenser-recipes.snow-shooter-enable", "Enables Snow Shooter Dispenser Recipe.");
        customDispensingSnowShooter = config.getBoolean("mechanics.dispenser-recipes.snow-shooter-enable", true);

        config.setComment("mechanics.dispenser-recipes.xp-shooter-enable", "Enables XP Shooter Dispenser Recipe.");
        customDispensingXPShooter = config.getBoolean("mechanics.dispenser-recipes.xp-shooter-enable", true);


        // Custom Drops Configuration Listener
        config.setComment("mechanics.custom-drops.enable", "Enable Custom Drops.");
        customDropEnabled = config.getBoolean("mechanics.custom-drops.enable", false);

        config.setComment("mechanics.custom-drops.require-permissions", "Require a permission node to get custom drops.");
        customDropPermissions = config.getBoolean("mechanics.custom-drops.require-permissions", false);


        // Door Configuration Listener
        config.setComment("mechanics.door.enable", "Enables Doors.");
        doorEnabled = config.getBoolean("mechanics.door.enable", false);

        config.setComment("mechanics.door.allow-redstone", "Allow doors to be toggled via redstone.");
        doorAllowRedstone = config.getBoolean("mechanics.door.allow-redstone", true);

        config.setComment("mechanics.door.max-length", "The maximum length(height) of a door.");
        doorMaxLength = config.getInt("mechanics.door.max-length", 30);

        config.setComment("mechanics.door.max-width", "Max width either side. 5 = 11, 1 in middle, 5 on either side");
        doorMaxWidth = config.getInt("mechanics.door.max-width", 5);

        config.setComment("mechanics.door.blocks", "A list of blocks that a door can be made out of.");
        doorBlocks = ItemInfo.parseListFromString(config.getStringList("mechanics.door.blocks", Arrays.asList("COBBLESTONE", "WOOD", "GLASS", "DOUBLE_STEP", "WOOD_DOUBLE_STEP")));


        // Elevator Configuration Listener
        config.setComment("mechanics.elevator.enable", "Enables the Elevator mechanic.");
        elevatorEnabled = config.getBoolean("mechanics.elevator.enable", false);

        config.setComment("mechanics.elevator.allow-redstone", "Allows elevators to be triggered by redstone, which will move all players in a radius.");
        elevatorAllowRedstone = config.getBoolean("mechanics.elevator.allow-redstone", false);

        config.setComment("mechanics.elevator.redstone-player-search-radius", "The radius that elevators will look for players in when triggered by redstone.");
        elevatorRedstoneRadius = config.getInt("mechanics.elevator.redstone-player-search-radius", 3);

        config.setComment("mechanics.elevator.enable-buttons", "Allow elevators to be used by a button on the other side of the block.");
        elevatorButtonEnabled = config.getBoolean("mechanics.elevator.enable-buttons", true);

        config.setComment("mechanics.elevator.allow-looping", "Allows elevators to loop the world height. The heighest lift up will go to the next lift on the bottom of the world and vice versa.");
        elevatorLoop = config.getBoolean("mechanics.elevator.allow-looping", false);

        config.setComment("mechanics.elevator.smooth-movement", "Causes the elevator to slowly move the player between floors instead of instantly.");
        elevatorSlowMove = config.getBoolean("mechanics.elevator.smooth-movement", false);

        config.setComment("mechanics.elevator.smooth-movement-speed", "The speed at which players move from floor to floor when smooth movement is enabled.");
        elevatorMoveSpeed = config.getDouble("mechanics.elevator.smooth-movement-speed", 0.5);


        // Footprints Configuration Listener
        config.setComment("mechanics.footprints.enable", "Enable the footprints mechanic.");
        footprintsEnabled = config.getBoolean("mechanics.footprints.enable", false);

        config.setComment("mechanics.footprints.blocks", "The list of blocks that footprints appear on.");
        footprintsBlocks = ItemInfo.parseListFromString(config.getStringList("mechanics.footprints.blocks", Arrays.asList("DIRT", "SAND", "SNOW", "SNOW_BLOCK", "ICE")));


        // Gate Configuration Listener
        config.setComment("mechanics.gate.enable", "Enables the gate mechanic.");
        gateEnabled = config.getBoolean("mechanics.gate.enable", false);

        config.setComment("mechanics.gate.allow-redstone", "Allows the gate mechanic to be toggled via redstone.");
        gateAllowRedstone = config.getBoolean("mechanics.gate.allow-redstone", true);

        config.setComment("mechanics.gate.limit-columns", "Limit the amount of columns a gate can toggle.");
        gateLimitColumns = config.getBoolean("mechanics.gate.limit-columns", true);

        config.setComment("mechanics.gate.max-columns", "If limit-columns is enabled, the maximum number of columns that a gate can toggle.");
        gateColumnLimit = config.getInt("mechanics.gate.max-columns", 14);

        config.setComment("mechanics.gate.blocks", "The list of blocks that a gate can use.");
        gateBlocks = ItemInfo.parseListFromString(config.getStringList("mechanics.gate.blocks", Arrays.asList("FENCE", "IRON_FENCE", "THIN_GLASS", "NETHER_FENCE")));

        config.setComment("mechanics.gate.enforce-type", "Make sure gates are only able to toggle a specific material type. This prevents transmutation.");
        gateEnforceType = config.getBoolean("mechanics.gate.enforce-type", true);

        config.setComment("mechanics.gate.max-column-height", "The max height of a column.");
        gateColumnHeight = config.getInt("mechanics.gate.max-column-height", 12);

        config.setComment("mechanics.gate.gate-search-radius", "The radius around the sign the gate checks for fences in. Note: This is doubled upwards.");
        gateSearchRadius = config.getInt("mechanics.gate.gate-search-radius", 3);


        // Head Drops Configuration Listener
        config.setComment("mechanics.head-drops.enable", "Enables the Head Drops mechanic.");
        headDropsEnabled = config.getBoolean("mechanics.head-drops.enable", false);

        config.setComment("mechanics.head-drops.drop-mob-heads", "Allow the Head Drops mechanic to drop mob heads.");
        headDropsMobs = config.getBoolean("mechanics.head-drops.drop-mob-heads", true);

        config.setComment("mechanics.head-drops.drop-player-heads", "Allow the Head Drops mechanic to drop player heads.");
        headDropsPlayers = config.getBoolean("mechanics.head-drops.drop-player-heads", true);

        config.setComment("mechanics.head-drops.require-player-killed", "Only drop heads when killed by a player. Otherwise they will drop heads on any death.");
        headDropsPlayerKillOnly = config.getBoolean("mechanics.head-drops.require-player-killed", true);

        config.setComment("mechanics.head-drops.drop-head-when-mined", "When enabled, heads keep their current skin when mined and are dropped accordingly.");
        headDropsMiningDrops = config.getBoolean("mechanics.head-drops.drop-head-when-mined", true);

        config.setComment("mechanics.head-drops.override-natural-head-drops", "Override natural head drops, this will cause natural head drops to use the chances provided by CraftBook. (Eg, Wither Skeleton Heads)");
        headDropsDropOverrideNatural = config.getBoolean("mechanics.head-drops.override-natural-head-drops", false);

        config.setComment("mechanics.head-drops.drop-rate", "A value between 1 and 0 which dictates the global chance of heads being dropped. This can be overridden per-entity type.");
        headDropsDropRate = config.getDouble("mechanics.head-drops.drop-rate", 0.05);

        config.setComment("mechanics.head-drops.looting-rate-modifier", "This amount is added to the chance for every looting level on an item. Eg, a chance of 0.05(5%) and a looting mod of 0.05(5%) on a looting 3 sword, would give a 0.20 chance (20%).");
        headDropsLootingRateModifier = config.getDouble("mechanics.head-drops.looting-rate-modifier", 0.05);

        config.setComment("mechanics.head-drops.show-name-right-click", "When enabled, right clicking a placed head will say the owner of the head's skin.");
        headDropsShowNameClick = config.getBoolean("mechanics.head-drops.show-name-right-click", true);

        headDropsCustomDropRate = new HashMap<String, Double>();
        if(config.getKeys("mechanics.head-drops.drop-rates") != null) {
            for(String key : config.getKeys("mechanics.head-drops.drop-rates"))
                headDropsCustomDropRate.put(key.toUpperCase(), config.getDouble("mechanics.head-drops.drop-rates." + key));
        } else
            config.addNode("mechanics.head-drops.drop-rates");
        headDropsCustomSkins = new HashMap<String, String>();
        if(config.getKeys("mechanics.head-drops.custom-mob-skins") != null) {
            for(String key : config.getKeys("mechanics.head-drops.custom-mob-skins"))
                headDropsCustomSkins.put(key.toUpperCase(), config.getString("mechanics.head-drops.custom-mob-skins." + key));
        } else
            config.addNode("mechanics.head-drops.custom-mob-skins");


        // Hidden Switch Configuration Listener
        config.setComment("mechanics.hidden-switch.enable", "Enables the Hidden Switch mechanic.");
        hiddenSwitchEnabled = config.getBoolean("mechanics.hidden-switch.enable", false);

        config.setComment("mechanics.hidden-switch.any-side", "Allows the Hidden Switch to be activated from any side of the block.");
        hiddenSwitchAnyside = config.getBoolean("mechanics.hidden-switch.any-side", true);


        // Legacy Cauldron Configuration Listener
        config.setComment("mechanics.legacy-cauldron.enable", "Enables the Legacy Cauldron mechanic.");
        legacyCauldronEnabled = config.getBoolean("mechanics.legacy-cauldron.enable", false);

        config.setComment("mechanics.legacy-cauldron.block", "The block to use as the casing for the legacy cauldron.");
        legacyCauldronBlock = new ItemInfo(config.getString("mechanics.legacy-cauldron.block", "STONE"));


        // Lightstone Configuration Listener
        config.setComment("mechanics.lightstone.enable", "Enables the LightStone mechanic.");
        lightstoneEnabled = config.getBoolean("mechanics.lightstone.enable", false);

        config.setComment("mechanics.lightstone.item", "The item that the lightstone mechanic uses.");
        lightstoneItem = new ItemInfo(config.getString("mechanics.lightstone.item", "GLOWSTONE_DUST"));


        // Light Switch Configuration Listener
        config.setComment("mechanics.light-switch.enable", "Enables the Light Switch mechanic.");
        lightSwitchEnabled = config.getBoolean("mechanics.light-switch.enable", false);

        config.setComment("mechanics.light-switch.max-range", "The maximum range that the mechanic searches for lights in.");
        lightSwitchMaxRange = config.getInt("mechanics.light-switch.max-range", 10);

        config.setComment("mechanics.light-switch.max-lights", "The maximum amount of lights that a Light Switch can toggle per usage.");
        lightSwitchMaxLights = config.getInt("mechanics.light-switch.max-lights", 20);


        // Map Changer Configuration Listener
        config.setComment("mechanics.map-changer.enable", "Enables the Map Changer mechanic.");
        mapChangerEnabled = config.getBoolean("mechanics.map-changer.enable", false);


        // Marquee Configuration Listener
        config.setComment("mechanics.marquee.enable", "Enables the Marquee mechanic.");
        marqueeEnabled = config.getBoolean("mechanics.marquee.enable", false);


        // Painting Switcher Configuration Listener
        config.setComment("mechanics.paintings.enable", "Enables the Painting Switcher mechanic.");
        paintingsEnabled = config.getBoolean("mechanics.paintings.enable", false);


        // Payment Configuration Listener
        config.setComment("mechanics.payment.enable", "Enables the Payment mechanic.");
        paymentEnabled = config.getBoolean("mechanics.payment.enable", false);


        // SignCopy Configuration Listener
        config.setComment("mechanics.sign-copy.enable", "Enables the Sign Copy mechanic.");
        signCopyEnabled = config.getBoolean("mechanics.sign-copy.enable", false);

        config.setComment("mechanics.sign-copy.item", "The item the Sign Copy mechanic uses.");
        signCopyItem = new ItemInfo(config.getString("mechanics.sign-copy.item", "INK_SACK:0"));


        // Snow Configuration Listener
        config.setComment("mechanics.snow.enable", "Enables the Snow mechanic.");
        snowEnable = config.getBoolean("mechanics.snow.enable", false);

        config.setComment("mechanics.snow.piling", "Enables the piling feature of the Snow mechanic.");
        snowPiling = config.getBoolean("mechanics.snow.piling", false);

        config.setComment("mechanics.snow.trample", "Enables the trampling feature of the Snow mechanic.");
        snowTrample = config.getBoolean("mechanics.snow.trample", false);

        config.setComment("mechanics.snow.partial-trample-only", "If trampling is enabled, only trample it down to the smallest snow.");
        snowPartialTrample = config.getBoolean("mechanics.snow.partial-trample-only", false);

        config.setComment("mechanics.snow.jump-trample", "Require jumping to trample snow.");
        snowJumpTrample = config.getBoolean("mechanics.snow.jump-trample", false);

        config.setComment("mechanics.snow.place", "Allow snowballs to create snow when they land.");
        snowPlace = config.getBoolean("mechanics.snow.place", false);

        config.setComment("mechanics.snow.slowdown", "Slows down entities as they walk through thick snow.");
        snowSlowdown = config.getBoolean("mechanics.snow.slowdown", false);

        config.setComment("mechanics.snow.realistic", "Realistically move snow around, creating an 'avalanche' or 'mound' effect.");
        snowRealistic = config.getBoolean("mechanics.snow.realistic", false);

        config.setComment("mechanics.snow.high-piling", "Allow piling above the 1 block height.");
        snowHighPiles = config.getBoolean("mechanics.snow.high-piling", false);

        config.setComment("mechanics.snow.max-pile-height", "The maximum piling height of high piling snow.");
        snowMaxPileHeight = config.getInt("mechanics.snow.max-pile-height", 3);

        config.setComment("mechanics.snow.replacable-blocks", "A list of blocks that can be replaced by realistic snow.");
        snowRealisticReplacables = ItemInfo.parseListFromString(config.getStringList("mechanics.snow.replacable-blocks", Arrays.asList("DEAD_BUSH", "LONG_GRASS", "YELLOW_FLOWER", "RED_ROSE", "BROWN_MUSHROOM", "RED_MUSHROOM", "FIRE")));

        config.setComment("mechanics.snow.falldown-animation-speed", "The fall animation speed of realistic snow.");
        snowFallAnimationSpeed = config.getInt("mechanics.snow.falldown-animation-speed", 5);

        config.setComment("mechanics.snow.freeze-water", "Should snow freeze water?");
        snowFreezeWater = config.getBoolean("mechanics.snow.freeze-water", false);

        config.setComment("mechanics.snow.melt-in-sunlight", "Enables snow to melt in sunlight.");
        snowMeltSunlight = config.getBoolean("mechanics.snow.melt-in-sunlight", false);

        config.setComment("mechanics.snow.partial-melt-only", "If melt in sunlight is enabled, only melt it down to the smallest snow.");
        snowMeltSunlight = config.getBoolean("mechanics.snow.partial-melt-only", false);


        // Sponge Configuration Listener
        config.setComment("mechanics.sponge.enable", "Enables sponge mechanic.");
        spongeEnabled = config.getBoolean("mechanics.sponge.enable", false);

        config.setComment("mechanics.sponge.radius", "The maximum radius of the sponge.");
        spongeRadius = config.getInt("mechanics.sponge.radius", 5);

        config.setComment("mechanics.sponge.circular-radius", "Whether the radius should be circular or square.");
        spongeCircleRadius = config.getBoolean("mechanics.sponge.circular-radius", true);

        config.setComment("mechanics.sponge.require-redstone", "Whether to require redstone to suck up water or not.");
        spongeRedstone = config.getBoolean("mechanics.sponge.require-redstone", false);


        // Teleporter Configuration Listener
        config.setComment("mechanics.teleporter.enable", "Enables the Teleporter mechanic.");
        teleporterEnabled = config.getBoolean("mechanics.teleporter.enable", false);

        config.setComment("mechanics.teleporter.require-sign", "Require a sign to be at the destination of the teleportation.");
        teleporterRequireSign = config.getBoolean("mechanics.teleporter.require-sign", false);

        config.setComment("mechanics.teleporter.max-range", "The maximum distance between the start and end of a teleporter. Set to 0 for infinite.");
        teleporterMaxRange = config.getInt("mechanics.teleporter.max-range", 0);


        // TreeLopper Configuration Listener
        config.setComment("mechanics.tree-lopper.enable", "Enables the Tree Lopper mechanic.");
        treeLopperEnabled = config.getBoolean("mechanics.tree-lopper.enable", false);

        config.setComment("mechanics.tree-lopper.block-list", "A list of log blocks. This can be modified to include more logs. (for mod support etc)");
        treeLopperBlocks = ItemInfo.parseListFromString(config.getStringList("mechanics.tree-lopper.block-list", Arrays.asList("LOG", "LOG_2")));

        config.setComment("mechanics.tree-lopper.tool-list", "A list of tools that can trigger the TreeLopper mechanic.");
        treeLopperItems = ItemInfo.parseListFromString(config.getStringList("mechanics.tree-lopper.tool-list", Arrays.asList("IRON_AXE", "WOOD_AXE", "STONE_AXE", "DIAMOND_AXE", "GOLD_AXE")));

        config.setComment("mechanics.tree-lopper.max-size", "The maximum amount of blocks the TreeLopper can break.");
        treeLopperMaxSize = config.getInt("mechanics.tree-lopper.max-size", 30);

        config.setComment("mechanics.tree-lopper.allow-diagonals", "Allow the TreeLopper to break blocks that are diagonal from each other.");
        treeLopperAllowDiagonals = config.getBoolean("mechanics.tree-lopper.allow-diagonals", false);

        config.setComment("mechanics.tree-lopper.enforce-data", "Make sure the blocks broken by TreeLopper all share the same data values.");
        treeLopperEnforceData = config.getBoolean("mechanics.tree-lopper.enforce-data", false);

        config.setComment("mechanics.tree-lopper.place-saplings", "If enabled, TreeLopper will plant a sapling automatically when a tree is broken.");
        treeLopperPlaceSapling = config.getBoolean("mechanics.tree-lopper.place-saplings", false);

        config.setComment("mechanics.tree-lopper.break-leaves", "If enabled, TreeLopper will break leaves connected to the tree. (If enforce-data is enabled, will only break leaves of same type)");
        treeLopperBreakLeaves = config.getBoolean("mechanics.tree-lopper.break-leaves", false);


        // XPStorer Configuration Listener
        config.setComment("mechanics.xp-storer.enable", "Enable the XP Storer mechanic.");
        xpStorerEnabled = config.getBoolean("mechanics.xp-storer.enable", false);

        config.setComment("mechanics.xp-storer.require-bottle", "Requires the player to be holding a glass bottle to use.");
        xpStorerRequireBottle = config.getBoolean("mechanics.xp-storer.require-bottle", false);

        config.setComment("mechanics.xp-storer.xp-per-bottle", "Sets the amount of XP points required per each bottle.");
        xpStorerPerBottle = config.getInt("mechanics.xp-storer.xp-per-bottle", 16);

        config.setComment("mechanics.xp-storer.block", "The block that is an XP Spawner.");
        xpStorerBlock = new ItemInfo(config.getString("mechanics.xp-storer.block", "MOB_SPAWNER"));

        config.setComment("mechanics.xp-storer.require-sneaking-state", "Sets how the player must be sneaking in order to use the XP Storer.");
        xpStorerSneaking = TernaryState.getFromString(config.getString("mechanics.xp-storer.require-sneaking-state", "no"));


        /* Vehicle Configuration */


        // Vehicles Minecart Decay Configuration Listener
        config.setComment("vehicles.minecart.decay-when-empty.enable", "Enables the Minecart Decay Mechanic.");
        minecartDecayEnabled = config.getBoolean("vehicles.minecart.decay-when-empty.enable", false);

        config.setComment("vehicles.minecart.decay-when-empty.time-in-ticks", "The time in ticks that the cart will wait before decaying.");
        minecartDecayTime = config.getInt("vehicles.minecart.decay-when-empty.time-in-ticks", 20);


        // Vehicles Minecart Station Configuration Listener
        config.setComment("vehicles.minecart.mechanisms.station.enable", "Enables the Minecart Station Mechanic.");
        minecartStationEnabled = config.getBoolean("vehicles.minecart.mechanisms.station.enable", false);

        config.setComment("vehicles.minecart.mechanisms.station.block", "Sets the block that is the base of the station mechanic.");
        minecartStationBlock = new ItemInfo(config.getString("vehicles.minecart.mechanisms.station.block", "OBSIDIAN:0"));


        // Vehicles Minecart Sorter Configuration Listener
        config.setComment("vehicles.minecart.mechanisms.sorter.enable", "Enables the Minecart Sorter Mechanic.");
        minecartSorterEnabled = config.getBoolean("vehicles.minecart.mechanisms.sorter.enable", false);

        config.setComment("vehicles.minecart.mechanisms.sorter.block", "Sets the block that is the base of the sorter mechanic.");
        minecartSorterBlock = new ItemInfo(config.getString("vehicles.minecart.mechanisms.sorter.block", "NETHERRACK:0"));


        // Vehicles Minecart Ejector Configuration Listener
        config.setComment("vehicles.minecart.mechanisms.ejector.enable", "Enables the Minecart Ejector Mechanic.");
        minecartEjectorEnabled = config.getBoolean("vehicles.minecart.mechanisms.ejector.enable", false);

        config.setComment("vehicles.minecart.mechanisms.ejector.block", "Sets the block that is the base of the ejector mechanic.");
        minecartEjectorBlock = new ItemInfo(config.getString("vehicles.minecart.mechanisms.ejector.block", "IRON_BLOCK:0"));


        // Vehicles Minecart Deposit Configuration Listener
        config.setComment("vehicles.minecart.mechanisms.deposit.enable", "Enables the Minecart Deposit Mechanic.");
        minecartDepositEnabled = config.getBoolean("vehicles.minecart.mechanisms.deposit.enable", false);

        config.setComment("vehicles.minecart.mechanisms.deposit.block", "Sets the block that is the base of the deposit mechanic.");
        minecartDepositBlock = new ItemInfo(config.getString("vehicles.minecart.mechanisms.deposit.block", "IRON_ORE:0"));


        // Vehicles Minecart Teleport Configuration Listener
        config.setComment("vehicles.minecart.mechanisms.teleport.enable", "Enables the Minecart Teleport Mechanic.");
        minecartTeleportEnabled = config.getBoolean("vehicles.minecart.mechanisms.teleport.enable", false);

        config.setComment("vehicles.minecart.mechanisms.teleport.block", "Sets the block that is the base of the teleport mechanic.");
        minecartTeleportBlock = new ItemInfo(config.getString("vehicles.minecart.mechanisms.teleport.block", "EMERALD_BLOCK:0"));


        // Vehicles Minecart Lift Configuration Listener
        config.setComment("vehicles.minecart.mechanisms.elevator.enable", "Enables the Minecart Elevator Mechanic.");
        minecartElevatorEnabled = config.getBoolean("vehicles.minecart.mechanisms.elevator.enable", false);

        config.setComment("vehicles.minecart.mechanisms.elevator.block", "Sets the block that is the base of the elevator mechanic.");
        minecartElevatorBlock = new ItemInfo(config.getString("vehicles.minecart.mechanisms.elevator.block", "NETHER_BRICK:0"));


        // Vehicles Minecart Messager Configuration Listener
        config.setComment("vehicles.minecart.mechanisms.messager.enable", "Enables the Minecart Messager Mechanic.");
        minecartMessagerEnabled = config.getBoolean("vehicles.minecart.mechanisms.messager.enable", false);

        config.setComment("vehicles.minecart.mechanisms.messager.block", "Sets the block that is the base of the messager mechanic.");
        minecartMessagerBlock = new ItemInfo(config.getString("vehicles.minecart.mechanisms.messager.block", "ENDER_STONE:0"));


        // Vehicles Minecart Reverse Configuration Listener
        config.setComment("vehicles.minecart.mechanisms.reverse.enable", "Enables the Minecart Reverse Mechanic.");
        minecartReverseEnabled = config.getBoolean("vehicles.minecart.mechanisms.reverse.enable", false);

        config.setComment("vehicles.minecart.mechanisms.reverse.block", "Sets the block that is the base of the reverse mechanic.");
        minecartReverseBlock = new ItemInfo(config.getString("vehicles.minecart.mechanisms.reverse.block", "WOOL:0"));


        // Vehicles Minecart MaxSpeed Configuration Listener
        config.setComment("vehicles.minecart.mechanisms.max-speed.enable", "Enables the Minecart Max Speed Mechanic.");
        minecartMaxSpeedEnabled = config.getBoolean("vehicles.minecart.mechanisms.max-speed.enable", false);

        config.setComment("vehicles.minecart.mechanisms.max-speed.block", "Sets the block that is the base of the max speed mechanic.");
        minecartMaxSpeedBlock = new ItemInfo(config.getString("vehicles.minecart.mechanisms.max-speed.block", "COAL_BLOCK:0"));


        // Vehicles Minecart SpeedMod Configuration Listener
        config.setComment("vehicles.minecart.mechanisms.speed-modifier.enable", "Enables the Minecart Speed Modifier Block Mechanic.");
        minecartSpeedModEnabled = config.getBoolean("vehicles.minecart.mechanisms.speed-modifier.enable", false);

        config.setComment("vehicles.minecart.mechanisms.speed-modifier.max-boost-block", "Sets the block that is the base of the max boost block.");
        minecartSpeedModMaxBoostBlock = new ItemInfo(config.getString("vehicles.minecart.mechanisms.speed-modifier.max-boost-block", "GOLD_BLOCK:0"));

        config.setComment("vehicles.minecart.mechanisms.speed-modifier.25x-boost-block", "Sets the block that is the base of the 25x boost block.");
        minecartSpeedMod25xBoostBlock = new ItemInfo(config.getString("vehicles.minecart.mechanisms.speed-modifier.25x-boost-block", "GOLD_ORE:0"));

        config.setComment("vehicles.minecart.mechanisms.speed-modifier.50x-slow-block", "Sets the block that is the base of the 50x slower block.");
        minecartSpeedMod50xSlowBlock = new ItemInfo(config.getString("vehicles.minecart.mechanisms.speed-modifier.50x-slow-block", "SOUL_SAND:0"));

        config.setComment("vehicles.minecart.mechanisms.speed-modifier.20x-slow-block", "Sets the block that is the base of the 20x slower block.");
        minecartSpeedMod20xSlowBlock = new ItemInfo(config.getString("vehicles.minecart.mechanisms.speed-modifier.20x-slow-block", "GRAVEL:0"));


        // Vehicles Minecart Dispenser Configuration Listener
        config.setComment("vehicles.minecart.mechanisms.dispenser.enable", "Enables the Minecart Dispenser Mechanic.");
        minecartDispenserEnabled = config.getBoolean("vehicles.minecart.mechanisms.dispenser.enable", false);

        config.setComment("vehicles.minecart.mechanisms.dispenser.block", "Sets the block that is the base of the dispenser mechanic.");
        minecartDispenserBlock = new ItemInfo(config.getString("vehicles.minecart.mechanisms.dispenser.block", "EMERALD_ORE:0"));

        config.setComment("vehicles.minecart.mechanisms.dispenser.spawn-infront", "Sets whether the minecarts should spawn infront of the mechanic instead of directly above.");
        minecartDispenserLegacy = config.getBoolean("vehicles.minecart.mechanisms.dispenser.spawn-infront", false);

        config.setComment("vehicles.minecart.mechanisms.dispenser.check-for-carts", "Sets whether or not the mechanic checks for existing carts before spawning a new one.");
        minecartDispenserAntiSpam = config.getBoolean("vehicles.minecart.mechanisms.dispenser.check-for-carts", true);

        config.setComment("vehicles.minecart.mechanisms.dispenser.propel-cart", "Sets whether or not the dispenser propels carts that it spawns.");
        minecartDispenserPropel = config.getBoolean("vehicles.minecart.mechanisms.dispenser.propel-cart", false);


        // Vehicles Minecart Fall Speed Listener
        config.setComment("vehicles.minecart.fall-speed.enable", "Enables the Fall Speed Changer mechanic.");
        minecartFallModifierEnabled = config.getBoolean("vehicles.minecart.fall-speed.enable", false);

        config.setComment("vehicles.minecart.fall-speed.vertical-fall-speed", "Sets the vertical fall speed of the minecart");
        minecartFallVerticalSpeed = config.getDouble("vehicles.minecart.fall-speed.vertical-fall-speed", 0.9D);

        config.setComment("vehicles.minecart.fall-speed.horizontal-fall-speed", "Sets the horizontal fall speed of the minecart");
        minecartFallHorizontalSpeed = config.getDouble("vehicles.minecart.fall-speed.horizontal-fall-speed", 1.1D);


        // Vehicles Minecart More Rails Listener
        config.setComment("vehicles.minecart.more-rails.enable", "Enables the More Rails mechanic.");
        minecartMoreRailsEnabled = config.getBoolean("vehicles.minecart.more-rails.enable", false);

        config.setComment("vehicles.minecart.more-rails.pressure-plate-intersection", "Enables the pressure plate as an intersection.");
        minecartMoreRailsPressurePlate = config.getBoolean("vehicles.minecart.more-rails.pressure-plate-intersection", false);

        config.setComment("vehicles.minecart.more-rails.ladder-vertical-rail", "Enables the ladder as a vertical rail.");
        minecartMoreRailsLadder = config.getBoolean("vehicles.minecart.more-rails.ladder-vertical-rail", false);

        config.setComment("vehicles.minecart.more-rails.ladder-vertical-rail-velocity", "Sets the velocity applied to the minecart on vertical rails.");
        minecartMoreRailsLadderVelocity = config.getDouble("vehicles.minecart.more-rails.ladder-vertical-rail-velocity", 0.5D);


        // Vehicles Minecart Remove Entities Listener
        config.setComment("vehicles.minecart.remove-entities.enable", "Enables the Remove Entities mechanic for minecarts.");
        minecartRemoveEntitiesEnabled = config.getBoolean("vehicles.minecart.remove-entities.enable", false);

        config.setComment("vehicles.minecart.remove-entities.remove-other-minecarts", "Allows the remove entities mechanic to remove other minecarts.");
        minecartRemoveEntitiesOtherCarts = config.getBoolean("vehicles.minecart.remove-entities.remove-other-minecarts", false);

        config.setComment("vehicles.minecart.remove-entities.allow-empty-carts", "Allows the cart to be empty.");
        minecartRemoveEntitiesEmpty = config.getBoolean("vehicles.minecart.remove-entities.allow-empty-carts", false);


        // Vehicles Minecart Vision Steering Listener
        config.setComment("vehicles.minecart.vision-steering.enable", "Enables the Vision Steering mechanic.");
        minecartVisionSteeringEnabled = config.getBoolean("vehicles.minecart.vision-steering.enable", false);

        config.setComment("vehicles.minecart.vision-steering.minimum-sensitivity", "Sets the sensitivity of Vision Steering.");
        minecartVisionSteeringMinimumSensitivity = config.getInt("vehicles.minecart.vision-steering.minimum-sensitivity", 3);


        // Vehicles Minecart Block Mob Entry Listener
        config.setComment("vehicles.minecart.block-mob-entry.enable", "Enables the Block Mob Entry mechanic.");
        minecartBlockMobEntryEnabled = config.getBoolean("vehicles.minecart.block-mob-entry.enable", false);


        // Vehicles Minecart Remove On Exit Listener
        config.setComment("vehicles.minecart.remove-on-exit.enable", "Enables the Remove on Exit mechanic.");
        minecartRemoveOnExitEnabled = config.getBoolean("vehicles.minecart.remove-on-exit.enable", false);

        config.setComment("vehicles.minecart.remove-on-exit.give-item", "Sets whether to give the player the item back or not.");
        minecartRemoveOnExitGiveItem = config.getBoolean("vehicles.minecart.remove-on-exit.give-item", false);


        // Vehicles Minecart Collision Entry Listener
        config.setComment("vehicles.minecart.collision-entry.enable", "Enables the Collision Entry mechanic.");
        minecartCollisionEntryEnabled = config.getBoolean("vehicles.minecart.collision-entry.enable", false);


        // Vehicles Minecart Item Pickup Listener
        config.setComment("vehicles.minecart.item-pickup.enable", "Enables the Item Pickup mechanic.");
        minecartItemPickupEnabled = config.getBoolean("vehicles.minecart.item-pickup.enable", false);


        // Vehicles Minecart Constant Speed Listener
        config.setComment("vehicles.minecart.constant-speed.enable", "Enables the Constant Speed mechanic.");
        minecartConstantSpeedEnable = config.getBoolean("vehicles.minecart.constant-speed.enable", false);

        config.setComment("vehicles.minecart.constant-speed.speed", "Sets the speed to move at constantly.");
        minecartConstantSpeedSpeed = config.getDouble("vehicles.minecart.constant-speed.speed", 0.5);


        // Vehicles Minecart Rail Placer Listener
        config.setComment("vehicles.minecart.rail-placer.enable", "Enables the Rail Placer mechanic.");
        minecartRailPlacerEnable = config.getBoolean("vehicles.minecart.rail-placer.enable", false);


        // Vehicles Minecart Speed Modifier Listener
        config.setComment("vehicles.minecart.speed-modifiers.enable", "Enables the Speed Modifiers mechanic.");
        minecartSpeedModifierEnable = config.getBoolean("vehicles.minecart.speed-modifiers.enable", false);

        config.setComment("vehicles.minecart.speed-modifiers.max-speed", "Sets the max speed of carts. Normal max speed speed is 0.4D");
        minecartSpeedModifierMaxSpeed = config.getDouble("vehicles.minecart.speed-modifiers.max-speed", 0.4);

        config.setComment("vehicles.minecart.speed-modifiers.off-rail-speed", "Sets the off-rail speed modifier of carts. 0 is none.");
        minecartSpeedModifierOffRail = config.getDouble("vehicles.minecart.speed-modifiers.off-rail-speed", 0);


        // Vehicles Minecart Configuration Listener
        config.setComment("vehicles.minecart.empty-slowdown-stopper.enable", "Enables the Empty Slowdown Stopper mechanic.");
        minecartEmptySlowdownStopperEnable = config.getBoolean("vehicles.minecart.empty-slowdown-stopper.enable", false);


        // Vehicles Minecart No Collide Listener
        config.setComment("vehicles.minecart.no-collide.enable", "Enables the No Collide mechanic.");
        minecartNoCollideEnable = config.getBoolean("vehicles.minecart.no-collide.enable", false);

        config.setComment("vehicles.minecart.no-collide.empty-carts", "Enable No Collide for empty carts.");
        minecartNoCollideEmpty = config.getBoolean("vehicles.minecart.no-collide.empty-carts", true);

        config.setComment("vehicles.minecart.no-collide.full-carts", "Enable No Collide for occupied carts.");
        minecartNoCollideFull = config.getBoolean("vehicles.minecart.no-collide.full-carts", false);


        // Vehicles Minecart Place Anywhere Listener
        config.setComment("vehicles.minecart.place-anywhere.enable", "Enables the Place Anywhere mechanic.");
        minecartPlaceAnywhereEnable = config.getBoolean("vehicles.minecart.place-anywhere.enable", false);


        // Vehicles Minecart Temporary Cart Listener
        config.setComment("vehicles.minecart.temporary-cart.enable", "Enables the Temporary Cart mechanic.");
        minecartTemporaryCartEnable = config.getBoolean("vehicles.minecart.temporary-cart.enable", false);


        // Vehicles - Boat Drops Listener
        config.setComment("vehicles.boat.drops.enable", "Enables the Boat Drops Mechanic.");
        boatDropsEnabled = config.getBoolean("vehicles.boat.drops.enable", false);


        // Vehicles Boat Remove On Exit Listener
        config.setComment("vehicles.boat.remove-on-exit.enable", "Enables the Remove On Exit mechanic.");
        boatRemoveOnExitEnabled = config.getBoolean("vehicles.boat.remove-on-exit.enable", false);

        config.setComment("vehicles.boat.remove-on-exit.give-item", "Sets whether to give the player the item back or not.");
        boatRemoveOnExitGiveItem = config.getBoolean("vehicles.boat.remove-on-exit.give-item", false);


        // Vehicles Boat Land Boats Listener
        config.setComment("vehicles.boat.land-boats.enable", "Enables the Land Boats mechanic.");
        boatLandBoatsEnable = config.getBoolean("vehicles.boat.land-boats.enable", false);


        // Vehicles - Boat Remove Entities Listener
        config.setComment("vehicles.boat.remove-entities.enable", "Enables the Remove Entities mechanic for boats.");
        boatRemoveEntitiesEnabled = config.getBoolean("vehicles.boat.remove-entities.enable", false);

        config.setComment("vehicles.boat.remove-entities.remove-other-boats", "Allows the remove entities boats to remove other boats.");
        boatRemoveEntitiesOtherBoats = config.getBoolean("vehicles.boat.remove-entities.remove-other-boats", false);


        // Vehicles Boat Speed Modifier Listener
        config.setComment("vehicles.boat.speed-modifiers.enable", "Enables the Speed Modifiers mechanic.");
        boatSpeedModifierEnable = config.getBoolean("vehicles.boat.speed-modifiers.enable", false);

        config.setComment("vehicles.boat.speed-modifiers.max-speed", "Sets the maximum speed of a boat. 0.4D is normal maximum speed.");
        boatSpeedModifierMaxSpeed = config.getDouble("vehicles.boat.speed-modifiers.max-speed", 0.4D);

        config.setComment("vehicles.boat.speed-modifiers.unnoccupied-deceleration", "Sets the unnoccupied deceleration of a boat. -1 is disabled.");
        boatSpeedModifierUnnoccupiedDeceleration = config.getDouble("vehicles.boat.speed-modifiers.unnoccupied-deceleration", -1);

        config.setComment("vehicles.boat.speed-modifiers.occupied-deceleration", "Sets the occupied deceleration of a boat. 0.3 is normal occupied deceleration");
        boatSpeedModifierOccupiedDeceleration = config.getDouble("vehicles.boat.speed-modifiers.occupied-deceleration", 0.2);


        // Vehicles - Boat Uncrashable Listener
        config.setComment("vehicles.boat.uncrashable.enable", "Enables the Boat Uncrashable mechanic.");
        boatNoCrashEnabled = config.getBoolean("vehicles.boat.uncrashable.enable", false);


        // Vehicles Boat Water Place Only Listener
        config.setComment("vehicles.boat.water-place-only.enable", "Enables the Boat Water Place Only mechanic.");
        boatWaterPlaceOnly = config.getBoolean("vehicles.boat.water-place-only.enable", false);


        config.save(); //Save all the added values.
    }
}