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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import com.sk89q.craftbook.mech.CustomDropManager;

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
public class MechanismsConfiguration extends BaseConfiguration{
    public MechanismsConfiguration(FileConfiguration cfg, File dataFolder) {
        super(cfg,dataFolder);
        this.dataFolder = dataFolder;

        mechSettings = new MechanismSettings(cfg);
        ammeterSettings = new AmmeterSettings(cfg);
        bookcaseSettings = new BookcaseSettings(cfg);
        bridgeSettings = new BridgeSettings(cfg);
        doorSettings = new DoorSettings(cfg);
        gateSettings = new GateSettings(cfg);
        elevatorSettings = new ElevatorSettings(cfg);
        teleporterSettings = new TeleporterSettings(cfg);
        cauldronSettings = new CauldronSettings(cfg);
        lightStoneSettings = new LightStoneSettings(cfg);
        lightSwitchSettings = new LightSwitchSettings(cfg);
        hiddenSwitchSettings = new HiddenSwitchSettings(cfg);
        snowSettings = new SnowSettings(cfg);
        areaSettings = new AreaSettings(cfg);
        commandSettings = new CommandSettings(cfg);
        customDrops = new CustomDropManager(dataFolder);
        customDropSettings = new CustomDropSettings(cfg);
        dispenserSettings = new DispenserSettings(cfg);
    }

    public final File dataFolder;
    public final MechanismSettings mechSettings;
    public final AmmeterSettings ammeterSettings;
    public final BookcaseSettings bookcaseSettings;
    public final BridgeSettings bridgeSettings;
    public final DoorSettings doorSettings;
    public final GateSettings gateSettings;
    public final ElevatorSettings elevatorSettings;
    public final TeleporterSettings teleporterSettings;
    public final CauldronSettings cauldronSettings;
    public final LightStoneSettings lightStoneSettings;
    public final LightSwitchSettings lightSwitchSettings;
    public final HiddenSwitchSettings hiddenSwitchSettings;
    public final SnowSettings snowSettings;
    public final AreaSettings areaSettings;
    public final CommandSettings commandSettings;
    public final CustomDropManager customDrops;
    public final CustomDropSettings customDropSettings;
    public final DispenserSettings dispenserSettings;

    //General settings
    public class MechanismSettings {
        public final boolean stopDestruction;

        private MechanismSettings(FileConfiguration cfg) {
            stopDestruction      = cfg.getBoolean("stop-mechanism-dupe",          true);
            cfg.set("stop-mechanism-dupe", stopDestruction);
        }
    }

    public class DispenserSettings {
        public final boolean enable;

        private DispenserSettings(FileConfiguration cfg) {
            enable      = cfg.getBoolean("dispenser-recipes-enable",          true);
            cfg.set("dispenser-recipes-enable", enable);
        }
    }

    public class BookcaseSettings {
        public final boolean enable;
        public final String readLine;

