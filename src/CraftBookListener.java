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

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.CraftBookCore;
import com.sk89q.craftbook.CraftBookDelegateListener;
import com.sk89q.craftbook.blockbag.AdminBlockSource;
import com.sk89q.craftbook.blockbag.BlockBag;
import com.sk89q.craftbook.blockbag.BlockBagFactory;
import com.sk89q.craftbook.blockbag.CompoundBlockBag;
import com.sk89q.craftbook.blockbag.DummyBlockBag;
import com.sk89q.craftbook.blockbag.NearbyChestBlockBag;
import com.sk89q.craftbook.exception.InsufficientArgumentsException;
import com.sk89q.craftbook.exception.LocalWorldEditBridgeException;
import com.sk89q.craftbook.mech.area.CopyManager;
import com.sk89q.craftbook.util.BlockVector;
import com.sk89q.craftbook.util.LoggerToChatHandler;
import com.sk89q.craftbook.util.Vector;

/**
 * This is CraftBook's main event listener for Hey0's server mod. "Delegate"
 * listeners are also used for different features and this listener acts
 * as a proxy for some custom hooks and events.
 *
 * @author sk89q
 */
public class CraftBookListener extends PluginListener {
    /**
     * Logger instance.
     */
    static final Logger logger = Logger.getLogger("Minecraft.CraftBook");

    
    /**
     * Used for toggle-able areas.
     */
    private CopyManager copies = new CopyManager();

    /**
     * Stores an instance of the plugin. This is used to register delegate
     * listeners and access some global features.
     */
    public CraftBookCore craftBook; 

    /**
     * Stores who has been shown the CraftBook version.
     */
    private Set<String> beenToldVersion =
            new HashSet<String>();
    
    /**
     * Properties file for CraftBook. This instance is shared among this
     * listener and all delegates.
     */
    private PropertiesFile properties = new PropertiesFile("craftbook.properties");

    /**
     * Construct the object.
     * 
     * @param craftBook
     */
    public CraftBookListener(CraftBookCore craftBook) {
        this.craftBook = craftBook;
    }

    /**
     * Loads CraftBooks's configuration. This will update the features that use
     * the settings and delegates will be informed of changes.
     * 
     * @see CraftBookDelegateListener
     * @see #loadBlockBags()
     */
    public void loadConfiguration() {
        try {
            properties.load();
        } catch (IOException e) {
            logger.warning("Failed to load craftbook.properties: " + e.getMessage());
        }
        
        // Load the configuration for delegates -- assuming that none will
        // throw any exceptions
        for (CraftBookDelegateListener listener : delegates) {
            listener.loadConfiguration();
        }
    }
        
    /**
     * Called on redstone change.
     *
     * @param block
     * @param oldLevel
     * @param newLevel
     */
    public int onRedstoneChange(Block block, int oldLevel, int newLevel) {
        BlockVector v = new BlockVector(block.getX(), block.getY(), block.getZ());
        
        // Give the method a BlockVector instead of a Block
        return onRedstoneChange(v, oldLevel, newLevel);
    }
    
