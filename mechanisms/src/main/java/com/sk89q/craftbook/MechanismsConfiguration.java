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
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        chairSettings = new ChairSettings(cfg);
        aiSettings = new AISettings(cfg);
        anchorSettings = new AnchorSettings(cfg);
        cookingPotSettings = new CookingPotSettings(cfg);
        customCraftingSettings = new CustomCraftingSettings(cfg);
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
    public final ChairSettings chairSettings;
    public final AISettings aiSettings;
    public final AnchorSettings anchorSettings;
    public final CookingPotSettings cookingPotSettings;
    public final CustomCraftingSettings customCraftingSettings;

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

    public class AnchorSettings {

        public final boolean enable;

        private AnchorSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "chunk-anchor-enable", true);
        }
    }

    public class CustomCraftingSettings {

        public final boolean enable;

        private CustomCraftingSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "custom-crafting-enable", true);
        }
    }

    public class CookingPotSettings {

        public final boolean enable;

        private CookingPotSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "cooking-pot-enable", true);
        }
    }

    public class BookcaseSettings {

        public final boolean enable;
        public final String readLine;

        private BookcaseSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "bookshelf-enable", true);
            readLine = getString(cfg, "bookshelf-read-text", "You pick up a book...");
        }
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
            allowedBlocks = getMaterialSet(cfg, "bridge-blocks", Arrays.asList(4, 5, 20, 43));
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
        public final Set<Integer> allowedBlocks;

        private DoorSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "door-enable", true);
            enableRedstone = getBoolean(cfg, "door-redstone", true);
            maxLength = getInt(cfg, "door-max-length", 30);
            maxWidth = getInt(cfg, "door-max-width", 5);
            allowedBlocks = getIntegerSet(cfg, "door-blocks", Arrays.asList(4, 5, 20, 43));
        }

        /**
         * @param b
         *
         * @return true if the given block type can be used for a bridge; false
         *         otherwise.
         */
        public boolean canUseBlock(int b) {

            return allowedBlocks.contains(b);
        }
    }

    public class GateSettings {

        public final boolean enable;
        public final boolean enableRedstone;
        public final Set<Integer> allowedBlocks;

        private GateSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "gate-enable", true);
            enableRedstone = getBoolean(cfg, "gate-redstone", true);
            allowedBlocks = getIntegerSet(cfg, "gate-blocks", Arrays.asList(85, 101, 102, 113));
        }

        /**
         * @param b
         *
         * @return true if the given block type can be used for a bridge; false
         *         otherwise.
         */
        public boolean canUseBlock(int b) {

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
        public final boolean enableNew;
        public final boolean newSpoons;

        private CauldronSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "cauldron-enable", false);
            cauldronBlock = getInt(cfg, "cauldron-block", 1);
            enableNew = getBoolean(cfg, "new-cauldron-enable", true);
            newSpoons = getBoolean(cfg, "new-cauldron-spoons", true);
        }
        //TODO the recipes should probably go here
    }

    public class LightSwitchSettings {

        public final boolean enable;
        public final int maxRange;
        public final int maxMaximum;

        private LightSwitchSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "light-switch-enable", true);
            maxRange = getInt(cfg, "light-switch-max-range", 10);
            maxMaximum = getInt(cfg, "light-switch-max-lights", 20);
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
        public final boolean useSchematics;

        private AreaSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "area-enable", true);
            enableRedstone = getBoolean(cfg, "area-redstone", true);
            maxAreasPerUser = getInt(cfg, "max-areas-per-user", 30);
            maxSizePerArea = getInt(cfg, "max-size-per-area", 5000);
            useSchematics = getBoolean(cfg, "area-use-schematic", false);
        }
    }

    public class CustomDropSettings {

        public final boolean enable;
        public final boolean requirePermissions;

        private CustomDropSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "custom-drops-enable", true);
            requirePermissions = getBoolean(cfg, "custom-drops-require-permissions", false);
        }
    }

    public class ChairSettings {

        public final boolean enable;
        public final boolean requireSneak;
        public final Set<Material> allowedBlocks;
        public Map<String, Block> chairs = new HashMap<String, Block>();

        private ChairSettings(FileConfiguration cfg) {

            enable = getBoolean(cfg, "chair-enable", true);
            requireSneak = getBoolean(cfg, "chair-sneaking", true);
            allowedBlocks = getMaterialSet(cfg, "chair-blocks", Arrays.asList(53, 67, 108, 109, 114, 128, 134, 135,
                    136));
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

    public class AISettings {

        public final boolean enabled;
        public final boolean zombieVision;

        private AISettings(FileConfiguration cfg) {

            enabled = getBoolean(cfg, "ai-mechanic-enable", true);
            zombieVision = getBoolean(cfg, "realistic-zombie-vision", true);
        }
    }
}