        private BookcaseSettings(FileConfiguration cfg) {
            enable      = cfg.getBoolean("bookshelf-enable",             true);
            readLine    = cfg.getString( "bookshelf-read-text",          "You pick up a book...");
            cfg.set("bookshelf-enable", enable);
            cfg.set("bookshelf-read-text", readLine);
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
            cfg.set("bridge-enable", enable);
            cfg.set("bridge-redstone", enableRedstone);
            cfg.set("bridge-max-length", maxLength);
            cfg.set("bridge-blocks", tids);
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
            cfg.set("door-enable", enable);
            cfg.set("door-redstone", enableRedstone);
            cfg.set("door-max-length", maxLength);
            cfg.set("door-blocks", tids);

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
        public final Set<Material> allowedBlocks;

        private GateSettings(FileConfiguration cfg) {
            enable             = cfg.getBoolean("gate-enable",             true);
            enableRedstone     = cfg.getBoolean("gate-redstone",           true);
            List<Integer> tids = cfg.getIntegerList("gate-blocks");
            if (tids == null || tids.isEmpty()) tids = Arrays.asList(85,101,102,113);
            Set<Material> allowedBlocks = new HashSet<Material>();
            for (Integer tid: tids) allowedBlocks.add(Material.getMaterial(tid));
            this.allowedBlocks = Collections.unmodifiableSet(allowedBlocks);
            cfg.set("gate-enable", enable);
            cfg.set("gate-redstone", enableRedstone);
            cfg.set("gate-blocks", tids);
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


    public class CommandSettings {
        public final boolean enable;

        private CommandSettings(FileConfiguration cfg) {
            enable             = cfg.getBoolean("command-sign-enable",             true);
            cfg.set("command-sign-enable", enable);
        }
    }


    public class ElevatorSettings {
        public final boolean enable;

        private ElevatorSettings(FileConfiguration cfg) {
            enable             = cfg.getBoolean("elevators-enable",        true);
            cfg.set("elevators-enable", enable);
        }
    }



    public class TeleporterSettings {
        public final boolean enable;

        private TeleporterSettings(FileConfiguration cfg) {
            enable             = cfg.getBoolean("teleporter-enable",        true);
            cfg.set("teleporter-enable", enable);
        }
    }


    public class CauldronSettings {
        public final boolean enable;
        public final int cauldronBlock;

        private CauldronSettings(FileConfiguration cfg) {
            enable             = cfg.getBoolean("cauldron-enable",         true);
            cauldronBlock      = cfg.getInt("cauldron-block",         1);
            cfg.set("cauldron-enable", enable);
            cfg.set("cauldron-block", cauldronBlock);
        }
        //FIXME the recipes should probably go here
    }



    public class LightSwitchSettings {
        public final boolean enable;

        private LightSwitchSettings(FileConfiguration cfg) {
            enable             = cfg.getBoolean("light-switch-enable",     true);
            cfg.set("light-switch-enable", enable);
        }
    }

    public class LightStoneSettings {
        public final boolean enable;

        private LightStoneSettings(FileConfiguration cfg) {
            enable             = cfg.getBoolean("light-stone-enable",      true);
            cfg.set("light-stone-enable", enable);
        }
    }
    public class AmmeterSettings {
        public final boolean enable;

        private AmmeterSettings(FileConfiguration cfg) {
            enable             = cfg.getBoolean("ammeter-enable",          true);
            cfg.set("ammeter-enable", enable);
        }
    }
    public class HiddenSwitchSettings {
        public final boolean enable;

        private HiddenSwitchSettings(FileConfiguration cfg) {
            enable             = cfg.getBoolean("hidden-switches-enable",  true);
            cfg.set("hidden-switches-enable", enable);
        }
    }
    public class SnowSettings {
        public final boolean enable;
        public final boolean trample;
        public final boolean placeSnow;
        public final boolean jumpTrample;

        private SnowSettings(FileConfiguration cfg) {
            enable              = cfg.getBoolean("snow-piling-enable",  true);
            trample             = cfg.getBoolean("snow-trample-enable",  true);
            placeSnow		= cfg.getBoolean("placable-snow", true);
            jumpTrample           = cfg.getBoolean("jump-trample-only", true);
            cfg.set("snow-piling-enable", enable);
            cfg.set("snow-trample-enable", trample);
            cfg.set("placable-snow", jumpTrample);
            cfg.set("jump-trample-only", jumpTrample);
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
            cfg.set("area-enable", enable);
            cfg.set("area-redstone", enableRedstone);
            cfg.set("max-areas-per-user", maxAreasPerUser);
            cfg.set("max-size-per-area", maxSizePerArea);
        }
    }

    public class CustomDropSettings {
        public final boolean requirePermissions;

        private CustomDropSettings(FileConfiguration cfg) {
            requirePermissions = cfg.getBoolean("custom-drops-require-permissions", false);
            cfg.set("custom-drops-require-permissions", requirePermissions);
        }
    }
}
