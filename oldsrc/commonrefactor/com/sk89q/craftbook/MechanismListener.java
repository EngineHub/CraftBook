// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.craftbook;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

import com.sk89q.craftbook.access.*;
import com.sk89q.craftbook.blockbag.*;
import com.sk89q.craftbook.mech.*;
import com.sk89q.craftbook.mech.area.*;
import com.sk89q.craftbook.util.*;
import com.sk89q.craftbook.util.Vector;

/**
 * Listener for mechanisms.
 * 
 * @author sk89q
 */
public class MechanismListener extends CraftBookDelegateListener {
    /**
     * Tracks copy saves to prevent flooding.
     */
    private Map<String,Long> lastCopySave =
            new HashMap<String,Long>();
    
    private boolean checkPermissions;
    private boolean checkCreatePermissions;
    private boolean redstoneToggleAreas = true;
    private int maxToggleAreaSize;
    private int maxUserToggleAreas;
    private boolean useBookshelves = true;
    private String bookReadLine;
    private Cauldron cauldronModule;
    private boolean useElevators = true;
    private boolean useGates = true;
    private boolean redstoneGates = true;
    private boolean useLightSwitches = true;
    private boolean useBridges = true;
    private boolean redstoneBridges = true;
    private boolean useDoors = true;
    private boolean redstoneDoors = true;
    private boolean useHiddenSwitches = true;
    private boolean useToggleAreas;
    private boolean dropBookshelves = true;
    private double dropAppleChance = 0;
    private boolean enableAmmeter = true;

    private Bridge.BridgeSettings bridgeSettings = new Bridge.BridgeSettings();
    private Door.DoorSettings doorSettings = new Door.DoorSettings();
    private LightSwitch light = new LightSwitch();
    
    /**
     * Construct the object.
     * 
     * @param craftBook
     * @param listener
     */
    public MechanismListener(CraftBookCore craftBook, ServerInterface server) {
        super(craftBook, server);
    }

    /**
     * Loads CraftBooks's configuration from file.
     */
    public void loadConfiguration() {
        Configuration c = server.getConfiguration();
        
        maxToggleAreaSize = Math.max(0, c.getInt("toggle-area-max-size", 5000));
        maxUserToggleAreas = Math.max(0, c.getInt("toggle-area-max-per-user", 30));

        useBookshelves = c.getBoolean("bookshelf-enable", true);
        bookReadLine = c.getString("bookshelf-read-text", "You pick out a book...");
        useLightSwitches = c.getBoolean("light-switch-enable", true);
        useGates = c.getBoolean("gate-enable", true);
        redstoneGates = c.getBoolean("gate-redstone", true);
        useElevators = c.getBoolean("elevators-enable", true);
        useBridges = c.getBoolean("bridge-enable", true);
        redstoneBridges = c.getBoolean("bridge-redstone", true);
        useHiddenSwitches = c.getBoolean("door-enable", true);
        bridgeSettings.allowedBlocks = CraftBookUtil.toBlockIDSet(c,c.getString("bridge-blocks", "4,5,20,43"));
        bridgeSettings.maxLength = c.getInt("bridge-max-length", 30);
        useDoors = c.getBoolean("door-enable", true);
        redstoneDoors = c.getBoolean("door-redstone", true);
        doorSettings.allowedBlocks = CraftBookUtil.toBlockIDSet(c,c.getString("door-blocks", "1,3,4,5,17,20,35,43,44,45,47,80,82"));
        doorSettings.maxLength = c.getInt("door-max-length", 30);
        dropBookshelves = c.getBoolean("drop-bookshelves", true);
        try {
            dropAppleChance = Double.parseDouble(c.getString("apple-drop-chance", "0.5")) / 100.0;
        } catch (NumberFormatException e) {
            dropAppleChance = -1;
            server.getLogger().warning("Invalid apple drop chance setting in craftbook.properties");
        }
        useHiddenSwitches = c.getBoolean("hidden-switches-enable", true);
        useToggleAreas = c.getBoolean("toggle-areas-enable", true);
        redstoneToggleAreas = c.getBoolean("toggle-areas-redstone", true);
        checkPermissions = c.getBoolean("check-permissions", false);
        checkCreatePermissions = c.getBoolean("check-create-permissions", false);
        cauldronModule = null;
        enableAmmeter = c.getBoolean("ammeter", true);

        loadCauldron();
    }
    
