// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * FileConfiguration handler for CraftBook.
 * 
 * All fields are final because it is never appropriate to modify them during
 * operation, except for when the FileConfiguration is reloaded entirely, at which
 * point it is appropriate to construct an entirely new FileConfiguration instance
 * and update the plugin accordingly.
 * 
 * @author sk89q
 * @author hash
 */
public class MechanismsConfiguration {
    public MechanismsConfiguration(FileConfiguration cfg, File dataFolder) {
        this.dataFolder = dataFolder;
        ammeterSettings = new AmmeterSettings(cfg);
        bookcaseSettings = new BookcaseSettings(cfg);
        bridgeSettings = new BridgeSettings(cfg);
        doorSettings = new DoorSettings(cfg);        
        gateSettings = new GateSettings(cfg);
        elevatorSettings = new ElevatorSettings(cfg);
        cauldronSettings = new CauldronSettings(cfg);
        lightStoneSettings = new LightStoneSettings(cfg);
        lightSwitchSettings = new LightSwitchSettings(cfg);
        hiddenSwitchSettings = new HiddenSwitchSettings(cfg);
        snowSettings = new SnowSettings(cfg);
        customDropSettings = new CustomDropSettings(dataFolder);
        areaSettings = new AreaSettings(cfg);
    }
    
    public final File dataFolder;
    public final AmmeterSettings ammeterSettings;
    public final BookcaseSettings bookcaseSettings;
    public final BridgeSettings bridgeSettings;
    public final DoorSettings doorSettings;    
    public final GateSettings gateSettings;
    public final ElevatorSettings elevatorSettings;
    public final CauldronSettings cauldronSettings;
    public final LightStoneSettings lightStoneSettings;
    public final LightSwitchSettings lightSwitchSettings;
    public final HiddenSwitchSettings hiddenSwitchSettings;
    public final SnowSettings snowSettings;
    public final CustomDropSettings customDropSettings;
    public final AreaSettings areaSettings;
    
    
    public class BookcaseSettings {
        public final boolean enable;
        public final String readLine;

        private BookcaseSettings(FileConfiguration cfg) {
            enable      = cfg.getBoolean("bookshelf-enable",             true);
            readLine    = cfg.getString( "bookshelf-read-text",          "You pick up a book...");
        }
        //FIXME the books file should probably be cached here too
    }
    
    
    
    public class BridgeSettings {
        public final boolean enable;
        public final boolean enableRedstone;
        public final int maxLength;
        public final Set<Material> allowedBlocks;
        
        private BridgeSettings(FileConfiguration cfg) {
            enable             = cfg.getBoolean("bridge-enable",             true);
            enableRedstone     = cfg.getBoolean("bridge-redstone",           true);
            maxLength          = cfg.getInt(    "bridge-max-length",         30);
            List<Integer> tids = cfg.getIntegerList("bridge-blocks");
            if (tids == null) tids = Arrays.asList(4,5,20,43);
            Set<Material> allowedBlocks = new HashSet<Material>();
            for (Integer tid: tids) allowedBlocks.add(Material.getMaterial(tid));
            this.allowedBlocks = Collections.unmodifiableSet(allowedBlocks);
        }
        
        /**
         * @param b
         * @return true if the given block type can be used for a bridge; false
         *         otherwise.
         */
        public boolean canUseBlock(Material b) {
            return allowedBlocks.contains(b);
        }
    }
    
    public class DoorSettings {
        public final boolean enable;
        public final boolean enableRedstone;
        public final int maxLength;
        public final Set<Material> allowedBlocks;
        
        private DoorSettings(FileConfiguration cfg) {
            enable             = cfg.getBoolean("door-enable",             true);
            enableRedstone     = cfg.getBoolean("door-redstone",           true);
            maxLength          = cfg.getInt(    "door-max-length",         30);
            List<Integer> tids = cfg.getIntegerList("door-blocks");
            if(tids == null) Arrays.asList(4,5,20,43);
            Set<Material> allowedBlocks = new HashSet<Material>();
            for (Integer tid: tids) allowedBlocks.add(Material.getMaterial(tid));
            this.allowedBlocks = Collections.unmodifiableSet(allowedBlocks);
        }
        
