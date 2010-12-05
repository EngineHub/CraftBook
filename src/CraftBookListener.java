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

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

import lymia.perlstone.Perlstone_1_0;

/**
 * Event listener for Hey0's server mod.
 *
 * @author sk89q
 */
public class CraftBookListener extends PluginListener {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft");
    /**
     * CraftBook.
     */
    private CraftBook craftBook;
    /**
     * Properties file for CraftBook.
     */
    private PropertiesFile properties = new PropertiesFile("craftbook.properties");
    /**
     * Used for toggle-able areas.
     */
    private CopyManager copies = new CopyManager();
    /**
     * Stores who has been shown the CraftBook version.
     */
    private Set<String> beenToldVersion =
            new HashSet<String>();
    /**
     * Last minecart message sent from a sign to a player.
     */
    private Map<String,String> lastMinecartMsg =
            new HashMap<String,String>();
    /**
     * Last minecart message send time from a sign to a player.
     */
    private Map<String,Long> lastMinecartMsgTime =
            new HashMap<String,Long>();
    /**
     * SISO ICs.
     */
    private Map<String,SISOFamilyIC> sisoICs =
            new HashMap<String,SISOFamilyIC>();
    /**
     * SI3O ICs.
     */
    private Map<String,SI3OFamilyIC> si3oICs =
            new HashMap<String,SI3OFamilyIC>();
    /**
     * 3ISO ICs.
     */
    private Map<String,_3ISOFamilyIC> _3isoICs =
            new HashMap<String,_3ISOFamilyIC>();
    /**
     * VIVO ICs.
     */
    private Map<String,VIVOFamilyIC> vivoICs =
            new HashMap<String,VIVOFamilyIC>();
    /**
     * The block that was changed.
     */
    private BlockVector changedRedstoneInput;
    
    /**
     * Redstone recursion limit.
     */
    private int recursionCount;

    /**
     * Indicates whether each function should check permissions when using.
     */
    private boolean checkPermissions;
    /**
     * Indicates whether each function should check permissions when creating.
     */
    private boolean checkCreatePermissions;
    /**
     * Maximum toggle area size.
     */
    private int maxToggleAreaSize;

    /**
     * Fast block bag access.
     */
    private static BlockBag dummyBlockBag = new UnlimitedBlackHoleBlockBag();

    private boolean useChestsBlockBag = false;
    private BookReader readingModule;
    private String bookReadLine;
    private Cauldron cauldronModule;
    private Elevator elevatorModule;
    private GateSwitch gateSwitchModule;
    private boolean redstoneGates = true;
    private LightSwitch lightSwitchModule;
    private Bridge bridgeModule;
    private boolean redstoneBridges = true;
    private boolean useToggleAreas;
    private boolean dropBookshelves = true;
    private boolean redstonePumpkins = true;
    private double dropAppleChance = 0;
    private boolean redstoneICs = true;
    private boolean redstonePLCs = true;
    private boolean enableAmmeter = true;
    private boolean minecartControlBlocks = true;
    private boolean hinderPressurePlateMinecartSlow = false;
    private boolean hinderUnoccupiedSlowdown = true;
    private boolean inCartControl = true;
    private boolean minecartDispensers = true;
    private boolean minecartTrackMessages = true;
    private int minecart25xBoostBlock = BlockType.GOLD_ORE;
    private int minecart100xBoostBlock = BlockType.GOLD_BLOCK;
    private int minecart50xSlowBlock = BlockType.SLOW_SAND;
    private int minecart20xSlowBlock = BlockType.GRAVEL;
    private int minecartStationBlock = BlockType.OBSIDIAN;
    private int minecartReverseBlock = BlockType.CLOTH;
    private int minecartTriggerBlock = BlockType.IRON_ORE;
    private int minecartEjectBlock = BlockType.IRON_BLOCK;

    /**
     * Construct CraftBook.
     * 
     * @param craftBook
     */
    public CraftBookListener(CraftBook craftBook) {
        this.craftBook = craftBook;
        sisoICs.put("MC1000", new MC1000());
        sisoICs.put("MC1001", new MC1001());
        sisoICs.put("MC1017", new MC1017());
        sisoICs.put("MC1018", new MC1018());
        sisoICs.put("MC1020", new MC1020());
        sisoICs.put("MC1025", new MC1025());
        sisoICs.put("MC1110", new MC1110());
        sisoICs.put("MC1111", new MC1111());
        sisoICs.put("MC1200", new MC1200());
        sisoICs.put("MC1201", new MC1201());
        sisoICs.put("MC1205", new MC1205());
        sisoICs.put("MC1206", new MC1206());
        sisoICs.put("MC1230", new MC1230());
        sisoICs.put("MC1231", new MC1231());
        si3oICs.put("MC2020", new MC2020());
        _3isoICs.put("MC3020", new MC3020());
        _3isoICs.put("MC3002", new MC3002());
        _3isoICs.put("MC3003", new MC3003());
        _3isoICs.put("MC3021", new MC3021());
        _3isoICs.put("MC3030", new MC3030());
        _3isoICs.put("MC3031", new MC3031());
        _3isoICs.put("MC3034", new MC3034());
        _3isoICs.put("MC3036", new MC3036());
        _3isoICs.put("MC3231", new MC3231());
        vivoICs.put("MC5000", new PlcBase(new Perlstone_1_0(), true));
        //Uncomment this when switch-based memory is implemented.
        //vivoICs.put("MC5100", new PlcBase(new Perlstone_1_0(), false));
    }

    /**
     * Checks to make sure that there are enough but not too many arguments.
     *
     * @param args
     * @param min
     * @param max -1 for no maximum
     * @param cmd command name
     * @throws InsufficientArgumentsException
     */
    private void checkArgs(String[] args, int min, int max, String cmd)
            throws InsufficientArgumentsException {
        if (args.length <= min) {
            throw new InsufficientArgumentsException("Minimum " + min + " arguments");
        } else if (max != -1 && args.length - 1 > max) {
            throw new InsufficientArgumentsException("Maximum " + max + " arguments");
        }
    }

    /**
     * Convert a comma-delimited list to a set of integers.
     *
     * @param str
     * @return
     */
    private static Set<Integer> toBlockIDSet(String str) {
        if (str.trim().length() == 0) {
            return null;
        }

        String[] items = str.split(",");
        Set<Integer> result = new HashSet<Integer>();

        for (String item : items) {
            try {
                result.add(Integer.parseInt(item.trim()));
            } catch (NumberFormatException e) {
                int id = etc.getDataSource().getItem(item.trim());
                if (id != 0) {
                    result.add(id);
                } else {
                    logger.log(Level.WARNING, "CraftBook: Unknown block name: "
                            + item);
                }
            }
        }

        return result;
    }