    /**
     * Called on redstone input change. This is passed a BlockVector. This
     * method is fairly complicated -- it has to deal with the block data
     * value for the changing block not having been set yet, and it also
     * needs to detect when redstone input is directed.
     * 
     * @param v
     * @param oldLevel
     * @param newLevel
     * @return
     */
    public int onRedstoneChange(BlockVector v, int oldLevel, int newLevel) {
        boolean wasOn = oldLevel >= 1;
        boolean isOn = newLevel >= 1;
        boolean wasChange = wasOn != isOn;

        // For efficiency reasons, we're only going to consider changes between
        // off and on state, and ignore simple current changes (i.e. 15->13)
        if (!wasChange) {
            return newLevel;
        }

        int x = v.getBlockX();
        int y = v.getBlockY();
        int z = v.getBlockZ();

        int type = CraftBookCore.getBlockID(x, y, z);

        // When this hook has been called, the level in the world has not
        // yet been updated, so we're going to do this very ugly thing of
        // faking the value with the new one whenever the data value of this
        // block is requested -- it is quite ugly
        try {
            if (type == BlockType.LEVER) {
                // Fake data
                CraftBookCore.fakeBlockData(x, y, z,
                        newLevel > 0
                            ? CraftBookCore.getBlockData(x, y, z) | 0x8
                            : CraftBookCore.getBlockData(x, y, z) & 0x7);
            } else if (type == BlockType.STONE_PRESSURE_PLATE) {
                // Fake data
                CraftBookCore.fakeBlockData(x, y, z,
                        newLevel > 0
                            ? CraftBookCore.getBlockData(x, y, z) | 0x1
                            : CraftBookCore.getBlockData(x, y, z) & 0x14);
            } else if (type == BlockType.WOODEN_PRESSURE_PLATE) {
                // Fake data
                CraftBookCore.fakeBlockData(x, y, z,
                        newLevel > 0
                            ? CraftBookCore.getBlockData(x, y, z) | 0x1
                            : CraftBookCore.getBlockData(x, y, z) & 0x14);
            } else if (type == BlockType.STONE_BUTTON) {
                // Fake data
                CraftBookCore.fakeBlockData(x, y, z,
                        newLevel > 0
                            ? CraftBookCore.getBlockData(x, y, z) | 0x8
                            : CraftBookCore.getBlockData(x, y, z) & 0x7);
            } else if (type == BlockType.REDSTONE_WIRE) {
                // Fake data
                CraftBookCore.fakeBlockData(x, y, z, newLevel);

                int westSide = CraftBookCore.getBlockID(x, y, z + 1);
                int westSideAbove = CraftBookCore.getBlockID(x, y + 1, z + 1);
                int westSideBelow = CraftBookCore.getBlockID(x, y - 1, z + 1);
                int eastSide = CraftBookCore.getBlockID(x, y, z - 1);
                int eastSideAbove = CraftBookCore.getBlockID(x, y + 1, z - 1);
                int eastSideBelow = CraftBookCore.getBlockID(x, y - 1, z - 1);

                int northSide = CraftBookCore.getBlockID(x - 1, y, z);
                int northSideAbove = CraftBookCore.getBlockID(x - 1, y + 1, z);
                int northSideBelow = CraftBookCore.getBlockID(x - 1, y - 1, z);
                int southSide = CraftBookCore.getBlockID(x + 1, y, z);
                int southSideAbove = CraftBookCore.getBlockID(x + 1, y + 1, z);
                int southSideBelow = CraftBookCore.getBlockID(x + 1, y - 1, z);

                // Make sure that the wire points to only this block
                if (!BlockType.isRedstoneBlock(westSide)
                        && !BlockType.isRedstoneBlock(eastSide)
                        && (!BlockType.isRedstoneBlock(westSideAbove) || westSide == 0)
                        && (!BlockType.isRedstoneBlock(eastSideAbove) || eastSide == 0)
                        && (!BlockType.isRedstoneBlock(westSideBelow) || westSide != 0)
                        && (!BlockType.isRedstoneBlock(eastSideBelow) || eastSide != 0)) {
                    // Possible blocks north / south
                    handleDirectWireInput(new Vector(x - 1, y, z), isOn, v);
                    handleDirectWireInput(new Vector(x + 1, y, z), isOn, v);
                    handleDirectWireInput(new Vector(x - 1, y - 1, z), isOn, v);
                    handleDirectWireInput(new Vector(x + 1, y - 1, z), isOn, v);
                }

                if (!BlockType.isRedstoneBlock(northSide)
                        && !BlockType.isRedstoneBlock(southSide)
                        && (!BlockType.isRedstoneBlock(northSideAbove) || northSide == 0)
                        && (!BlockType.isRedstoneBlock(southSideAbove) || southSide == 0)
                        && (!BlockType.isRedstoneBlock(northSideBelow) || northSide != 0)
                        && (!BlockType.isRedstoneBlock(southSideBelow) || southSide != 0)) {
                    // Possible blocks west / east
                    handleDirectWireInput(new Vector(x, y, z - 1), isOn, v);
                    handleDirectWireInput(new Vector(x, y, z + 1), isOn, v);
                    handleDirectWireInput(new Vector(x, y - 1, z - 1), isOn, v);
                    handleDirectWireInput(new Vector(x, y - 1, z + 1), isOn, v);
                }

                // Can be triggered from below
                handleDirectWireInput(new Vector(x, y + 1, z), isOn, v);

                return newLevel;
            }

            // For redstone wires, the code already exited this method
            // Non-wire blocks proceed

            handleDirectWireInput(new Vector(x - 1, y, z), isOn, v);
            handleDirectWireInput(new Vector(x + 1, y, z), isOn, v);
            handleDirectWireInput(new Vector(x - 1, y - 1, z), isOn, v);
            handleDirectWireInput(new Vector(x + 1, y - 1, z), isOn, v);
            handleDirectWireInput(new Vector(x, y, z - 1), isOn, v);
            handleDirectWireInput(new Vector(x, y, z + 1), isOn, v);
            handleDirectWireInput(new Vector(x, y - 1, z - 1), isOn, v);
            handleDirectWireInput(new Vector(x, y - 1, z + 1), isOn, v);

            // Can be triggered from below
            handleDirectWireInput(new Vector(x, y + 1, z), isOn, v);

            return newLevel;
        } finally {
            CraftBookCore.clearFakeBlockData();
        }
    }
    