        /**
         * @param b
         * @return true if the given block type can be used for a bridge; false
         *         otherwise.
         */
        public boolean canUseBlock(Material b) {
            return allowedBlocks.contains(b);
        }
    }

    
    
    public class GateSettings {
        public final boolean enable;
        public final boolean enableRedstone;

        private GateSettings(FileConfiguration cfg) {
            enable             = cfg.getBoolean("gate-enable",             true);
            enableRedstone     = cfg.getBoolean("gate-redstone",           true);
        }
    }
    
    
    
    public class ElevatorSettings {
        public final boolean enable;

        private ElevatorSettings(FileConfiguration cfg) {
            enable             = cfg.getBoolean("elevators-enable",        true);
        }
    }
    
    
    
    public class CauldronSettings {
        public final boolean enable;

        private CauldronSettings(FileConfiguration cfg) {
            enable             = cfg.getBoolean("cauldron-enable",         true);
        }
        //FIXME the recipes should probably go here
    }
    
    
    
    public class LightSwitchSettings {
        public final boolean enable;

        private LightSwitchSettings(FileConfiguration cfg) {
            enable             = cfg.getBoolean("light-switch-enable",     true);
        }
    }
    
    public class LightStoneSettings {
        public final boolean enable;

        private LightStoneSettings(FileConfiguration cfg) {
            enable             = cfg.getBoolean("light-stone-enable",      true);
        }
    }    
    public class AmmeterSettings {
        public final boolean enable;

        private AmmeterSettings(FileConfiguration cfg) {
            enable             = cfg.getBoolean("ammeter-enable",          true);
        }
    }
    public class HiddenSwitchSettings {
        public final boolean enable;

        private HiddenSwitchSettings(FileConfiguration cfg) {
            enable             = cfg.getBoolean("hidden-switches-enable",  true);
        }
    }
    public class SnowSettings {
        public final boolean enable;
        public final boolean trample;
        public final boolean placeSnow;

        private SnowSettings(FileConfiguration cfg) {
            enable             = cfg.getBoolean("snow-piling-enable",  true);
            trample             = cfg.getBoolean("snow-trample-enable",  true);
            placeSnow			= cfg.getBoolean("placable-snow", true);
        }
    }
    
    public class AreaSettings {
        public final boolean enable;
        public final boolean enableRedstone;
        public final int maxAreasPerUser;
        public final int maxSizePerArea;

        private AreaSettings(FileConfiguration cfg) {
            enable             = cfg.getBoolean("area-enable",             true);
            enableRedstone     = cfg.getBoolean("area-redstone",           true);
            maxAreasPerUser     = cfg.getInt("max-areas-per-user",           30);
            maxSizePerArea     = cfg.getInt("max-size-per-area",           5000);
        }
    }
    
    public class CustomDropSettings {
	
	public final ArrayList<String> blockData;
	public final ArrayList<String> mobData;
	
        private CustomDropSettings(File location) {
            blockData = new ArrayList<String>();
            try {
                File drops = new File(location, "CustomDrops.txt");
                if(!drops.exists()) drops.createNewFile();
                BufferedReader br = new BufferedReader(new FileReader(drops));
                String line = "";
                while((line = br.readLine())!=null) {
                    if(line!=null && line.trim().length() > 1) {
                	if(!blockData.contains(line.trim()))
                	    blockData.add(line.trim());
                    }
                }
                br.close();
            }
            catch(Exception e){
        	Bukkit.getLogger().log(Level.SEVERE, "Failed to load custom drops!");
            }
            mobData = new ArrayList<String>();
            try {
                File drops = new File(location, "CustomMobDrops.txt");
                if(!drops.exists()) drops.createNewFile();
                BufferedReader br = new BufferedReader(new FileReader(drops));
                String line = "";
                while((line = br.readLine())!=null) {
                    if(line!=null && line.trim().length() > 1) {
                	if(!mobData.contains(line.trim()))
                	    mobData.add(line.trim());
                    }
                }
                br.close();
            }
            catch(Exception e){
        	Bukkit.getLogger().log(Level.SEVERE, "Failed to load custom mob drops!");
            }
        }
    }
}