    /**
    /**
     * Loads CraftBooks's configuration from file.
     */
    public void loadConfiguration() {
        try {
            properties.load();
        } catch (IOException e) {
            logger.warning("Failed to load craftbook.properties: " + e.getMessage());
        }

        maxToggleAreaSize = Math.max(0, properties.getInt("toggle-area-max-size", 5000));

        readingModule = properties.getBoolean("bookshelf-enable", true) ? new BookReader() : null;
        bookReadLine = properties.getString("bookshelf-read-text", "You pick out a book...");
        lightSwitchModule = properties.getBoolean("light-switch-enable", true) ? new LightSwitch() : null;
        gateSwitchModule = properties.getBoolean("gate-enable", true) ? new GateSwitch() : null;
        redstoneGates = properties.getBoolean("gate-redstone", true);
        elevatorModule = properties.getBoolean("elevators-enable", true) ? new Elevator() : null;
        bridgeModule = properties.getBoolean("bridge-enable", true) ? new Bridge() : null;
        redstoneBridges = properties.getBoolean("bridge-redstone", true);
        Bridge.allowableBridgeBlocks = toBlockIDSet(properties.getString("bridge-blocks", "4,5,20,43"));
        Bridge.maxBridgeLength = properties.getInt("bridge-max-length", 30);
        dropBookshelves = properties.getBoolean("drop-bookshelves", true);
        try {
            dropAppleChance = Double.parseDouble(properties.getString("apple-drop-chance", "0.5")) / 100.0;
        } catch (NumberFormatException e) {
            dropAppleChance = -1;
            logger.log(Level.WARNING, "Invalid apple drop chance setting in craftbook.properties");
        }
        useToggleAreas = properties.getBoolean("toggle-areas-enable", true);
        redstonePumpkins = properties.getBoolean("redstone-pumpkins", true);
        checkPermissions = properties.getBoolean("check-permissions", false);
        checkCreatePermissions = properties.getBoolean("check-create-permissions", false);
        cauldronModule = null;
        redstoneICs = properties.getBoolean("redstone-ics", true);
        redstonePLCs = properties.getBoolean("redstone-plcs", true);
        enableAmmeter = properties.getBoolean("ammeter", true);
        minecartControlBlocks = properties.getBoolean("minecart-control-blocks", true);
        hinderPressurePlateMinecartSlow = properties.getBoolean("hinder-minecart-pressure-plate-slow", true);
        hinderUnoccupiedSlowdown = properties.getBoolean("minecart-hinder-unoccupied-slowdown", true);
        inCartControl = properties.getBoolean("minecart-in-cart-control", true);
        minecartDispensers = properties.getBoolean("minecart-dispensers", true);
        minecartTrackMessages = properties.getBoolean("minecart-track-messages", true);
        minecart25xBoostBlock = properties.getInt("minecart-25x-boost-block", BlockType.GOLD_ORE);
        minecart100xBoostBlock = properties.getInt("minecart-100x-boost-block", BlockType.GOLD_BLOCK);
        minecart50xSlowBlock = properties.getInt("minecart-50x-slow-block", BlockType.SLOW_SAND);
        minecart20xSlowBlock = properties.getInt("minecart-20x-slow-block", BlockType.GRAVEL);
        minecartStationBlock = properties.getInt("minecart-station-block", BlockType.OBSIDIAN);
        minecartReverseBlock = properties.getInt("minecart-reverse-block", BlockType.CLOTH);
        minecartTriggerBlock = properties.getInt("minecart-trigger-block", BlockType.IRON_ORE);
        minecartEjectBlock = properties.getInt("minecart-eject-block", BlockType.IRON_BLOCK);

        String blockBag = properties.getString("block-bag", "unlimited-black-hole");
        if (blockBag.equalsIgnoreCase("nearby-chests")) {
            useChestsBlockBag = true;
        } else if (blockBag.equalsIgnoreCase("unlimited-black-hole")) {
            useChestsBlockBag = false;
        } else {
            logger.log(Level.WARNING, "Unknown CraftBook block bag: " + blockBag);
            useChestsBlockBag = false;
        }

        if (properties.getBoolean("cauldron-enable", true)) {
            try {
                CauldronCookbook recipes =
                        readCauldronRecipes("cauldron-recipes.txt");

                if (recipes.size() != 0) {
                    cauldronModule = new Cauldron(recipes);
                    logger.log(Level.INFO, recipes.size() + " cauldron recipe(s) loaded");
                } else {
                    logger.log(Level.WARNING, "cauldron-recipes.txt had no recipes");
                }
            } catch (IOException e) {
                logger.log(Level.INFO, "cauldron-recipes.txt not loaded: "
                        + e.getMessage());
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
    public boolean onBlockDestroy(Player player, Block block) {
        // Random apple drops
        if (dropAppleChance > 0 && block.getType() == BlockType.LEAVES
                && checkPermission(player, "/appledrops")) {
            if (block.getStatus() == 3) {
                if (Math.random() <= dropAppleChance) {
                    etc.getServer().dropItem(
                            block.getX(), block.getY(), block.getZ(),
                            ItemType.APPLE);
                }
            }

        // Bookshelf drops
        } else if (dropBookshelves && block.getType() == BlockType.BOOKCASE
                && checkPermission(player, "/bookshelfdrops")) {
            if (block.getStatus() == 3) {
                    etc.getServer().dropItem(
                            block.getX(), block.getY(), block.getZ(),
                            BlockType.BOOKCASE);
            }
        }

        return false;
    }

    /**
     * Called when a block is being attempted to be placed.
     *
     * @param player
     * @param blockPlaced
     * @param blockClicked
     * @param itemInHand
     * @return
     */
    @Override
    public boolean onBlockCreate(Player player, Block blockPlaced,
            Block blockClicked, int itemInHand) {
        try {
            return doBlockCreate(player, blockPlaced, blockClicked, itemInHand);
        } catch (OutOfBlocksException e) {
            player.sendMessage(Colors.Rose + "Uh oh! Ran out of: " + toBlockName(e.getID()));
            player.sendMessage(Colors.Rose + "Make sure nearby chests have the necessary materials.");
        } catch (OutOfSpaceException e) {
            player.sendMessage(Colors.Rose + "No room left to put: " + toBlockName(e.getID()));
            player.sendMessage(Colors.Rose + "Make sure nearby partially occupied chests have free slots.");
        } catch (BlockBagException e) {
            player.sendMessage(Colors.Rose + "Unknown error: " + e.getMessage());
        }

        return true; // On error
    }

    /**
     * Called when a block is being attempted to be placed.
     * 
     * @param player
     * @param blockPlaced
     * @param blockClicked
     * @param itemInHand
     * @return
     */
    private boolean doBlockCreate(Player player, Block blockPlaced,
            Block blockClicked, int itemInHand) throws BlockBagException {

        int current = -1;

        if (enableAmmeter && itemInHand == 263) { // Coal
            int type = blockClicked.getType();
            int data = CraftBook.getBlockData(blockClicked.getX(),
                    blockClicked.getY(), blockClicked.getZ());
            
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
                player.sendMessage(Colors.Yellow + "Ammeter: "
                        + Colors.Yellow + "["
                        + Colors.Yellow + repeatString("|", current)
                        + Colors.Black + repeatString("|", 15 - current)
                        + Colors.Yellow + "] "
                        + Colors.White
                        + current + " A");
            } else {
                player.sendMessage(Colors.Yellow + "Ammeter: " + Colors.Red + "Not supported.");
            }

            return false;
        }

        // Discriminate against attempts that would actually place blocks
        boolean isPlacingBlock = blockPlaced.getType() != -1
                && blockPlaced.getType() <= 256;

        int plyX = (int)Math.floor(player.getLocation().x);
        int plyY = (int)Math.floor(player.getLocation().y);
        int plyZ = (int)Math.floor(player.getLocation().z);

        // Book reading
        if (readingModule != null
                && blockClicked.getType() == BlockType.BOOKCASE
                && !isPlacingBlock
                && checkPermission(player, "/readbooks")) {
            readingModule.readBook(player, bookReadLine);
            return true;

        // Sign buttons
        } else if (blockClicked.getType() == BlockType.WALL_SIGN ||
                blockClicked.getType() == BlockType.SIGN_POST ||
                CraftBook.getBlockID(plyX, plyY + 1, plyZ) == BlockType.WALL_SIGN ||
                CraftBook.getBlockID(plyX, plyY, plyZ) == BlockType.WALL_SIGN) {
            int x = blockClicked.getX();
            int y = blockClicked.getY();
            int z = blockClicked.getZ();

            Vector pt;
            if (blockClicked.getType() == BlockType.WALL_SIGN
                    || blockClicked.getType() == BlockType.SIGN_POST) {
                pt = new Vector(x, y, z);
            } else if (CraftBook.getBlockID(plyX, plyY + 1, plyZ) == BlockType.WALL_SIGN) {
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

            ComplexBlock cBlock = etc.getServer().getComplexBlock(x, y, z);

            if (cBlock instanceof Sign) {
                Sign sign = (Sign)cBlock;
                String line2 = sign.getText(1);

                // Gate
                if (gateSwitchModule != null && line2.equalsIgnoreCase("[Gate]")
                        && checkPermission(player, "/gate")) {
                    BlockBag bag = getBlockBag(pt);
                    bag.addSourcePosition(pt);

                    informUser(player);

                    // A gate may toggle or not
                    if (gateSwitchModule.toggleGates(pt, bag)) {
                        player.sendMessage(Colors.Gold + "*screeetch* Gate moved!");
                    } else {
                        player.sendMessage(Colors.Rose + "No nearby gate to toggle.");
                    }
                
                // Light switch
                } else if (lightSwitchModule != null &&
                        (line2.equalsIgnoreCase("[|]") || line2.equalsIgnoreCase("[I]"))
                        && checkPermission(player, "/lightswitch")) {
                    BlockBag bag = getBlockBag(pt);
                    bag.addSourcePosition(pt);

                    informUser(player);
                    
                    return lightSwitchModule.toggleLights(pt, bag);

                // Elevator
                } else if (elevatorModule != null
                        && (line2.equalsIgnoreCase("[Lift Up]")
                        || line2.equalsIgnoreCase("[Lift Down]"))
                        && checkPermission(player, "/elevator")) {

                    informUser(player);

                    // Go up or down?
                    boolean up = line2.equalsIgnoreCase("[Lift Up]");
                    elevatorModule.performLift(player, pt, up);
                    return true;

                // Toggle areas
                } else if (useToggleAreas != false
                        && line2.equalsIgnoreCase("[Toggle]")
                        && checkPermission(player, "/togglearea")) {
                    String name = sign.getText(0);

                    if (name.trim().length() == 0) {
                        player.sendMessage(Colors.Rose + "Area name must be the first line.");
                        return true;
                    } else if (!CopyManager.isValidName(name)) {
                        player.sendMessage(Colors.Rose + "Not a valid area name (1st sign line)!");
                        return true;
                    }

                    informUser(player);

                    try {
                        BlockBag bag = getBlockBag(pt);
                        bag.addSourcePosition(pt);
                        CuboidCopy copy = copies.load(name);
                        if (copy.distance(pt) <= 4) {
                            copy.toggle(bag);
                            
                            // Get missing
                            Map<Integer,Integer> missing = bag.getMissing();
                            if (missing.size() > 0) {
                                for (Map.Entry<Integer,Integer> entry : missing.entrySet()) {
                                    player.sendMessage(Colors.Rose + "Missing "
                                            + entry.getValue() + "x "
                                            + toBlockName(entry.getKey()));
                                }
                            } else {
                                player.sendMessage(Colors.Gold + "Toggled!");
                            }
                        } else {
                            player.sendMessage(Colors.Rose + "This sign is too far away!");
                        }
                    } catch (CuboidCopyException e) {
                        player.sendMessage(Colors.Rose + "Could not load area: " + e.getMessage());
                    } catch (IOException e2) {
                        player.sendMessage(Colors.Rose + "Could not load area: " + e2.getMessage());
                    }

                // Bridges
                } else if (bridgeModule != null
                        && blockClicked.getType() == BlockType.SIGN_POST
                        && line2.equalsIgnoreCase("[Bridge]")
                        && checkPermission(player, "/bridge")) {
                    int data = CraftBook.getBlockData(x, y, z);

                    informUser(player);

                    try {
                        BlockBag bag = getBlockBag(pt);
                        bag.addSourcePosition(pt);
                        
                        if (data == 0x0) {
                            bridgeModule.toggleBridge(new Vector(x, y, z), Bridge.Direction.EAST, bag);
                            player.sendMessage(Colors.Gold + "Bridge toggled.");
                        } else if (data == 0x4) {
                            bridgeModule.toggleBridge(new Vector(x, y, z), Bridge.Direction.SOUTH, bag);
                            player.sendMessage(Colors.Gold + "Bridge toggled.");
                        } else if (data == 0x8) {
                            bridgeModule.toggleBridge(new Vector(x, y, z), Bridge.Direction.WEST, bag);
                            player.sendMessage(Colors.Gold + "Bridge toggled.");
                        } else if (data == 0xC) {
                            bridgeModule.toggleBridge(new Vector(x, y, z), Bridge.Direction.NORTH, bag);
                            player.sendMessage(Colors.Gold + "Bridge toggled.");
                        } else {
                            player.sendMessage(Colors.Rose + "That sign is not in a right direction.");
                        }
                    } catch (OperationException e) {
                        player.sendMessage(Colors.Rose + e.getMessage());
                    }
                }
            }

        // Cauldron
        } else if (cauldronModule != null
                && checkPermission(player, "/cauldron")
                && (blockPlaced.getType() == -1 || blockPlaced.getType() >= 256)
                && blockPlaced.getType() != BlockType.STONE) {
            
            int x = blockClicked.getX();
            int y = blockClicked.getY();
            int z = blockClicked.getZ();

            cauldronModule.preCauldron(new Vector(x, y, z), player);
        }

        return false;
    }

    /*
    * Called whenever a redstone source (wire, switch, torch) changes its
    * current.
    *
    * Standard values for wires are 0 for no current, and 14 for a strong current.
    * Default behaviour for redstone wire is to lower the current by one every
    * block.
    *
    * For other blocks which provide a source of redstone current, the current
    * value will be 1 or 0 for on and off respectively.
    *
    * @param redstone Block of redstone which has just changed in current
    * @param oldLevel the old current
    * @param newLevel the new current
    */
    public int onRedstoneChange(Block block, int oldLevel, int newLevel) {
        recursionCount++;

        if (recursionCount < 0) {
            recursionCount = 0;
        }

        try {
            // Pre-check
            if (!redstoneGates && !redstoneBridges && !redstonePumpkins && !redstoneICs) {
                return newLevel;
            }

            if (recursionCount > 4) {
                recursionCount = 0;
                block.setType(0);
                block.update();
                return 0;
            }

            boolean wasOn = oldLevel >= 1;
            boolean isOn = newLevel >= 1;
            boolean wasChange = wasOn != isOn;

            // For efficiency reasons, we're only going to consider changes between
            // off and on state, and ignore simple current changes (i.e. 15->13)
            if (!wasChange) {
                return newLevel;
            }

            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();

            int type = CraftBook.getBlockID(x, y, z);
            int above = CraftBook.getBlockID(x, y + 1, z);

            changedRedstoneInput = new BlockVector(x, y, z);

            // When this hook has been called, the level in the world has not
            // yet been updated, so we're going to do this very ugly thing of
            // faking the value with the new one whenever the data value of this
            // block is requested -- it is quite ugly
            try {
                if (type == BlockType.LEVER) {
                    // Fake data
                    CraftBook.fakeBlockData(x, y, z,
                            newLevel > 0
                                ? CraftBook.getBlockData(x, y, z) | 0x8
                                : CraftBook.getBlockData(x, y, z) & 0x7);
                } else if (type == BlockType.STONE_PRESSURE_PLATE) {
                    // Fake data
                    CraftBook.fakeBlockData(x, y, z,
                            newLevel > 0
                                ? CraftBook.getBlockData(x, y, z) | 0x1
                                : CraftBook.getBlockData(x, y, z) & 0x14);
                } else if (type == BlockType.WOODEN_PRESSURE_PLATE) {
                    // Fake data
                    CraftBook.fakeBlockData(x, y, z,
                            newLevel > 0
                                ? CraftBook.getBlockData(x, y, z) | 0x1
                                : CraftBook.getBlockData(x, y, z) & 0x14);
                } else if (type == BlockType.STONE_BUTTON) {
                    // Fake data
                    CraftBook.fakeBlockData(x, y, z,
                            newLevel > 0
                                ? CraftBook.getBlockData(x, y, z) | 0x8
                                : CraftBook.getBlockData(x, y, z) & 0x7);
                } else if (type == BlockType.REDSTONE_WIRE) {
                    // Fake data
                    CraftBook.fakeBlockData(x, y, z, newLevel);

                    int westSide = CraftBook.getBlockID(x, y, z + 1);
                    int westSideAbove = CraftBook.getBlockID(x, y + 1, z + 1);
                    int westSideBelow = CraftBook.getBlockID(x, y - 1, z + 1);
                    int eastSide = CraftBook.getBlockID(x, y, z - 1);
                    int eastSideAbove = CraftBook.getBlockID(x, y + 1, z - 1);
                    int eastSideBelow = CraftBook.getBlockID(x, y - 1, z - 1);

                    int northSide = CraftBook.getBlockID(x - 1, y, z);
                    int northSideAbove = CraftBook.getBlockID(x - 1, y + 1, z);
                    int northSideBelow = CraftBook.getBlockID(x - 1, y - 1, z);
                    int southSide = CraftBook.getBlockID(x + 1, y, z);
                    int southSideAbove = CraftBook.getBlockID(x + 1, y + 1, z);
                    int southSideBelow = CraftBook.getBlockID(x + 1, y - 1, z);

                    // Make sure that the wire points to only this block
                    if (!BlockType.isRedstoneBlock(westSide)
                            && !BlockType.isRedstoneBlock(eastSide)
                            && (!BlockType.isRedstoneBlock(westSideAbove) || westSide == 0)
                            && (!BlockType.isRedstoneBlock(eastSideAbove) || eastSide == 0)
                            && (!BlockType.isRedstoneBlock(westSideBelow) || westSide != 0)
                            && (!BlockType.isRedstoneBlock(eastSideBelow) || eastSide != 0)) {
                        // Possible blocks north / south
                        handleDirectWireInput(new Vector(x - 1, y, z), isOn);
                        handleDirectWireInput(new Vector(x + 1, y, z), isOn);
                    }

                    if (!BlockType.isRedstoneBlock(northSide)
                            && !BlockType.isRedstoneBlock(southSide)
                            && (!BlockType.isRedstoneBlock(northSideAbove) || northSide == 0)
                            && (!BlockType.isRedstoneBlock(southSideAbove) || southSide == 0)
                            && (!BlockType.isRedstoneBlock(northSideBelow) || northSide != 0)
                            && (!BlockType.isRedstoneBlock(southSideBelow) || southSide != 0)) {
                        // Possible blocks west / east
                        handleDirectWireInput(new Vector(x, y, z - 1), isOn);
                        handleDirectWireInput(new Vector(x, y, z + 1), isOn);
                    }

                    // Can be triggered from below
                    handleDirectWireInput(new Vector(x, y + 1, z), isOn);

                    return newLevel;
                }

                // For redstone wires, the code already exited this method
                // Non-wire blocks proceed

                handleDirectWireInput(new Vector(x - 1, y, z), isOn);
                handleDirectWireInput(new Vector(x + 1, y, z), isOn);
                handleDirectWireInput(new Vector(x, y, z - 1), isOn);
                handleDirectWireInput(new Vector(x, y, z + 1), isOn);

                // Can be triggered from below
                handleDirectWireInput(new Vector(x, y + 1, z), isOn);

                return newLevel;
            } finally {
                CraftBook.clearFakeBlockData();
            }
        } finally {
            recursionCount--;
        }
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
    public void handleDirectWireInput(Vector pt, boolean isOn) {
        int type = CraftBook.getBlockID(pt);
        
        // Redstone pumpkins
        if (redstonePumpkins
                && (type == BlockType.PUMPKIN || type == BlockType.JACKOLANTERN)) {
            Boolean useOn = testAnyRedstoneInput(pt);

            if (useOn != null && useOn) {
                CraftBook.setBlockID(pt, BlockType.JACKOLANTERN);
            } else if (useOn != null) {
                CraftBook.setBlockID(pt, BlockType.PUMPKIN);
            }
        // Minecart dispenser
        } else if (minecartDispensers && type == BlockType.CHEST
                && (CraftBook.getBlockID(pt.add(0, -2, 0)) == BlockType.SIGN_POST
                    || CraftBook.getBlockID(pt.add(0, -1, 0)) == BlockType.SIGN_POST)) {
            if (!isOn) {
                return;
            }
            
            Vector signPos = pt.add(0, -2, 0);

            if (!doesSignSay(signPos, 1, "[Dispenser]")) {
                signPos = pt.add(0, -1, 0);
            }

            if (!doesSignSay(signPos, 1, "[Dispenser]")) {
                return;
            }

            Vector dir = getSignPostOrthogonalBack(signPos, 1)
                    .subtract(signPos);
            Vector depositPt = pt.add(dir.multiply(2.5));

            if (CraftBook.getBlockID(depositPt) != BlockType.MINECART_TRACKS) {
                return;
            }

            NearbyChestBlockBag blockBag = new NearbyChestBlockBag(pt);
            blockBag.addSingleSourcePosition(pt);
            blockBag.addSingleSourcePosition(pt.add(1, 0, 0));
            blockBag.addSingleSourcePosition(pt.add(-1, 0, 0));
            blockBag.addSingleSourcePosition(pt.add(0, 0, 1));
            blockBag.addSingleSourcePosition(pt.add(0, 0, -1));

            try {
                blockBag.fetchBlock(ItemType.MINECART);
                blockBag.flushChanges();
                jo minecart = new jo(etc.getMCServer().e,
                        depositPt.getX(), depositPt.getY(),
                        depositPt.getZ(), 0);
                etc.getMCServer().e.a(minecart);
            } catch (BlockBagException e) {
                // No minecarts
            }
        // Minecart station
        } else if (minecartControlBlocks && type == minecartStationBlock
                && CraftBook.getBlockID(pt.add(0, 1, 0)) == BlockType.MINECART_TRACKS
                && (CraftBook.getBlockID(pt.add(0, -2, 0)) == BlockType.SIGN_POST
                    || CraftBook.getBlockID(pt.add(0, -1, 0)) == BlockType.SIGN_POST)) {
            ComplexBlock cblock = etc.getServer().getComplexBlock(
                    pt.getBlockX(), pt.getBlockY() - 2, pt.getBlockZ());

            // Maybe it's the sign directly below
            if (cblock == null || !(cblock instanceof Sign)) {
                cblock = etc.getServer().getComplexBlock(
                        pt.getBlockX(), pt.getBlockY() - 1, pt.getBlockZ());
            }

            if (cblock == null || !(cblock instanceof Sign)) {
                return;
            }

            Sign sign = (Sign)cblock;
            String line2 = sign.getText(1);

            if (!line2.equalsIgnoreCase("[Station]")) {
                return;
            }

            Vector motion;
            int data = CraftBook.getBlockData(
                    pt.getBlockX(), pt.getBlockY() - 2, pt.getBlockZ());
            
            if (data == 0x0) {
                motion = new Vector(0, 0, -0.3);
            } else if (data == 0x4) {
                motion = new Vector(0.3, 0, 0);
            } else if (data == 0x8) {
                motion = new Vector(0, 0, 0.3);
            } else if (data == 0xC) {
                motion = new Vector(-0.3, 0, 0);
            } else {
                return;
            }

            for (BaseEntity ent : etc.getServer().getEntityList()) {
                if (ent instanceof Minecart) {
                    Minecart minecart = (Minecart)ent;
                    int cartX = (int)Math.floor(minecart.getX());
                    int cartY = (int)Math.floor(minecart.getY());
                    int cartZ = (int)Math.floor(minecart.getZ());

                    if (cartX == pt.getBlockX()
                            && cartY == pt.getBlockY() + 1
                            && cartZ == pt.getBlockZ()) {
                        minecart.setMotion(motion.getX(), motion.getY(), motion.getZ());
                    }
                }
            }
        // Sign gates
        } else if (type == BlockType.WALL_SIGN
                || type == BlockType.SIGN_POST) {
            ComplexBlock cblock = etc.getServer().getComplexBlock(
                    pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

            if (!(cblock instanceof Sign)) {
                return;
            }

            Sign sign = (Sign)cblock;
            String line2 = sign.getText(1);
            int len = line2.length();

            // Gate
            if (gateSwitchModule != null && redstoneGates
                    && line2.equalsIgnoreCase("[Gate]")) {
                BlockBag bag = getBlockBag(pt);
                bag.addSourcePosition(pt);

                // A gate may toggle or not
                try {
                    gateSwitchModule.setGateState(pt, bag, isOn);
                } catch (BlockBagException e) {
                }

            // Bridges
            } else if (bridgeModule != null
                    && redstoneBridges
                    && type == BlockType.SIGN_POST
                    && line2.equalsIgnoreCase("[Bridge]")) {
                int data = CraftBook.getBlockData(pt);

                try {
                    BlockBag bag = getBlockBag(pt);
                    bag.addSourcePosition(pt);

                    if (data == 0x0) {
                        bridgeModule.setBridgeState(pt, Bridge.Direction.EAST, bag, !isOn);
                    } else if (data == 0x4) {
                        bridgeModule.setBridgeState(pt, Bridge.Direction.SOUTH, bag, !isOn);
                    } else if (data == 0x8) {
                        bridgeModule.setBridgeState(pt, Bridge.Direction.WEST, bag, !isOn);
                    } else if (data == 0xC) {
                        bridgeModule.setBridgeState(pt, Bridge.Direction.NORTH, bag, !isOn);
                    }
                } catch (OperationException e) {
                } catch (BlockBagException e) {
                }
            // ICs
            } else if (redstoneICs
                    && type == BlockType.WALL_SIGN
                    && line2.length() > 4
                    && line2.substring(0, 3).equalsIgnoreCase("[MC") &&
                    line2.charAt(len - 1) == ']') {
                String id = line2.substring(1, len - 1).toUpperCase();

                SignText signText = new SignText(
                        sign.getText(0), sign.getText(1), sign.getText(2),
                        sign.getText(3));

                // SISO family
                SISOFamilyIC sisoIC = sisoICs.get(id);

                if (sisoIC != null) {
                    Vector outputVec = getWallSignBack(pt, 2);
                    Vector backVec = getWallSignBack(pt, 1);

                    Signal[] in = new Signal[1];
                    in[0] = new Signal(isOn, true);
                    
                    Signal[] out = new Signal[1];
                    out[0] = new Signal(getRedstoneOutput(outputVec));
                    
                    ChipState chip = new ChipState(pt, backVec, in, out, signText);
                    
                    sisoIC.think(chip);
                    
                    if (chip.isModified()) {
                        setRedstoneOutput(outputVec, chip.getOut(1).is());
                    }

                    if (signText.isChanged()) {
                        sign.setText(0, signText.getLine1());
                        sign.setText(1, signText.getLine2());
                        sign.setText(2, signText.getLine3());
                        sign.setText(3, signText.getLine4());
                        sign.update();
                    }

                    return;
                }

                // SI3O family
                SI3OFamilyIC si30IC = si3oICs.get(id);

                if (si30IC != null) {
                    Vector backVec = getWallSignBack(pt, 1);
                    Vector backShift = backVec.subtract(pt);
                    Vector output1Vec = getWallSignBack(pt, 2);
                    Vector output2Vec = getWallSignSide(pt, 1).add(backShift);
                    Vector output3Vec = getWallSignSide(pt, -1).add(backShift);

                    Signal[] in = new Signal[1];
                    in[0] = new Signal(isOn, true);

                    Signal[] out = new Signal[3];
                    out[0] = new Signal(getRedstoneOutput(output1Vec));
                    out[1] = new Signal(getRedstoneOutput(output2Vec));
                    out[2] = new Signal(getRedstoneOutput(output3Vec));

                    ChipState chip = new ChipState(pt, backVec, in, out, signText);

                    // The most important part...
                    si30IC.think(chip);

                    if (chip.isModified()) {
                        setRedstoneOutput(output1Vec, chip.getOut(1).is());
                        setRedstoneOutput(output2Vec, chip.getOut(2).is());
                        setRedstoneOutput(output3Vec, chip.getOut(3).is());
                    }

                    if (signText.isChanged()) {
                        sign.setText(0, signText.getLine1());
                        sign.setText(1, signText.getLine2());
                        sign.setText(2, signText.getLine3());
                        sign.setText(3, signText.getLine4());
                        sign.update();
                    }

                    return;
                }

                // 3ISO family
                _3ISOFamilyIC _3isoIC = _3isoICs.get(id);

                if (_3isoIC != null) {
                    // Hard-coded
                    if (!redstonePLCs) {
                        return;
                    }
                    
                    Vector backVec = getWallSignBack(pt, 1);
                    Vector outputVec = getWallSignBack(pt, 2);
                    Vector input1Vec = getWallSignBack(pt, -1);
                    Vector input2Vec = getWallSignSide(pt, 1);
                    Vector input3Vec = getWallSignSide(pt, -1);

                    Signal[] in = new Signal[3];
                    in[0] = new Signal(isRedstoneHighBinary(input1Vec, true),
                            changedRedstoneInput.equals(input1Vec));
                    in[1] = new Signal(isRedstoneHighBinary(input2Vec, true),
                            changedRedstoneInput.equals(input2Vec));
                    in[2] = new Signal(isRedstoneHighBinary(input3Vec, true),
                            changedRedstoneInput.equals(input3Vec));

                    Signal[] out = new Signal[1];
                    out[0] = new Signal(getRedstoneOutput(outputVec));

                    ChipState chip = new ChipState(pt, backVec, in, out, signText);

                    // The most important part...
                    _3isoIC.think(chip);

                    if (chip.isModified()) {
                        setRedstoneOutput(outputVec, chip.getOut(1).is());
                    }

                    if (signText.isChanged()) {
                        sign.setText(0, signText.getLine1());
                        sign.setText(1, signText.getLine2());
                        sign.setText(2, signText.getLine3());
                        sign.setText(3, signText.getLine4());
                        sign.update();
                    }

                    return;
                }
                
                // VIVO family
                VIVOFamilyIC vivoIC = vivoICs.get(id);

                if (vivoIC != null) {
                    Vector backVec = getWallSignBack(pt, 1);
                    Vector backShift = backVec.subtract(pt);
                    
                    Vector out0 = getWallSignBack(pt, 2);
                    Vector out1 = getWallSignSide(pt, 1).add(backShift);
                    Vector out2 = getWallSignSide(pt, -1).add(backShift);
                    
                    Vector in0 = getWallSignBack(pt, -1);
                    Vector in1 = getWallSignSide(pt, 1);
                    Vector in2 = getWallSignSide(pt, -1);

                    boolean hasOut1 = CraftBook.getBlockID(out1) == BlockType.LEVER;
                    boolean hasOut2 = CraftBook.getBlockID(out2) == BlockType.LEVER;
                    
                    Signal[] in = new Signal[3];
                    Signal[] out = new Signal[3];
                    
                    out[0] = new Signal(getRedstoneOutput(out0));
                    in[0] = new Signal(isRedstoneHighBinary(in0, true),
                                       changedRedstoneInput.equals(in0));
                    
                    if(hasOut1) {
                        out[1] = new Signal(getRedstoneOutput(out1));
                        in[1] = new Signal(false);
                    }
                    else {
                        out[1] = new Signal(false);
                        in[1] = new Signal(isRedstoneHighBinary(in1, true),
                                           changedRedstoneInput.equals(in1));
                    }
                    
                    if(hasOut2) {
                        out[2] = new Signal(getRedstoneOutput(out2));
                        in[2] = new Signal(false);
                    }
                    else {
                        out[2] = new Signal(false);
                        in[2] = new Signal(isRedstoneHighBinary(in2, true),
                                           changedRedstoneInput.equals(in2));
                    }
                    
                    ChipState chip = new ChipState(pt, backVec, in, out, signText);
                    
                    // The most important part...
                    vivoIC.think(chip);
                    
                    if (chip.isModified()) {
                        setRedstoneOutput(out0, chip.getOut(1).is());
                        if(hasOut1) setRedstoneOutput(out1,chip.getOut(2).is());
                        if(hasOut2) setRedstoneOutput(out2,chip.getOut(3).is());
                    }

                    if (signText.isChanged()) {
                        sign.setText(0, signText.getLine1());
                        sign.setText(1, signText.getLine2());
                        sign.setText(2, signText.getLine3());
                        sign.setText(3, signText.getLine4());
                        sign.update();
                    }

                    return;
                }

                sign.setText(1, Colors.Red + line2);
            }
        }
    }

    /**
     * Attempts to detect redstone input. If there are many inputs to one
     * block, only one of the inputs has to be high.
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    private Boolean testAnyRedstoneInput(Vector pt) {
        Boolean result = null;
        Boolean temp = null;

        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        // Check block above
        int above = CraftBook.getBlockID(x, y + 1, z);
        temp = isRedstoneHigh(new Vector(x, y + 1, z), above, true);
        if (temp != null) {
            if (temp == true) {
                return true;
            } else {
                result = false;
            }
        }

        // Check block below
        int below = CraftBook.getBlockID(x, y - 1, z);
        temp = isRedstoneHigh(new Vector(x, y - 1, z), below, true);
        if (temp != null) {
            if (temp == true) {
                return true;
            } else {
                result = false;
            }
        }

        int north = CraftBook.getBlockID(x - 1, y, z);
        int south = CraftBook.getBlockID(x + 1, y, z);
        int west = CraftBook.getBlockID(x, y, z + 1);
        int east = CraftBook.getBlockID(x, y, z - 1);

        // For wires that lead up to only this block
        if (north == BlockType.REDSTONE_WIRE) {
            temp = isRedstoneWireHigh(new Vector(x - 1, y, z),
                                      new Vector(x - 1, y, z - 1),
                                      new Vector(x - 1, y, z + 1));
            if (temp != null) {
                if (temp == true) {
                    return true;
                } else {
                    result = false;
                }
            }
        }

        if (south == BlockType.REDSTONE_WIRE) {
            temp = isRedstoneWireHigh(new Vector(x + 1, y, z),
                                      new Vector(x + 1, y, z - 1),
                                      new Vector(x + 1, y, z + 1));
            if (temp != null) {
                if (temp == true) {
                    return true;
                } else {
                    result = false;
                }
            }
        }

        if (west == BlockType.REDSTONE_WIRE) {
            temp = isRedstoneWireHigh(new Vector(x, y, z + 1),
                                      new Vector(x + 1, y, z + 1),
                                      new Vector(x - 1, y, z + 1));
            if (temp != null) {
                if (temp == true) {
                    return true;
                } else {
                    result = false;
                }
            }
        }

        if (east == BlockType.REDSTONE_WIRE) {
            temp = isRedstoneWireHigh(new Vector(x, y, z - 1),
                                      new Vector(x + 1, y, z - 1),
                                      new Vector(x - 1, y, z - 1));
            if (temp != null) {
                if (temp == true) {
                    return true;
                } else {
                    result = false;
                }
            }
        }

        // The sides of the block
        temp = isRedstoneHigh(new Vector(x - 1, y, z), north, false);
        if (temp != null) {
            if (temp == true) {
                return true;
            } else {
                result = false;
            }
        }

        temp = isRedstoneHigh(new Vector(x + 1, y, z), south, false);
        if (temp != null) {
            if (temp == true) {
                return true;
            } else {
                result = false;
            }
        }
        
        temp = isRedstoneHigh(new Vector(x, y, z + 1), west, false);
        if (temp != null) {
            if (temp == true) {
                return true;
            } else {
                result = false;
            }
        }

        temp = isRedstoneHigh(new Vector(x, y, z - 1), east, false);
        if (temp != null) {
            if (temp == true) {
                return true;
            } else {
                result = false;
            }
        }

        return result;
    }

    /**
     * Checks to see whether a wire is high and directed.
     * 
     * @param pt
     * @param sidePt1
     * @param sidePt2
     * @return
     */
    private Boolean isRedstoneWireHigh(Vector pt, Vector sidePt1, Vector sidePt2) {
        int side1 = CraftBook.getBlockID(sidePt1);
        int side1Above = CraftBook.getBlockID(sidePt1.add(0, 1, 0));
        int side1Below = CraftBook.getBlockID(sidePt1.add(0, -1, 0));
        int side2 = CraftBook.getBlockID(sidePt2);
        int side2Above = CraftBook.getBlockID(sidePt2.add(0, 1, 0));
        int side2Below = CraftBook.getBlockID(sidePt2.add(0, -1, 0));

        if (!BlockType.isRedstoneBlock(side1)
                && !BlockType.isRedstoneBlock(side1Above)
                && (!BlockType.isRedstoneBlock(side1Below) || side1 != 0)
                && !BlockType.isRedstoneBlock(side2)
                && !BlockType.isRedstoneBlock(side2Above)
                && (!BlockType.isRedstoneBlock(side2Below) || side2 != 0)) {
            return CraftBook.getBlockData(pt) > 0;
        }

        return null;
    }

    /**
     * Tests to see if a block is high, possibly including redstone wires. If
     * there was no redstone at that location, null will be returned.
     * 
     * @param pt
     * @param type
     * @param considerWires
     * @return
     */
    private Boolean isRedstoneHigh(Vector pt, int type, boolean considerWires) {
        if (type == BlockType.LEVER) {
            return (CraftBook.getBlockData(pt) & 0x8) == 0x8;
        } else if (type == BlockType.STONE_PRESSURE_PLATE) {
            return (CraftBook.getBlockData(pt) & 0x1) == 0x1;
        } else if (type == BlockType.WOODEN_PRESSURE_PLATE) {
            return (CraftBook.getBlockData(pt) & 0x1) == 0x1;
        } else if (type == BlockType.REDSTONE_TORCH_ON) {
            return true;
        } else if (type == BlockType.REDSTONE_TORCH_OFF) {
            return false;
        } else if (type == BlockType.STONE_BUTTON) {
            return (CraftBook.getBlockData(pt) & 0x8) == 0x8;
        } else if (considerWires && type == BlockType.REDSTONE_WIRE) {
            return CraftBook.getBlockData(pt) > 0;
        }

        return null;
    }

    /**
     * Tests to see if a block is high, possibly including redstone wires. If
     * there was no redstone at that location, null will be returned.
     *
     * @param pt
     * @param type
     * @param considerWires
     * @return
     */
    private Boolean isRedstoneHigh(Vector pt, boolean considerWires) {
        return isRedstoneHigh(pt, CraftBook.getBlockID(pt), considerWires);
    }

    /**
     * Tests to see if a block is high, possibly including redstone wires. If
     * there was no redstone at that location, null will be returned.
     *
     * @param pt
     * @param type
     * @param considerWires
     * @return
     */
    private boolean isRedstoneHighBinary(Vector pt, boolean considerWires) {
        Boolean result = isRedstoneHigh(pt, CraftBook.getBlockID(pt), considerWires);
        if (result != null && result) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tests the simple input at a block.
     * 
     * @param pt
     * @return
     */
    public Boolean testRedstoneSimpleInput(Vector pt) {
        Boolean result = null;
        Boolean temp;

        temp = isRedstoneHigh(pt.add(1, 0, 0), true);
        if (temp != null) if (temp) return true; else result = false;
        temp = isRedstoneHigh(pt.add(-1, 0, 0), true);
        if (temp != null) if (temp) return true; else result = false;
        temp = isRedstoneHigh(pt.add(0, 0, 1), true);
        if (temp != null) if (temp) return true; else result = false;
        temp = isRedstoneHigh(pt.add(0, 0, -1), true);
        if (temp != null) if (temp) return true; else result = false;
        temp = isRedstoneHigh(pt.add(0, -1, 0), true);
        if (temp != null) if (temp) return true; else result = false;
        return result;
    }

    /**
     * Gets the output state of a redstone IC at a location.
     *
     * @param getPosition
     * @param state
     */
    private boolean getRedstoneOutput(Vector pos) {
        if (CraftBook.getBlockID(pos) == BlockType.LEVER) {
            return (CraftBook.getBlockData(pos) & 0x8) == 0x8;
        } else {
            return false;
        }
    }

    /**
     * Sets the output state of a redstone IC at a location.
     *
     * @param getPosition
     * @param state
     */
    private void setRedstoneOutput(Vector pos, boolean state) {
        if (CraftBook.getBlockID(pos) == BlockType.LEVER) {
            int data = CraftBook.getBlockData(pos);
            int newData = data & 0x7;

            if (!state) {
                newData = data & 0x7;
            } else {
                newData = data | 0x8;
            }

            if (newData != data) {
                CraftBook.setBlockData(pos, newData);
                etc.getServer().updateBlockPhysics(
                        pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), newData);
            }
        }
    }

    /**
     * Sets the output state of a minecart trigger at a location.
     *
     * @param getPosition
     * @param state
     */
    private void setTrackRedstoneTrigger(Vector pos) {
        if (CraftBook.getBlockID(pos) == BlockType.LEVER) {
            int data = CraftBook.getBlockData(pos);
            int newData = 0;
            boolean state = (data & 0x8) == 0x8;

            if (state) {
                newData = data & 0x7;
            } else {
                newData = data | 0x8;
            }

            CraftBook.setBlockData(pos, newData);
            etc.getServer().updateBlockPhysics(
                    pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), newData);
        }
    }

    /**
     * Get the registered IC.
     * 
     * @param id
     * @return
     */
    public IC getIC(String id) {
        // SISO family
        SISOFamilyIC sisoIC = sisoICs.get(id);

        if (sisoIC != null) {
            return sisoIC;
        }

        // SI3O family
        SI3OFamilyIC si30IC = si3oICs.get(id);

        if (si30IC != null) {
            return si30IC;
        }

        // 3ISO family
        _3ISOFamilyIC _3isoIC = _3isoICs.get(id);

        if (_3isoIC != null) {
            return _3isoIC;
        }

        // VIVO family
        VIVOFamilyIC vivoIC = vivoICs.get(id);
        
        if(vivoIC != null) {
            return vivoIC;
        }

        return null;
    }

    /**
     * Called when either a sign, chest or furnace is changed.
     *
     * @param player
     *            player who changed it
     * @param cblock
     *            complex block that changed
     * @return true if you want any changes to be reverted
     */
    public boolean onComplexBlockChange(Player player, ComplexBlock cblock) {
        if (cblock instanceof Sign) {
            Sign sign = (Sign)cblock;
            int type = CraftBook.getBlockID(
                    cblock.getX(), cblock.getY(), cblock.getZ());
            
            String line2 = sign.getText(1);
            int len = line2.length();

            if (checkCreatePermissions) {
                // Gate
                if (line2.equalsIgnoreCase("[Gate]")) {
                    if (!player.canUseCommand("/makegate")) {
                        player.sendMessage(Colors.Rose
                                + "You don't have permission to make gates.");
                        CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                        return true;
                    }
                // Light switch
                } else if (line2.equalsIgnoreCase("[|]")
                        || line2.equalsIgnoreCase("[I]")) {
                    if (!player.canUseCommand("/makelightswitch")) {
                        player.sendMessage(Colors.Rose
                                + "You don't have permission to make light switches.");
                        CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                        return true;
                    }

                // Elevator
                } else if (line2.equalsIgnoreCase("[Lift Up]")
                        || line2.equalsIgnoreCase("[Lift Down]")
                        || line2.equalsIgnoreCase("[Lift]")) {
                    if (!player.canUseCommand("/makeelevator")) {
                        player.sendMessage(Colors.Rose
                                + "You don't have permission to make elevators.");
                        CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                        return true;
                    }

                // Toggle areas
                } else if (line2.equalsIgnoreCase("[Toggle]")) {
                    if (!player.canUseCommand("/maketogglearea")) {
                        player.sendMessage(Colors.Rose
                                + "You don't have permission to make toggle areas.");
                        CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                        return true;
                    }

                // Bridges
                } else if (line2.equalsIgnoreCase("[Bridge]")) {
                    if (!player.canUseCommand("/makebridge")) {
                        player.sendMessage(Colors.Rose
                                + "You don't have permission to make bridges.");
                        CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                        return true;
                    }
                }
            }

            // PLC code blocks
            if (line2.equalsIgnoreCase("[Code Block]")) {
                sign.setText(1, "[Code Block]");
                player.sendMessage("PLC code block detected.");
                return false;
            }

            // ICs
            if (line2.length() > 4
                    && line2.substring(0, 3).equalsIgnoreCase("[MC") &&
                    line2.charAt(len - 1) == ']') {

                informUser(player);

                // Check to see if the player can even create ICs
                if (checkCreatePermissions
                        && !player.canUseCommand("/makeic")) {
                    player.sendMessage(Colors.Rose
                            + "You don't have permission to make ICs.");
                    CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                    return true;
                }

                String id = line2.substring(1, len - 1).toUpperCase();

                if (id.equalsIgnoreCase("MC5000")) {
                    if (!redstonePLCs) {
                        player.sendMessage(Colors.Rose + "PLCs are not enabled.");
                        CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                        return true;
                    }
                }

                IC ic = getIC(id);
                if (ic != null) {
                    if (ic.requiresPermission() && !player.canUseCommand("/allic")
                             && !player.canUseCommand("/" + id.toLowerCase())) {
                        player.sendMessage(Colors.Rose
                                + "You don't have permission to make " + id + ".");
                        CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                        return true;
                    } else {
                        // To check the environment
                        Vector pos = new Vector(cblock.getX(), cblock.getY(), cblock.getZ());
                        SignText signText = new SignText(
                            sign.getText(0), sign.getText(1), sign.getText(2),
                            sign.getText(3));

                        // Maybe the IC is setup incorrectly
                        String envError = ic.validateEnvironment(pos, signText);

                        if (envError != null) {
                            player.sendMessage(Colors.Rose
                                    + "Error: " + envError);
                            CraftBook.dropSign(cblock.getX(), cblock.getY(), cblock.getZ());
                            return true;
                        } else {
                            sign.setText(0, ic.getTitle());
                            sign.setText(1, "[" + id + "]");
                        }
                    }
                } else {
                    sign.setText(1, Colors.Red + line2);
                    player.sendMessage(Colors.Rose + "Unrecognized IC: " + id);
                }

                if (!redstoneICs) {
                    player.sendMessage(Colors.Rose + "Warning: ICs are disabled.");
                } else if (type == BlockType.SIGN_POST) {
                    player.sendMessage(Colors.Rose + "Warning: IC signs must be on a wall.");
                }

                return false;
            }
        }
        
        return false;
    }

    /**
     *
     * @param player
     * @param split
     * @return whether the command was processed
     */
    @Override
    public boolean onCommand(Player player, String[] split) {
        try {
            return runCommand(player, split);
        } catch (InsufficientArgumentsException e) {
            player.sendMessage(Colors.Rose + e.getMessage());
            return true;
        } catch (LocalWorldEditBridgeException e) {
            if (e.getCause() != null) {
                Throwable cause = e.getCause();
                if (cause.getClass().getCanonicalName().equals("com.sk89q.worldedit.IncompleteRegionException")) {
                    player.sendMessage(Colors.Rose + "Region not fully defined (via WorldEdit).");
                } else if (cause.getClass().getCanonicalName().equals("com.sk89q.worldedit.WorldEditNotInstalled")) {
                    player.sendMessage(Colors.Rose + "The WorldEdit plugin is not loaded.");
                } else {
                    player.sendMessage(Colors.Rose + "Unknown CraftBook<->WorldEdit error: " + cause.getClass().getCanonicalName());
                }
            } else {
                player.sendMessage(Colors.Rose + "Unknown CraftBook<->WorldEdit error: " + e.getMessage());
            }

            return true;
        }
    }
    
    /**
     *
     * @param player
     * @param split
     * @return whether the command was processed
     */
    public boolean runCommand(Player player, String[] split)
            throws InsufficientArgumentsException, LocalWorldEditBridgeException {
        if (split[0].equalsIgnoreCase("/savearea") && canUse(player, "/savearea")) {
            checkArgs(split, 1, -1, split[0]);

            String name = joinString(split, " ", 1);

            if (!CopyManager.isValidName(name)) {
                player.sendMessage(Colors.Rose + "Invalid area name.");
                return true;
            }
            
            try {
                Vector min = LocalWorldEditBridge.getRegionMinimumPoint(player);
                Vector max = LocalWorldEditBridge.getRegionMaximumPoint(player);
                Vector size = max.subtract(min).add(1, 1, 1);

                if (size.getBlockX() * size.getBlockY() * size.getBlockZ() > maxToggleAreaSize) {
                    player.sendMessage(Colors.Rose + "Area is larger than allowed "
                            + maxToggleAreaSize + " blocks.");
                    return true;
                }

                CuboidCopy copy = new CuboidCopy(min, size);
                copy.copy();
                try {
                    copies.save(name, copy);
                    player.sendMessage(Colors.Gold + "Area saved as '" + name + "'");
                } catch (IOException e) {
                    player.sendMessage(Colors.Rose + "Could not save area: " + e.getMessage());
                }
            } catch (NoClassDefFoundError e) {
                player.sendMessage(Colors.Rose + "WorldEdit.jar does not exist in plugins/.");
            }

            return true;
        }

        return false;
    }

    /**
     * Get a block bag.
     * 
     * @param origin
     * @return
     */
    public BlockBag getBlockBag(Vector origin) {
        if (useChestsBlockBag) {
            return new NearbyChestBlockBag(origin);
        } else {
            return dummyBlockBag;
        }
    }

    /**
     * Called when a vehicle enters or leaves a block
     *
     * @param vehicle the vehicle
     */
    @Override
    public void onVehicleUpdate(BaseVehicle vehicle) {
        if (!minecartControlBlocks && !hinderUnoccupiedSlowdown) {
            return;
        }

        if (vehicle instanceof Minecart) {
            Minecart minecart = (Minecart)vehicle;

            int blockX = (int)Math.floor(minecart.getX());
            int blockY = (int)Math.floor(minecart.getY());
            int blockZ = (int)Math.floor(minecart.getZ());
            Vector underPt = new Vector(blockX, blockY - 1, blockZ);
            int under = CraftBook.getBlockID(blockX, blockY - 1, blockZ);

            if (minecartControlBlocks) {
                if (under == BlockType.OBSIDIAN) {
                    Boolean test = testRedstoneSimpleInput(underPt);

                    if (test != null) {
                        if (!test) {
                            minecart.setMotion(0, 0, 0);
                            return;
                        }
                    }
                }
            }

            if (hinderPressurePlateMinecartSlow
                    && under == BlockType.STONE_PRESSURE_PLATE
                    || under == BlockType.WOODEN_PRESSURE_PLATE) {
                // Numbers from code
                minecart.setMotion(minecart.getMotionX() / 0.55000000000000004D,
                                   0,
                                   minecart.getMotionZ() / 0.55000000000000004D);
            }

            if (hinderUnoccupiedSlowdown && minecart.getPassenger() == null) {
                minecart.setMotionX(minecart.getMotionX() * 1.0188250000000001D);
                minecart.setMotionZ(minecart.getMotionZ() * 1.0188250000000001D);
            }
        }
    }
    /**
     * Called when a vehicle changes block
     *
     * @param vehicle the vehicle
     * @param blockX coordinate x
     * @param blockY coordinate y
     * @param blockZ coordinate z
     */
    @Override
    public void onVehiclePositionChange(BaseVehicle vehicle,
            int blockX, int blockY, int blockZ) {
        if (!minecartControlBlocks && !minecartTrackMessages && !minecartDispensers) {
            return;
        }

        if (vehicle instanceof Minecart) {
            Minecart minecart = (Minecart)vehicle;

            Vector underPt = new Vector(blockX, blockY - 1, blockZ);
            int under = CraftBook.getBlockID(blockX, blockY - 1, blockZ);

            if (minecartControlBlocks) {
                if (under == minecart25xBoostBlock) {
                    Boolean test = testRedstoneSimpleInput(underPt);

                    if (test == null || test) {
                        minecart.setMotionX(minecart.getMotionX() * 1.25);
                        minecart.setMotionZ(minecart.getMotionZ() * 1.25);
                        return;
                    }
                } else if (under == minecart100xBoostBlock) {
                    Boolean test = testRedstoneSimpleInput(underPt);

                    if (test == null || test) {
                        minecart.setMotionX(minecart.getMotionX() * 2);
                        minecart.setMotionZ(minecart.getMotionZ() * 2);
                        return;
                    }
                } else if (under == minecart50xSlowBlock) {
                    Boolean test = testRedstoneSimpleInput(underPt);

                    if (test == null || test) {
                        minecart.setMotionX(minecart.getMotionX() * 0.5);
                        minecart.setMotionZ(minecart.getMotionZ() * 0.5);
                        return;
                    }
                } else if (under == minecart20xSlowBlock) {
                    Boolean test = testRedstoneSimpleInput(underPt);

                    if (test == null || test) {
                        minecart.setMotionX(minecart.getMotionX() * 0.8);
                        minecart.setMotionZ(minecart.getMotionZ() * 0.8);
                        return;
                    }
                } else if (under == minecartReverseBlock) {
                    Boolean test = testRedstoneSimpleInput(underPt);

                    if (test == null || test) {
                        Vector signPos = new Vector(blockX, blockY - 2, blockZ);
                        boolean reverseX = true;
                        boolean reverseZ = true;

                        // Directed reverse block
                        if (CraftBook.getBlockID(signPos) == BlockType.SIGN_POST
                                && doesSignSay(signPos, 1, "[Reverse]")) {
                            Vector dir = getSignPostOrthogonalBack(signPos, 1)
                                    .subtract(signPos);

                            // Acceptable sign direction
                            if (dir != null) {
                                if (CBMath.isSameSign(minecart.getMotionX(),
                                        dir.getBlockX())) {
                                    reverseX = false;
                                }
                                if (CBMath.isSameSign(minecart.getMotionZ(),
                                        dir.getBlockZ())) {
                                    reverseZ = false;
                                }
                            }
                        }
                        
                        if (reverseX) {
                            minecart.setMotionX(minecart.getMotionX() * -1);
                        }
                        if (reverseZ) {
                            minecart.setMotionZ(minecart.getMotionZ() * -1);
                        }
                        
                        return;
                    }
                } else if (under == minecartTriggerBlock) {
                    setTrackRedstoneTrigger(underPt.add(1, 0, 0));
                    setTrackRedstoneTrigger(underPt.add(-1, 0, 0));
                    setTrackRedstoneTrigger(underPt.add(0, 0, 1));
                    setTrackRedstoneTrigger(underPt.add(0, 0, -1));

                    return;
                } else if (under == minecartEjectBlock) {
                    Boolean test = testRedstoneSimpleInput(underPt);

                    if (test == null || test) {
                        Player player = minecart.getPassenger();
                        if (player != null) {
                            // Let's find a place to put the player
                            Location loc = player.getLocation();
                            Vector signPos = new Vector(blockX, blockY - 2, blockZ);

                            if (CraftBook.getBlockID(signPos) == BlockType.SIGN_POST
                                    && doesSignSay(signPos, 1, "[Eject]")) {
                                Vector pos = getSignPostOrthogonalBack(signPos, 1);

                                // Acceptable sign direction
                                if (pos != null) {
                                    pos = pos.setY(blockY);

                                    // Is the spot free?
                                    if (BlockType.canPassThrough(CraftBook.getBlockID(pos.add(0, 1, 0)))
                                            && BlockType.canPassThrough(CraftBook.getBlockID(pos))) {
                                        loc = new Location(
                                                pos.getBlockX(),
                                                pos.getBlockY(),
                                                pos.getBlockZ());

                                        ComplexBlock cBlock = etc.getServer().getComplexBlock(
                                                blockX, blockY - 2, blockZ);

                                        if (cBlock instanceof Sign) {
                                            Sign sign = (Sign)cBlock;
                                            String text = sign.getText(0);
                                            if (text.length() > 0) {
                                                player.sendMessage(Colors.Gold + "You've arrived at: "
                                                        + text);
                                            }
                                        }
                                    }
                                }
                            }

                            loc.x = loc.x + 0.5;
                            loc.z = loc.z + 0.5;

                            player.teleportTo(loc);
                        }
                    }

                    return;
                }
            }

            if (minecartTrackMessages
                    && CraftBook.getBlockID(underPt.add(0, -1, 0)) == BlockType.SIGN_POST) {
                Vector signPos = underPt.add(0, -1, 0);
                
                Boolean test = testRedstoneSimpleInput(signPos);

                if (test == null || test) {
                    ComplexBlock cblock = etc.getServer().getComplexBlock(
                            signPos.getBlockX(), signPos.getBlockY(), signPos.getBlockZ());

                    if (!(cblock instanceof Sign)) {
                        return;
                    }

                    Sign sign = (Sign)cblock;
                    String line1 = sign.getText(0);

                    if (line1.equalsIgnoreCase("[Print]")) {
                        Player player = minecart.getPassenger();
                        if (player == null) { return; }
                        
                        String name = player.getName();
                        String msg = sign.getText(1) + sign.getText(2) + sign.getText(3);
                        long now = System.currentTimeMillis();

                        if (lastMinecartMsg.containsKey(name)) {
                            String lastMessage = lastMinecartMsg.get(name);
                            if (lastMessage.equals(msg)
                                    && now < lastMinecartMsgTime.get(name) + 3000) {
                                return;
                            }
                        }

                        lastMinecartMsg.put(name, msg);
                        lastMinecartMsgTime.put(name, now);
                        player.sendMessage("> " + msg);
                    }
                }
            }

            if (minecartDispensers) {
                Vector pt = new Vector(blockX, blockY, blockZ);
                Vector depositPt = null;

                if (CraftBook.getBlockID(pt.add(1, 0, 0)) == BlockType.CHEST
                        && CraftBook.getBlockID(pt.add(-1, 0, 0)) == BlockType.MINECART_TRACKS) {
                    depositPt = pt.add(1, 0, 0);
                } else if(CraftBook.getBlockID(pt.add(-1, 0, 0)) == BlockType.CHEST
                        && CraftBook.getBlockID(pt.add(1, 0, 0)) == BlockType.MINECART_TRACKS) {
                    depositPt = pt.add(-1, 0, 0);
                } else if(CraftBook.getBlockID(pt.add(0, 0, 1)) == BlockType.CHEST
                        && CraftBook.getBlockID(pt.add(0, 0, -1)) == BlockType.MINECART_TRACKS) {
                    depositPt = pt.add(0, 0, 1);
                } else if(CraftBook.getBlockID(pt.add(0, 0, -1)) == BlockType.CHEST
                        && CraftBook.getBlockID(pt.add(0, 0, 1)) == BlockType.MINECART_TRACKS) {
                    depositPt = pt.add(0, 0, -1);
                }

                if (depositPt != null) {
                    NearbyChestBlockBag blockBag = new NearbyChestBlockBag(depositPt);
                    blockBag.addSingleSourcePosition(depositPt);
                    blockBag.addSingleSourcePosition(depositPt.add(1, 0, 0));
                    blockBag.addSingleSourcePosition(depositPt.add(-1, 0, 0));
                    blockBag.addSingleSourcePosition(depositPt.add(0, 0, 1));
                    blockBag.addSingleSourcePosition(depositPt.add(0, 0, -1));

                    try {
                        blockBag.storeBlock(ItemType.MINECART);
                        blockBag.flushChanges();
                        minecart.destroy();
                    } catch (BlockBagException e) {
                    }
                }
            }
        }
    }

    /**
     * Called when vehicle receives damage
     *
     * @param vehicle
     * @param attacker entity that dealt the damage
     * @param damage
     * @return false to set damage
     */
    @Override
    public boolean onVehicleDamage(BaseVehicle vehicle,
            BaseEntity attacker, int damage) {

        if (!inCartControl) {
            return false;
        }

        Player passenger = vehicle.getPassenger();

        // Player.equals() now works correctly as of recent hMod versions
        if (passenger != null && vehicle instanceof Minecart
                && attacker.isPlayer()
                && attacker.getPlayer().equals(passenger)) {
            double speed = Math.sqrt(Math.pow(vehicle.getMotionX(), 2)
                    + Math.pow(vehicle.getMotionY(), 2)
                    + Math.pow(vehicle.getMotionZ(), 2));

            if (speed > 0.01) { // Stop the cart
                vehicle.setMotion(0, 0, 0);
            } else {
                // From hey0's code, and then stolen from WorldEdit
                double rot = (passenger.getRotation() - 90) % 360;
                if (rot < 0) {
                    rot += 360.0;
                }

                String dir = etc.getCompassPointForDirection(rot);
                
                if (dir.equals("N")) {
                    vehicle.setMotion(-0.1, 0, 0);
                } else if(dir.equals("NE")) {
                    vehicle.setMotion(-0.1, 0, -0.1);
                } else if(dir.equals("E")) {
                    vehicle.setMotion(0, 0, -0.1);
                } else if(dir.equals("SE")) {
                    vehicle.setMotion(0.1, 0, -0.1);
                } else if(dir.equals("S")) {
                    vehicle.setMotion(0.1, 0, 0);
                } else if(dir.equals("SW")) {
                    vehicle.setMotion(0.1, 0, 0.1);
                } else if(dir.equals("W")) {
                    vehicle.setMotion(0, 0, 0.1);
                } else if(dir.equals("NW")) {
                    vehicle.setMotion(-0.1, 0, 0.1);
                }
            }

            return true;
        }

        return false;
    }
    
    /**
     *
     * @param player
     */
    @Override
    public void onDisconnect(Player player) {
        beenToldVersion.remove(player.getName());
        lastMinecartMsg.remove(player.getName());
        lastMinecartMsgTime.remove(player.getName());
    }

    /**
     * Get the CraftBook session associated with a player. A new session
     * will be created if needed.
     *
     * @param player
     * @return
     */
    /*public CraftBookSession getSession(Player player) {
        if (sessions.containsKey(player.getName())) {
            return sessions.get(player.getName());
        } else {
            CraftBookSession session = new CraftBookSession();
            sessions.put(player.getName(), session);
            return session;
        }
    }*/

    /**
     * Tells a user once about the CraftBook version.
     * 
     * @param player
     */
    private void informUser(Player player) {
        if (beenToldVersion.contains(player.getName())) {
            return;
        }

        player.sendMessage(Colors.LightGray + "Powered by CraftBook ver. " +
                craftBook.getVersion() + " by sk89q <sk89q.com>");

        beenToldVersion.add(player.getName());
    }

    /**
     * Check if a player can use a command.
     *
     * @param player
     * @param command
     * @return
     */
    public boolean canUse(Player player, String command) {
        return player.canUseCommand(command);
    }

    /**
     * Check if a player can use a command. May be overrided if permissions
     * checking is disabled.
     * 
     * @param player
     * @param command
     * @return
     */
    public boolean checkPermission(Player player, String command) {
        return !checkPermissions || player.canUseCommand(command);
    }

    /**
     * Gets the block behind a sign.
     *
     * @param x
     * @param y
     * @param z
     * @param multiplier
     * @return
     */
    private static Vector getWallSignBack(Vector pt, int multiplier) {
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();
        int data = CraftBook.getBlockData(x, y, z);
        if (data == 0x2) { // East
            return new Vector(x, y, z + multiplier);
        } else if (data == 0x3) { // West
            return new Vector(x, y, z - multiplier);
        } else if (data == 0x4) { // North
            return new Vector(x + multiplier, y, z);
        } else {
            return new Vector(x - multiplier, y, z);
        }
    }

    /**
     * Gets the block behind a sign.
     *
     * @param x
     * @param y
     * @param z
     * @param multiplier
     * @return
     */
    private static Vector getSignPostOrthogonalBack(Vector pt, int multiplier) {
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();
        int data = CraftBook.getBlockData(x, y, z);
        if (data == 0x8) { // East
            return new Vector(x, y, z + multiplier);
        } else if (data == 0x0) { // West
            return new Vector(x, y, z - multiplier);
        } else if (data == 0x4) { // North
            return new Vector(x + multiplier, y, z);
        } else if (data == 0xC) { // South
            return new Vector(x - multiplier, y, z);
        } else {
            return null;
        }
    }

    /**
     * Gets the block next to a sign.
     *
     * @param x
     * @param y
     * @param z
     * @param multiplier
     * @return
     */
    private static Vector getWallSignSide(Vector pt, int multiplier) {
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();
        int data = CraftBook.getBlockData(x, y, z);
        if (data == 0x2) { // East
            return new Vector(x + multiplier, y, z );
        } else if (data == 0x3) { // West
            return new Vector(x - multiplier, y, z);
        } else if (data == 0x4) { // North
            return new Vector(x, y, z - multiplier);
        } else {
            return new Vector(x, y, z + multiplier);
        }
    }

    /**
     * Checks whether a sign at a location has a certain text on a
     * particular line, case in-sensitive.
     * 
     * @param pt
     * @param lineNo
     * @param text
     * @return
     */
    public static boolean doesSignSay(Vector pt, int lineNo, String text) {
        ComplexBlock cBlock = etc.getServer().getComplexBlock(
                pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

        if (cBlock instanceof Sign) {
            Sign sign = (Sign)cBlock;
            return text.equalsIgnoreCase(sign.getText(lineNo));
        }

        return false;
    }

    /**
     * Change a block ID to its name.
     * 
     * @param id
     * @return
     */
    private static String toBlockName(int id) {
        com.sk89q.worldedit.blocks.BlockType blockType =
                com.sk89q.worldedit.blocks.BlockType.fromID(id);

        if (blockType == null) {
            return "#" + id;
        } else {
            return blockType.getName();
        }
    }

    /**
     * Parse a list of cauldron items.
     * 
     * @param list
     * @return
     */
    private static List<Integer> parseCauldronItems(String list) {
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
                    int item = etc.getDataSource().getItem(part);

                    if (item > 0) {
                        for (int i = 0; i < multiplier; i++) {
                            out.add(item);
                        }
                    } else {
                        logger.log(Level.WARNING, "Cauldron: Unknown item " + part);
                    }
                }
            } catch (NumberFormatException e) { // Bad multiplier
                logger.log(Level.WARNING, "Cauldron: Bad multiplier in '" + part + "'");
            }
        }

        return out;
    }

    /**
     * Read a file containing cauldron recipes.
     *
     * @param file
     * @return
     * @throws IOException
     */
    private static CauldronCookbook readCauldronRecipes(String path)
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
                    logger.log(Level.WARNING, "Invalid cauldron recipe line in "
                            + file.getName() + ": '" + line + "'");
                } else {
                    String name = parts[0];
                    List<Integer> ingredients = parseCauldronItems(parts[1]);
                    List<Integer> results = parseCauldronItems(parts[2]);
                    
                    CauldronRecipe recipe =
                            new CauldronRecipe(name, ingredients, results);
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
     * Joins a string from an array of strings.
     *
     * @param str
     * @param delimiter
     * @return
     */
    private static String joinString(String[] str, String delimiter,
            int initialIndex) {
        if (str.length == 0) {
            return "";
        }
        StringBuilder buffer = new StringBuilder(str[initialIndex]);
        for (int i = initialIndex + 1; i < str.length; i++) {
            buffer.append(delimiter).append(str[i]);
        }
        return buffer.toString();
    }

    /**
     * Repeat a string.
     * 
     * @param string
     * @param num
     * @return
     */
    private static String repeatString(String str, int num) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < num; i++) {
            buffer.append(str);
        }
        return buffer.toString();
    }
}