    /**
     * Load the cauldron.
     */
    private void loadCauldron() {
        Configuration c = server.getConfiguration();
        
        if (c.getBoolean("cauldron-enable", true)) {
            try {
                CauldronCookbook recipes = readCauldronRecipes("cauldron-recipes.txt");

                if (recipes.size() != 0) {
                    cauldronModule = new Cauldron(recipes);
                    server.getLogger().info(recipes.size()
                            + " cauldron recipe(s) loaded");
                } else {
                    server.getLogger().warning("cauldron-recipes.txt had no recipes");
                }
            } catch (FileNotFoundException e) {
                server.getLogger().info("cauldron-recipes.txt not found: " + e.getMessage());
                try {
                    server.getLogger().info("Looked in: " + (new File(".")).getCanonicalPath());
                } catch (IOException ioe) {
                    // Eat error
                }
            } catch (IOException e) {
                server.getLogger().warning("cauldron-recipes.txt not loaded: " + e.getMessage());
            }
        } else {
            cauldronModule = null;
        }
    }

    /**
     * Called before the console command is parsed. Return true if you don't
     * want the server command to be parsed by the server.
     * 
     * @param split
     * @return false if you want the command to be parsed.
     */
    public boolean onConsoleCommand(String[] split) {
        if (split[0].equalsIgnoreCase("reload-cauldron")) {
            loadCauldron();
            return true;
        }
        
        return false;
    }

