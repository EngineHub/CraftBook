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

import com.sk89q.craftbook.mech.CustomDropManager;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.*;

/**
 * FileConfiguration handler for CraftBook.
 * All fields are final because it is never appropriate to modify them during
 * operation, except for when the FileConfiguration is reloaded entirely, at which
 * point it is appropriate to construct an entirely new FileConfiguration instance
 * and update the plugin accordingly.
 *
 * @author sk89q
 * @author hash
 * @author Me4502
 */
public class MechanismsConfiguration extends BaseConfiguration {

    public MechanismsConfiguration(FileConfiguration cfg, File dataFolder) {

        super(cfg, dataFolder);
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

            stopDestruction = getBoolean(cfg, "stop-mechanism-dupe", false);
        }
    }

    public class DispenserSettings {

        public final boolean enable;

        private DispenserSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "dispenser-recipes-enable", true);
        }
    }

    public class BookcaseSettings {

        public final boolean enable;
        public final String readLine;

        private BookcaseSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "bookshelf-enable", true);
            readLine = getString(cfg, "bookshelf-read-text", "You pick up a book...");
        }
        //FIXME the books file should probably be cached here too
    }

    public class BridgeSettings {

        public final boolean enable;
        public final boolean enableRedstone;
        public final int maxLength;
        public final int maxWidth;
        public final Set<Material> allowedBlocks;

        private BridgeSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "bridge-enable", true);
            enableRedstone = getBoolean(cfg, "bridge-redstone", true);
            maxLength = getInt(cfg, "bridge-max-length", 30);
            maxWidth = getInt(cfg, "bridge-max-width", 5);
            List<Integer> tids = cfg.getIntegerList("bridge-blocks");
            if (tids == null) tids = Arrays.asList(4, 5, 20, 43);
            Set<Material> allowedBlocks = new HashSet<Material>();
            for (Integer tid : tids) allowedBlocks.add(Material.getMaterial(tid));
            this.allowedBlocks = Collections.unmodifiableSet(allowedBlocks);
            cfg.set("bridge-blocks", tids);
        }

        /**
         * @param b
         *
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
        public final int maxWidth;
        public final Set<Material> allowedBlocks;

        private DoorSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "door-enable", true);
            enableRedstone = getBoolean(cfg, "door-redstone", true);
            maxLength = getInt(cfg, "door-max-length", 30);
            maxWidth = getInt(cfg, "door-max-width", 5);
            List<Integer> tids = cfg.getIntegerList("door-blocks");
            if (tids == null) Arrays.asList(4, 5, 20, 43);
            Set<Material> allowedBlocks = new HashSet<Material>();
            if (tids != null) {
                for (Integer tid : tids) allowedBlocks.add(Material.getMaterial(tid));
            }
            this.allowedBlocks = Collections.unmodifiableSet(allowedBlocks);
            cfg.set("door-blocks", tids);

        }

        /**
         * @param b
         *
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

            enable = getBoolean(cfg, "gate-enable", true);
            enableRedstone = getBoolean(cfg, "gate-redstone", true);
            List<Integer> tids = cfg.getIntegerList("gate-blocks");
            if (tids == null || tids.isEmpty() || tids.size() < 1) tids = Arrays.asList(85, 101, 102, 113);
            Set<Material> allowedBlocks = new HashSet<Material>();
            for (Integer tid : tids) allowedBlocks.add(Material.getMaterial(tid));
            this.allowedBlocks = Collections.unmodifiableSet(allowedBlocks);
            cfg.set("gate-blocks", tids);
        }

        /**
         * @param b
         *
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

            enable = getBoolean(cfg, "command-sign-enable", true);
        }
    }

    public class ElevatorSettings {

        public final boolean enable;

        private ElevatorSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "elevators-enable", true);
        }
    }

    public class TeleporterSettings {

        public final boolean enable;

        private TeleporterSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "teleporter-enable", true);
        }
    }

    public class CauldronSettings {

        public final boolean enable;
        public final int cauldronBlock;

        private CauldronSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "cauldron-enable", true);
            cauldronBlock = getInt(cfg, "cauldron-block", 1);
        }
        //FIXME the recipes should probably go here
    }

    public class LightSwitchSettings {

        public final boolean enable;

        private LightSwitchSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "light-switch-enable", true);
        }
    }

    public class LightStoneSettings {

        public final boolean enable;

        private LightStoneSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "light-stone-enable", true);
        }
    }

    public class AmmeterSettings {

        public final boolean enable;

        private AmmeterSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "ammeter-enable", true);
        }
    }

    public class HiddenSwitchSettings {

        public final boolean enable;

        private HiddenSwitchSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "hidden-switches-enable", true);
        }
    }

    public class SnowSettings {

        public final boolean enable;
        public final boolean trample;
        public final boolean placeSnow;
        public final boolean jumpTrample;
        public final boolean piling;

        private SnowSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "snow-piling-enable", true);
            trample = getBoolean(cfg, "snow-trample-enable", true);
            placeSnow = getBoolean(cfg, "placable-snow", true);
            jumpTrample = getBoolean(cfg, "jump-trample-only", true);
            piling = getBoolean(cfg, "snow-piles-high", false);
        }
    }

    public class AreaSettings {

        public final boolean enable;
        public final boolean enableRedstone;
        public final int maxAreasPerUser;
        public final int maxSizePerArea;

        private AreaSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "area-enable", true);
            enableRedstone = getBoolean(cfg, "area-redstone", true);
            maxAreasPerUser = getInt(cfg, "max-areas-per-user", 30);
            maxSizePerArea = getInt(cfg, "max-size-per-area", 5000);
        }
    }

    public class CustomDropSettings {

        public final boolean requirePermissions;

        private CustomDropSettings(FileConfiguration cfg) {

            requirePermissions = getBoolean(cfg, "custom-drops-require-permissions", false);
        }
    }
}
