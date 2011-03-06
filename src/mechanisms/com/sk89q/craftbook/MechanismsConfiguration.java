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

import java.io.File;
import java.util.*;

import org.bukkit.*;
import org.bukkit.util.config.*;

import com.sk89q.craftbook.mech.*;

/**
 * Configuration handler for CraftBook.
 * 
 * All fields are final because it is never appropriate to modify them during
 * operation, except for when the configuration is reloaded entirely, at which
 * point it is appropriate to construct an entirely new configuration instance
 * and update the plugin accordingly.
 * 
 * @author sk89q
 * @author hash
 */
public class MechanismsConfiguration {
    public MechanismsConfiguration(Configuration cfg, File dataFolder) {
        this.dataFolder = dataFolder;
        bookcaseSettings = new BookcaseSettings(cfg);
        bridgeSettings = new BridgeSettings(cfg);
        gateSettings = new GateSettings(cfg);
        elevatorSettings = new ElevatorSettings(cfg);
    }
    
    public final File dataFolder;
    public final BookcaseSettings bookcaseSettings;
    public final BridgeSettings bridgeSettings;
    public final GateSettings gateSettings;
    public final ElevatorSettings elevatorSettings;
    
    
    
    public class BookcaseSettings {
        public final boolean enable;
        public final String readLine;

        private BookcaseSettings(Configuration cfg) {
            enable      = cfg.getBoolean("bookshelf-enable",             true);
            readLine    = cfg.getString( "bookshelf-read-text",          "You pick up a book...");
        }
    }
    
    
    
    public class BridgeSettings {
        public final boolean enable;
        public final boolean enableRedstone;
        public final int maxLength;
        public final Set<Material> allowedBlocks;
        
        private BridgeSettings(Configuration cfg) {
            enable             = cfg.getBoolean("bridge-enable",             true);
            enableRedstone     = cfg.getBoolean("bridge-redstone",           true);
            maxLength          = cfg.getInt(    "bridge-max-length",         30);
            List<Integer> tids = cfg.getIntList("bridge-blocks",             Arrays.asList(4,5,20,43));
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

        private GateSettings(Configuration cfg) {
            enable             = cfg.getBoolean("gate-enable",             true);
            enableRedstone     = cfg.getBoolean("gate-redstone",           true);
        }
    }
    
    
    
    public class ElevatorSettings {
        public final boolean enable;

        private ElevatorSettings(Configuration cfg) {
            enable             = cfg.getBoolean("elevators-enable",             true);
        }
    }
}