    /**
     * Handles the wire input at a block in the case when the wire is
     * directly connected to the block in question only.
     *
     * @param x
     * @param y
     * @param z
     * @param isOn
     */
    public void onWireInput(final WorldInterface world, final Vector pt, 
            final boolean isOn, final Vector changed) {
        
        int type = world.getId(pt);
        
        // Sign gates
        if (type == BlockType.WALL_SIGN
                || type == BlockType.SIGN_POST) {
            BlockEntity cblock = world.getBlockEntity(
                    pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

            if (!(cblock instanceof SignInterface)) {
                return;
            }

            final SignInterface sign = (SignInterface)cblock;
            final String line2 = sign.getLine2();

            // Gate
            if (useGates && redstoneGates
                    && (line2.equalsIgnoreCase("[Gate]")
                    || line2.equalsIgnoreCase("[DGate]"))) {
                BlockBag bag = craftBook.getBlockBag(world,pt);
                bag.addSourcePosition(world,pt);

                // A gate may toggle or not
                try {
                    GateSwitch.setGateState(world, pt, bag, isOn,
                            line2.equalsIgnoreCase("[DGate]"));
                } catch (BlockBagException e) {
                }

            // Bridges
            } else if (useBridges != false
                    && redstoneBridges
                    && type == BlockType.SIGN_POST
                    && line2.equalsIgnoreCase("[Bridge]")) {
                world.delayAction(
                    new Action(world, pt.toBlockVector(), 2) {
                        @Override 
                        public void run() {
                            BlockBag bag = craftBook.getBlockBag(world, pt);
                            bag.addSourcePosition(world, pt);
                            
                            Bridge bridge = new Bridge(server, world, pt, bridgeSettings);
                            if (isOn) {
                                bridge.setActive(bag);
                            } else {
                                bridge.setInactive(bag);
                            }
                        }
                    }
                );

            // Doors
            } else if (useDoors != false
                    && redstoneDoors
                    && type == BlockType.SIGN_POST
                    && (line2.equalsIgnoreCase("[Door Up]")
                        || line2.equalsIgnoreCase("[Door Down]"))) {
                world.delayAction(
                    new Action(world, pt.toBlockVector(), 2) {
                        @Override
                        public void run() {
                            BlockBag bag = craftBook.getBlockBag(world,pt);
                            bag.addSourcePosition(world,pt);
                            
                            Door door = new Door(server,world,pt,doorSettings);
                            if (isOn) {
                                door.setActive(bag);
                            } else {
                                door.setInactive(bag);
                            }
                        }
                    });

            // Toggle areas
            } else if (useToggleAreas && redstoneToggleAreas
                    && (line2.equalsIgnoreCase("[Toggle]")
                    || line2.equalsIgnoreCase("[Area]"))) {                
                world.delayAction(
                    new Action(world,pt.toBlockVector(), 2) {
                        @Override
                        public void run() {
                            BlockBag bag = craftBook.getBlockBag(world,pt);
                            bag.addSourcePosition(world,pt);

                            ToggleArea area = new ToggleArea(server, world, pt, craftBook.getCopyManager());
                            
                            if (isOn) { 
                                area.setActive(bag);
                            } else {
                                area.setInactive(bag);
                            }
                        }
                    });
            }
        }
    }

    /**
     * Called when a block is hit with the primary attack.
     * 
     * @param player
     * @param block
     * @return
     */
    @Override
    public boolean onBlockDestroy(WorldInterface world, PlayerInterface p, Vector block, int status) {
        // Random apple drops
        if (dropAppleChance > 0 && world.getId(block) == BlockType.LEAVES
                && checkObjectUse(p,"appledrops")) {
            if (status == 3) {
                if (Math.random() <= dropAppleChance) {
                    world.dropItem(
                        block.getX(), block.getY(), block.getZ(),
                        ItemType.APPLE, 1);
                }
            }

        // Bookshelf drops
        } else if (dropBookshelves && world.getId(block) == BlockType.BOOKCASE
                && checkObjectUse(p,"bookshelfdrops")) {
            if (status == 3) {
                world.dropItem(
                        block.getX(), block.getY(), block.getZ(),
                        BlockType.BOOKCASE, 1);
            }
        }

        return false;
    }

    /**
     * Called when a sign is updated.
     * @param player
     * @param cblock
     * @return
     */
    public boolean onSignChange(PlayerInterface player, WorldInterface world, Vector v, SignInterface s) {
        String line2 = s.getLine2();
        
        // Gate
        if (line2.equalsIgnoreCase("[Gate]") || line2.equalsIgnoreCase("[DGate]")) {
            if (checkCreatePermissions && !checkObjectCreate(player,"gate")) {
                player.sendMessage(Colors.RED
                        + "You don't have permission to make gates.");
                MinecraftUtil.dropSign(world, s.getX(), s.getY(), s.getZ());
                return true;
            }
            
            s.setLine2(line2.equalsIgnoreCase("[Gate]") ? "[Gate]" : "[DGate]");
            s.flushChanges();
            
            craftBook.informUser(player);
            
            if (useGates) {
                player.sendMessage(Colors.GOLD + "Gate created!");
            } else {
                player.sendMessage(Colors.RED + "Gates are disabled on this server.");
            }
            
        // Light switch
        } else if (line2.equalsIgnoreCase("[|]")
                || line2.equalsIgnoreCase("[I]")) {
            if (checkCreatePermissions && !checkObjectCreate(player,"lightswitch")) {
                player.sendMessage(Colors.RED
                        + "You don't have permission to make light switches.");
                MinecraftUtil.dropSign(world, s.getX(), s.getY(), s.getZ());
                return true;
            }
            
            s.setLine2("[I]");
            s.flushChanges();
            
            craftBook.informUser(player);
            
            if (useLightSwitches) {
                player.sendMessage(Colors.GOLD + "Light switch created!");
            } else {
                player.sendMessage(Colors.RED + "Light switches are disabled on this server.");
            }

        // Elevator
        } else if (line2.equalsIgnoreCase("[Lift Up]")
                || line2.equalsIgnoreCase("[Lift Down]")
                || line2.equalsIgnoreCase("[Lift]")) {
            if (checkCreatePermissions && !checkObjectCreate(player,"elevator")) {
                player.sendMessage(Colors.RED
                        + "You don't have permission to make elevators.");
                MinecraftUtil.dropSign(world, s.getX(), s.getY(), s.getZ());
                return true;
            }

            if (line2.equalsIgnoreCase("[Lift Up]")) {
                s.setLine2("[Lift Up]");
            } else if (line2.equalsIgnoreCase("[Lift Down]")) {
                s.setLine2("[Lift Down]");
            } else if (line2.equalsIgnoreCase("[Lift]")) {
                s.setLine2("[Lift]");
            }
            s.flushChanges();
            
            craftBook.informUser(player);
            
            if (useElevators) {
                if (line2.equalsIgnoreCase("[Lift Up]")) {
                    if (Elevator.hasLinkedLift(world, v, true)) {
                        player.sendMessage(Colors.GOLD
                                + "Elevator created and linked!");
                    } else {
                        player.sendMessage(Colors.GOLD
                                + "Elevator created but not yet linked to an existing lift sign.");
                    }
                } else if (line2.equalsIgnoreCase("[Lift Down]")) {
                    if (Elevator.hasLinkedLift(world, v, false)) {
                        player.sendMessage(Colors.GOLD
                                + "Elevator created and linked!");
                    } else {
                        player.sendMessage(Colors.GOLD
                                + "Elevator created but not yet linked to an existing lift sign.");
                    }
                } else if (line2.equalsIgnoreCase("[Lift]")) {
                    if (Elevator.hasLinkedLift(world, v, true)
                            || Elevator.hasLinkedLift(world, v, false)) {
                        player.sendMessage(Colors.GOLD
                                + "Elevator created and linked!");
                    } else {
                        player.sendMessage(Colors.GOLD
                                + "Elevator created but not yet linked to an existing lift sign.");
                    }
                }
            } else {
                player.sendMessage(Colors.RED + "Elevators are disabled on this server.");
            }
        
        // Toggle areas
        } else if (line2.equalsIgnoreCase("[Area]")
                || line2.equalsIgnoreCase("[Toggle]")) {
            craftBook.informUser(player);
            
            if (useToggleAreas) {
                if (checkObjectCreate(player,"togglearea")
                        && ToggleArea.validateEnvironment(server, world, player, v)) {
                    s.flushChanges();
                } else {
                    MinecraftUtil.dropSign(world,v.getBlockX(),v.getBlockY(),v.getBlockZ());
                }
            } else {
                player.printError("Area toggles are disabled on this server.");
            }

        // Bridges
        } else if (line2.equalsIgnoreCase("[Bridge]")) {
            craftBook.informUser(player);

            if (useBridges) {
                if (checkObjectCreate(player,"bridge")
                        && Bridge.validateEnvironment(player, v, s)) {
                    s.flushChanges();
                } else {
                    MinecraftUtil.dropSign(world,v.getBlockX(),v.getBlockY(),v.getBlockZ());
                }
            } else {
                player.sendMessage(Colors.RED + "Bridges are disabled on this server.");
            }

        // Doors
        } else if (line2.equalsIgnoreCase("[Door Up]")
                || line2.equalsIgnoreCase("[Door Down]")) {
            craftBook.informUser(player);

            if (useDoors) {
                if (checkObjectCreate(player,"door")
                        && Door.validateEnvironment(player, v, s)) {
                    s.flushChanges();
                } else {
                    MinecraftUtil.dropSign(world,v.getBlockX(),v.getBlockY(),v.getBlockZ());
                }
            } else {
                player.sendMessage(Colors.RED + "Doors are disabled on this server.");
            }
        }

        return false;
    }

    /**
     * Called when a block is being attempted to be placed.
     * 
     * @param player
     * @param blockClicked
     * @param itemInHand
     * @return
     */
    @Override
    public void onBlockRightClicked(WorldInterface world, PlayerInterface player, 
            Vector block, int itemInHand) {
        try {
            // Discriminate against attempts that would actually place blocks
            boolean isPlacingBlock = itemInHand >= 1
                    && itemInHand <= 256;
            // 1 to work around empty hands bug in hMod
            
            if (!isPlacingBlock) {
                handleBlockUse(world, player, block, itemInHand);
            }
        } catch (OutOfBlocksException e) {
            player.sendMessage(Colors.RED + "Uh oh! Ran out of: " + CraftBookUtil.toBlockName(e.getID()));
            player.sendMessage(Colors.RED + "Make sure nearby block sources have the necessary");
            player.sendMessage(Colors.RED + "materials.");
        } catch (OutOfSpaceException e) {
            player.sendMessage(Colors.RED + "No room left to put: " + CraftBookUtil.toBlockName(e.getID()));
            player.sendMessage(Colors.RED + "Make sure nearby block sources have free slots.");
        } catch (BlockBagException e) {
            player.sendMessage(Colors.RED + "Error: " + e.getMessage());
        }
    }

    /**
     * Called when a block is being attempted to be placed.
     * 
     * @param player
     * @param blockClicked
     * @param itemInHand
     * @return
     */
    private boolean handleBlockUse(WorldInterface world, PlayerInterface player, 
            Vector blockClicked, int itemInHand)
            throws BlockBagException {

        int current = -1;

        int type = world.getId(blockClicked);
        int data = world.getData(blockClicked);
        
        // Ammeter
        if (enableAmmeter && itemInHand == 263) { // Coal
            
            if (type == BlockType.LEVER) {
                if ((data & 0x8) == 0x8) {
                    current = 15;
                }
                current = 0;
            } else if (type == BlockType.STONE_PRESSURE_PLATE) {
                if ((data & 0x1) == 0x1) {
                    current = 15;
                }
                current = 0;
            } else if (type == BlockType.WOODEN_PRESSURE_PLATE) {
                if ((data & 0x1) == 0x1) {
                    current = 15;
                }
                current = 0;
            } else if (type == BlockType.REDSTONE_TORCH_ON) {
                current = 15;
            } else if (type == BlockType.REDSTONE_TORCH_OFF) {
                current = 0;
            } else if (type == BlockType.STONE_BUTTON) {
                if ((data & 0x8) == 0x8) {
                    current = 15;
                }
                current = 0;
            } else if (type == BlockType.REDSTONE_WIRE) {
                current = data;
            }

            if (current > -1) {
                player.sendMessage(Colors.YELLOW + "Ammeter: "
                        + Colors.YELLOW + "["
                        + Colors.YELLOW + CraftBookUtil.repeatString("|", current)
                        + Colors.BLACK + CraftBookUtil.repeatString("|", 15 - current)
                        + Colors.YELLOW + "] "
                        + Colors.BLACK
                        + current + " A");
            } else {
                player.sendMessage(Colors.YELLOW + "Ammeter: " + Colors.DARK_RED + "Not supported.");
            }

            return false;
        }

        int plyX = (int)Math.floor(player.getX());
        int plyY = (int)Math.floor(player.getX());
        int plyZ = (int)Math.floor(player.getX());

        // Book reading
        if (useBookshelves
                && type == BlockType.BOOKCASE
                && checkObjectUse(player,"readbooks")) {
            BookReader.readBook(player, bookReadLine);
            return true;

        // Sign buttons
        } else if (type == BlockType.WALL_SIGN ||
                type == BlockType.SIGN_POST ||
                world.getId(plyX, plyY + 1, plyZ) == BlockType.WALL_SIGN ||
                world.getId(plyX, plyY, plyZ) == BlockType.WALL_SIGN) {
            int x = blockClicked.getBlockX();
            int y = blockClicked.getBlockY();
            int z = blockClicked.getBlockZ();

            // Because sometimes the player is *inside* the block with a sign,
            // it becomes impossible for the player to select the sign but
            // may try anyway, so we're fudging detection for this case
            Vector pt;
            if (type == BlockType.WALL_SIGN
                    || type == BlockType.SIGN_POST) {
                pt = new Vector(x, y, z);
            } else if (world.getId(plyX, plyY + 1, plyZ) == BlockType.WALL_SIGN) {
                pt = new Vector(plyX, plyY + 1, plyZ);
                x = plyX;
                y = plyY + 1;
                z = plyZ;
            } else {
                pt = new Vector(plyX, plyY, plyZ);
                x = plyX;
                y = plyY;
                z = plyZ;
            }

            BlockEntity cBlock = world.getBlockEntity(x, y, z);

            if (cBlock instanceof SignInterface) {
                SignInterface sign = (SignInterface)cBlock;
                String line2 = sign.getLine2();

                // Gate
                if (useGates
                        && (line2.equalsIgnoreCase("[Gate]")
                                || line2.equalsIgnoreCase("[DGate]"))
                        && checkObjectUse(player,"gate")) {
                    BlockBag bag = craftBook.getBlockBag(world,pt);
                    bag.addSourcePosition(world,pt);

                    // A gate may toggle or not
                    if (GateSwitch.toggleGates(world, pt, bag,
                            line2.equalsIgnoreCase("[DGate]"))) {
                        player.sendMessage(Colors.GOLD + "*screeetch* Gate moved!");
                    } else {
                        player.sendMessage(Colors.RED + "No nearby gate to toggle.");
                    }
                
                // Light switch
                } else if (useLightSwitches &&
                        (line2.equalsIgnoreCase("[|]") || line2.equalsIgnoreCase("[I]"))
                        && checkObjectUse(player,"lightswitch")) {
                    BlockBag bag = craftBook.getBlockBag(world,pt);
                    bag.addSourcePosition(world,pt);
                    
                    return light.toggleLights(world, pt, bag);

                // Elevator
                } else if (useElevators
                        && (line2.equalsIgnoreCase("[Lift Up]")
                        || line2.equalsIgnoreCase("[Lift Down]"))
                        && checkObjectUse(player,"elevator")) {

                    // Go up or down?
                    boolean up = line2.equalsIgnoreCase("[Lift Up]");
                    Elevator.performLift(player, world, pt, up);
                    
                    return true;

                // Toggle areas
                } else if (useToggleAreas != false
                        && (line2.equalsIgnoreCase("[Toggle]")
                        || line2.equalsIgnoreCase("[Area]"))
                        && checkObjectUse(player,"togglearea")) {
                    
                    BlockBag bag = craftBook.getBlockBag(world,pt);
                    bag.addSourcePosition(world,pt);
                    
                    ToggleArea area = new ToggleArea(server, world, pt, craftBook.getCopyManager());
                    area.playerToggle(player, bag);

                    // Tell the player of missing blocks
                    Map<Integer,Integer> missing = bag.getMissing();
                    if (missing.size() > 0) {
                        for (Map.Entry<Integer,Integer> entry : missing.entrySet()) {
                            player.sendMessage(Colors.RED + "Missing "
                                    + entry.getValue() + "x "
                                    + CraftBookUtil.toBlockName(entry.getKey()));
                        }
                    }
                    
                    return true;

                // Bridges
                } else if (useBridges
                        && type == BlockType.SIGN_POST
                        && line2.equalsIgnoreCase("[Bridge]")
                        && checkObjectUse(player,"bridge")) {
                    
                    BlockBag bag = craftBook.getBlockBag(world,pt);
                    bag.addSourcePosition(world,pt);
                    
                    Bridge bridge = new Bridge(server,world,pt, bridgeSettings);
                    bridge.playerToggleBridge(player, bag);
                    
                    return true;

                // Doors
                } else if (useDoors
                        && type == BlockType.SIGN_POST
                        && (line2.equalsIgnoreCase("[Door Up]")
                                || line2.equalsIgnoreCase("[Door Down]"))
                        && checkObjectUse(player,"door")) {
                    
                    BlockBag bag = craftBook.getBlockBag(world,pt);
                    bag.addSourcePosition(world,pt);
                    
                    Door door = new Door(server,world,pt,doorSettings);
                    door.playerToggleDoor(player, bag);
                    
                    return true;
                }
            }

        // Cauldron
        } else if (cauldronModule != null
                && checkObjectUse(player,"cauldron")) {
            
            int x = blockClicked.getBlockX();
            int y = blockClicked.getBlockY();
            int z = blockClicked.getBlockZ();

            cauldronModule.preCauldron(player, world, new Vector(x, y, z));

        }

        // Hidden switches
        if (useHiddenSwitches
                && itemInHand <= 0
                && type != BlockType.SIGN_POST
                && type != BlockType.WALL_SIGN
                && !BlockType.isRedstoneBlock(type)) {
            
            int x = blockClicked.getBlockX();
            int y = blockClicked.getBlockY();
            int z = blockClicked.getBlockZ();

            toggleHiddenSwitch(world, x, y - 1, z);
            toggleHiddenSwitch(world, x, y + 1, z);
            toggleHiddenSwitch(world, x - 1, y, z);
            toggleHiddenSwitch(world, x + 1, y, z);
            toggleHiddenSwitch(world, x, y, z - 1);
            toggleHiddenSwitch(world, x, y, z + 1);
            
            return true;
        }

        return false;
    }
    
    /**
     * Toggle a hidden switch.
     * 
     * @param pt
     */
    private void toggleHiddenSwitch(WorldInterface w, int x, int y, int z) {
        BlockEntity cblock = w.getBlockEntity(x, y, z);
        
        if (cblock instanceof SignInterface) {
            SignInterface sign = (SignInterface)cblock;
            
            if (sign.getLine2().equalsIgnoreCase("[X]")) {
                RedstoneUtil.toggleOutput(w, new Vector(x, y - 1, z));
                RedstoneUtil.toggleOutput(w, new Vector(x, y + 1, z));
                RedstoneUtil.toggleOutput(w, new Vector(x - 1, y, z));
                RedstoneUtil.toggleOutput(w, new Vector(x + 1, y, z));
                RedstoneUtil.toggleOutput(w, new Vector(x, y, z - 1));
                RedstoneUtil.toggleOutput(w, new Vector(x, y, z + 1));
            }
        }
    }
    
    /**
     * Called when a command is run
     *
     * @param player
     * @param split
     * @return whether the command was processed
     */
    @Override
    public boolean onCommand(PlayerInterface player, String[] split) {
        try {
            if ((split[0].equalsIgnoreCase("/savearea")
                    && player.canUseCommand("/savearea"))
                    || (split[0].equalsIgnoreCase("/savensarea")
                    && player.canUseCommand("/savensarea"))) {
                boolean namespaced = split[0].equalsIgnoreCase("/savensarea");
                
                CraftBookUtil.checkArgs(split, namespaced ? 2 : 1, -1, split[0]);
    
                String id;
                String namespace;
                
                if (namespaced) {
                    id = CraftBookUtil.joinString(split, " ", 2);
                    namespace = split[1];
    
                    if (namespace.equalsIgnoreCase("@")) {
                        namespace = "global";
                    } else {
                        if (!CopyManager.isValidNamespace(namespace)) {
                            player.sendMessage(Colors.RED + "Invalid namespace name. For the global namespace, use @");
                            return true;
                        }
                        namespace = "~" + namespace;
                    }
                } else {
                    id = CraftBookUtil.joinString(split, " ", 1);
                    String nameNamespace = player.getName();
                    
                    // Sign lines can only be 15 characters long while names
                    // can be up to 16 characters long
                    if (nameNamespace.length() > 15) {
                        nameNamespace = nameNamespace.substring(0, 15);
                    }
    
                    if (!CopyManager.isValidNamespace(nameNamespace)) {
                        player.sendMessage(Colors.RED + "You have an invalid player name.");
                        return true;
                    }
                    
                    namespace = "~" + nameNamespace;
                }
    
                if (!CopyManager.isValidName(id)) {
                    player.sendMessage(Colors.RED + "Invalid area name.");
                    return true;
                }
                
                try {
                    Vector min = server.getWorldEditBridge().getRegionMinimumPoint(player);
                    Vector max = server.getWorldEditBridge().getRegionMaximumPoint(player);
                    Vector size = max.subtract(min).add(1, 1, 1);
    
                    // Check maximum size
                    if (size.getBlockX() * size.getBlockY() * size.getBlockZ() > maxToggleAreaSize) {
                        player.sendMessage(Colors.RED + "Area is larger than allowed "
                                + maxToggleAreaSize + " blocks.");
                        return true;
                    }
                    
                    // Check to make sure that a user doesn't have too many toggle
                    // areas (to prevent flooding the server with files)
                    if (maxUserToggleAreas >= 0 && !namespace.equals("global")) {
                        int count = craftBook.getCopyManager().meetsQuota(player.getWorld(),
                                namespace, id, maxUserToggleAreas);
    
                        if (count > -1) {
                            player.sendMessage(Colors.RED + "You are limited to "
                                    + maxUserToggleAreas + " toggle area(s). You have "
                                    + count + " areas.");
                            return true;
                        }
                    }
    
                    // Prevent save flooding
                    Long lastSave = lastCopySave.get(player.getName());
                    long now = System.currentTimeMillis();
                    
                    if (lastSave != null) {
                        if (now - lastSave < 1000 * 3) {
                            player.sendMessage(Colors.RED + "Please wait before saving again.");
                            return true;
                        }
                    }
                    
                    lastCopySave.put(player.getName(), now);
                    
                    // Copy
                    CuboidCopy copy = new CuboidCopy(min, size);
                    copy.copy(player.getWorld());
                    
                    server.getLogger().info(player.getName() + " saving toggle area with folder '"
                            + namespace + "' and ID '" + id + "'.");
                    
                    // Save
                    try {
                        craftBook.getCopyManager().save(player.getWorld(), namespace, id, copy);
                        if (namespaced) {
                            player.sendMessage(Colors.GOLD + "Area saved as '"
                                    + id + "' under the specified namespace.");
                        } else {
                            player.sendMessage(Colors.GOLD + "Area saved as '"
                                    + id + "' under your player.");
                        }
                    } catch (IOException e) {
                        player.sendMessage(Colors.RED + "Could not save area: " + e.getMessage());
                    }
                } catch (NoClassDefFoundError e) {
                    player.sendMessage(Colors.RED + "WorldEdit.jar does not exist in plugins/.");
                }
    
                return true;
            }
    
            return false;
        } catch (LocalWorldEditBridgeException e) {
            if (e.getCause() != null) {
                Throwable cause = e.getCause();
                String causeName = cause.getClass().getCanonicalName();
                
                // If the player has not defined a region
                if (causeName.equals("com.sk89q.worldedit.IncompleteRegionException")) {
                    player.sendMessage(Colors.RED + "Region not fully defined (via WorldEdit).");
                // If WorldEdit is not an installed plugin
                } else if (causeName.equals("com.sk89q.worldedit.WorldEditNotInstalled")) {
                    player.sendMessage(Colors.RED + "The WorldEdit plugin is not loaded.");
                // An unknown error
                } else {
                    player.sendMessage(Colors.RED + "Unknown CraftBook<->WorldEdit error: "
                            + cause.getClass().getCanonicalName());
                }
            } else {
                player.sendMessage(Colors.RED + "Unknown CraftBook<->WorldEdit error: "
                        + e.getMessage());
            }

            return true;
        }
    }

    /**
     * Read a file containing cauldron recipes.
     *
     * @param file
     * @return
     * @throws IOException
     */
    private CauldronCookbook readCauldronRecipes(String path)
            throws IOException {
        
        File file = new File(path);
        FileReader input = null;
        CauldronCookbook cookbook = new CauldronCookbook();

        try {
            input = new FileReader(file);
            BufferedReader buff = new BufferedReader(input);

            String line;
            while ((line = buff.readLine()) != null) {
                line = line.trim();

                // Blank lines
                if (line.length() == 0) {
                    continue;
                }

                // Comment
                if (line.charAt(0) == ';' || line.charAt(0) == '#' || line.equals("")) {
                    continue;
                }

                String[] parts = line.split(":");
                
                if (parts.length < 3) {
                    server.getLogger().log(Level.WARNING, "Invalid cauldron recipe line in "
                            + file.getName() + ": '" + line + "'");
                } else {
                    String name = parts[0];
                    List<Integer> ingredients = parseCauldronItems(parts[1]);
                    List<Integer> results = parseCauldronItems(parts[2]);
                    String[] groups = null;
                    
                    if (parts.length >= 4 && parts[3].trim().length() > 0) {
                        groups = parts[3].split(",");
                    }
                    
                    CauldronCookbook.Recipe recipe =
                            new CauldronCookbook.Recipe(name, ingredients, results, groups);
                    cookbook.add(recipe);
                }
            }

            return cookbook;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * Parse a list of cauldron items.
     * 
     * @param list
     * @return
     */
    private List<Integer> parseCauldronItems(String list) {
        String[] parts = list.split(",");

        List<Integer> out = new ArrayList<Integer>();

        for (String part : parts) {
            int multiplier = 1;

            try {
                // Multiplier
                if (part.matches("^.*\\*([0-9]+)$")) {
                    int at = part.lastIndexOf("*");
                    multiplier = Integer.parseInt(
                            part.substring(at + 1, part.length()));
                    part = part.substring(0, at);
                }

                try {
                    for (int i = 0; i < multiplier; i++) {
                        out.add(Integer.valueOf(part));
                    }
                } catch (NumberFormatException e) {
                    int item = server.getConfiguration().getItemId(part);

                    if (item > 0) {
                        for (int i = 0; i < multiplier; i++) {
                            out.add(item);
                        }
                    } else {
                        server.getLogger().log(Level.WARNING, "Cauldron: Unknown item " + part);
                    }
                }
            } catch (NumberFormatException e) { // Bad multiplier
                server.getLogger().log(Level.WARNING, "Cauldron: Bad multiplier in '" + part + "'");
            }
        }

        return out;
    }
    
    /**
     * Check if a player can use a command. May be overrided if permissions
     * checking is disabled.
     * 
     * @param player
     * @param command
     * @return
     */
    public boolean checkObjectUse(PlayerInterface player, String command) {
        return !checkPermissions || player.canUseObject(command);
    }
    
    /**
     * Check if a player can use a command. May be overrided if permissions
     * checking is disabled.
     * 
     * @param player
     * @param command
     * @return
     */
    public boolean checkObjectCreate(PlayerInterface player, String permission) {
        if (!checkCreatePermissions || player.canCreateObject(permission)) {
            return true;
        } else {
            player.printError("You don't have permission to make that.");
            return false;
        }
    }

    /**
     *
     * @param player
     */
    @Override
    public void onDisconnect(PlayerInterface player) {
        lastCopySave.remove(player.getName());
    }
}