    /**
     * Handles direct redstone input. This method merely passes the call
     * onto the delegates for further processing. If a delegate throws an
     * exception, it will not be caught here.
     * 
     * @param inputVec
     * @param isOn
     * @param changed
     * @see CraftBookDelegateListener#onDirectWireInput(Vector, boolean, Vector)
     */
    public void handleDirectWireInput(Vector pt, boolean isOn, Vector changed) {
        // Call the direct wire input hook of delegates
        for (CraftBookDelegateListener listener : delegates) {
            listener.onDirectWireInput(pt, isOn, changed);
        }
    }

    /**
     * Called on command.
     * 
     * @param player
     * @param split
     * @return whether the command was processed
     */
    @Override
    public boolean onCommand(Player player, String[] split) {
        try {
            if (!runCommand(player, split)) {
                for (CraftBookDelegateListener listener : delegates) {
                    if (listener.onCheckedCommand(player, split)) {
                        return true;
                    }
                }

                return false;
            }
            
            return true;
        } catch (InsufficientArgumentsException e) {
            player.sendMessage(Colors.Rose + e.getMessage());
            return true;
        } catch (LocalWorldEditBridgeException e) {
            if (e.getCause() != null) {
                Throwable cause = e.getCause();
                String causeName = cause.getClass().getCanonicalName();
                
                // If the player has not defined a region
                if (causeName.equals("com.sk89q.worldedit.IncompleteRegionException")) {
                    player.sendMessage(Colors.Rose + "Region not fully defined (via WorldEdit).");
                // If WorldEdit is not an installed plugin
                } else if (causeName.equals("com.sk89q.worldedit.WorldEditNotInstalled")) {
                    player.sendMessage(Colors.Rose + "The WorldEdit plugin is not loaded.");
                // An unknown error
                } else {
                    player.sendMessage(Colors.Rose + "Unknown CraftBook<->WorldEdit error: "
                            + cause.getClass().getCanonicalName());
                }
            } else {
                player.sendMessage(Colors.Rose + "Unknown CraftBook<->WorldEdit error: "
                        + e.getMessage());
            }

            return true;
        }
    }
    
    /**
     * Gets called on a command.
     *
     * @param player
     * @param split
     * @return whether the command was processed
     */
    public boolean runCommand(Player player, String[] split)
            throws InsufficientArgumentsException, LocalWorldEditBridgeException {    

        if (split[0].equalsIgnoreCase("/reload")
                && player.canUseCommand("/reload")
                && split.length > 1
                && (split[1].equalsIgnoreCase("CraftBook")
                    || split[1].equals("*"))) {
        
            // Redirect log messages to the player's chat.
            LoggerToChatHandler handler = new LoggerToChatHandler(player);
            handler.setLevel(Level.ALL);
            Logger minecraftLogger = Logger.getLogger("Minecraft");
            minecraftLogger.addHandler(handler);

            try {
                loadConfiguration();
                player.sendMessage("CraftBook configuration reloaded.");
            } catch (Throwable t) {
                player.sendMessage("Error while reloading: "
                        + t.getMessage());
            } finally {
                minecraftLogger.removeHandler(handler);
            }

            return !split[1].equals("*");
        } else if (split[0].equalsIgnoreCase("/craftbookversion")) {
            player.sendMessage(Colors.LightGray + "CraftBook version: " +
                    craftBook.getVersion());
            player.sendMessage(Colors.LightGray
                    + "Website: http://wiki.sk89q.com/wiki/CraftBook");

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
    }

    /**
     * Tells a user once about the CraftBook version.
     * 
     * @param player
     */
    protected void informUser(Player player) {
        if (beenToldVersion.contains(player.getName())) {
            return;
        }

        player.sendMessage(Colors.Gray + "Use /craftbookversion for version information");

        beenToldVersion.add(player.getName());
    }
    
    /**
     * Get the properties file.
     * 
     * @return
     */
    public PropertiesFile getProperties() {
        return properties;
    }
    
    /**
     * Get copy manager.
     * 
     * @return
     */
    public CopyManager getCopyManager() {
        return copies;
    }
}