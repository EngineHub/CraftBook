// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 * Copyright (C) 2010 Lymia <lymiahugs@gmail.com>
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sk89q.craftbook.access.Configuration;
import com.sk89q.craftbook.access.Event;
import com.sk89q.craftbook.access.ServerInterface;
import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.blockbag.AdminBlockSource;
import com.sk89q.craftbook.blockbag.BlockBag;
import com.sk89q.craftbook.blockbag.BlockBagFactory;
import com.sk89q.craftbook.blockbag.CompoundBlockBag;
import com.sk89q.craftbook.blockbag.DummyBlockBag;
import com.sk89q.craftbook.blockbag.NearbyChestBlockBag;
import com.sk89q.craftbook.state.StateManager;
import com.sk89q.craftbook.util.Vector;

/**
 * CraftBook's core object.
 *
 * @author sk89q
 */
public class CraftBookCore {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.CraftBook");
    private static final File pathToState = new File("world"+File.separator+"craftbook");
    
    /** 
     * A list of block bags that can be used. This map is populated on
     * class loaded via static constructor.
     * 
     * Block bags are sources/sinks for getting blocks and storing blocks,
     * which some features use if some fairness is to be kept. Used block bags
     * are defined by the user.
     */
    private static final Map<String,BlockBagFactory> BLOCK_BAGS =
        new HashMap<String,BlockBagFactory>();
    
    /*
     * Static constructor to populate the list of available block bag types.
     */
    static {
        BLOCK_BAGS.put("unlimited-black-hole",
                new DummyBlockBag.UnlimitedBlackHoleFactory());
        BLOCK_BAGS.put("black-hole",
                new DummyBlockBag.BlackHoleFactory());
        BLOCK_BAGS.put("unlimited-block-source",
                new DummyBlockBag.UnlimitedSourceFactory());
        BLOCK_BAGS.put("admin-black-hole", 
                new AdminBlockSource.BlackHoleFactory());
        BLOCK_BAGS.put("admin-block-source", 
                new AdminBlockSource.UnlimitedSourceFactory());
        BLOCK_BAGS.put("nearby-chests", new NearbyChestBlockBag.Factory());
    }
    
    /**
     * CraftBook version, fetched from the .jar's manifest. Used to print the
     * CraftBook version in various places.
     */
    private String version;
    
    /**
     * State manager object.
     */
    private StateManager stateManager = new StateManager();
    
    /**
     * State manager thread. 
     */
    private Thread stateThread = new Thread() {
        public void run() {
            while(server.isCraftBookLoaded()) {
                try {Thread.sleep(10*60*1000);} catch (InterruptedException e) {}
                if(server.isCraftBookLoaded()&&server.isCraftBookEnabled()) 
                    stateManager.save(pathToState);
            }
        }
    };
    
    /**
     * A list of block bag factories. The value of this list is determined by
     * the 'block-bags' configuration.
     */
    private List<BlockBagFactory> blockBags =
        new ArrayList<BlockBagFactory>();

    /**
     * Delegate listener for mechanisms.
     */
    private final CraftBookDelegateListener mechanisms;
    
    /**
     * Delegate listener for redstone.
     */
    private final CraftBookDelegateListener redstone;
    
    /**
     * Delegate listener for vehicle.
     */
    private final CraftBookDelegateListener vehicle;
    
    /**
     * Delegate listener for misc.
     */
    private final CraftBookDelegateListener misc;
    
    /**
     * Server interface object.
     */
    private final ServerInterface server;
    
    public CraftBookCore(ServerInterface server) {
        this.server = server;
        mechanisms = new MechanismListener(this, server);
        redstone = new RedstoneListener(this, server);
        vehicle = new VehicleListener(this, server);
        misc = new MiscListener(this, server);
    }
    
    /**
     * Initializes the plugin.
     */
    public void initialize() {
        pathToState.mkdirs();
        stateManager.load(pathToState);
        
        stateThread.setName("StateManager");
        stateThread.start();
    }
    
    /**
     * Enables the plugin.
     */
    public void enable() {
        logger.log(Level.INFO, "CraftBook version " + getVersion() + " loaded");
        loadConfiguration();
    }

    /**
     * Disables the plugin.
     */
    public void disable() {
        stateManager.save(pathToState);
    }
    
    public void loadConfiguration() {
        Configuration c = server.getConfiguration();
        
        loadBlockBags(c);
    }
    
    /**
     * Load the configured block bags.
     * 
     * @see #loadConfiguration()
     */
    private void loadBlockBags(Configuration c) {
        String blockBagsConfig;
        
        // Get the list of block bags to use
        if (c.hasKey("block-bag")) {
            logger.log(
                    Level.WARNING,
                    "CraftBook's block-bag configuration option is "
                            + "deprecated, and may be removed in a future version. Please use "
                            + "block-bags instead.");
            blockBagsConfig = c.getString("block-bags",
                    c.getString("block-bag",
                            "black-hole,unlimited-block-source"));
        } else {
            blockBagsConfig = c.getString("block-bags",
                    "black-hole,unlimited-block-source");
        }
    
        // Parse out block bags
        for (String s : blockBagsConfig.split(",")) {
            BlockBagFactory f = BLOCK_BAGS.get(s);
            
            if (f == null) {
                logger.log(Level.WARNING, "Unknown CraftBook block source: "
                        + s);
                
                // Add a default block bag
                this.blockBags.clear();
                this.blockBags.add(new BlockBagFactory() {
                    public BlockBag createBlockSource(WorldInterface w, Vector v) {
                        return new DummyBlockBag();
                    }
                });
                
                break;
            } else {
                this.blockBags.add(f);
            }
        }
    }

    /**
     * Get a block bag.
     * 
     * @param origin
     * @return
     */
    public BlockBag getBlockBag(WorldInterface w, Vector origin) {
        List<BlockBag> bags = new ArrayList<BlockBag>();
        
        for (BlockBagFactory f : blockBags) {
            
            BlockBag b = f.createBlockSource(w, origin);
            if (b == null) {
                continue;
            }
            
            bags.add(b);
        }
        
        return new CompoundBlockBag(bags);
    }

    /**
     * Get the CraftBook version.
     *
     * @return
     */
    public String getVersion() {
        if (version != null) {
            return version;
        }
        
        Package p = CraftBookCore.class.getPackage();
        
        if (p == null) {
            p = Package.getPackage("com.sk89q.craftbook");
        }
        
        if (p == null) {
            version = "(unknown)";
        } else {
            version = p.getImplementationVersion();
            
            if (version == null) {
                version = "(unknown)";
            }
        }

        return version;
    }
    
    public StateManager getStateManager() {
        return stateManager;
    }
}
