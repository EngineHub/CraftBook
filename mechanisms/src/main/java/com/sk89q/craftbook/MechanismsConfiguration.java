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

        mechSettings = new MechanismSettings();
        ammeterSettings = new AmmeterSettings();
        bookcaseSettings = new BookcaseSettings();
        bridgeSettings = new BridgeSettings();
        doorSettings = new DoorSettings();
        gateSettings = new GateSettings();
        elevatorSettings = new ElevatorSettings();
        teleporterSettings = new TeleporterSettings();
        cauldronSettings = new CauldronSettings();
        lightStoneSettings = new LightStoneSettings();
        lightSwitchSettings = new LightSwitchSettings();
        hiddenSwitchSettings = new HiddenSwitchSettings();
        snowSettings = new SnowSettings();
        areaSettings = new AreaSettings();
        commandSettings = new CommandSettings();
        customDrops = new CustomDropManager(dataFolder);
        customDropSettings = new CustomDropSettings();
        dispenserSettings = new DispenserSettings();
        chairSettings = new ChairSettings();
        aiSettings = new AISettings();
        anchorSettings = new AnchorSettings();
        cookingPotSettings = new CookingPotSettings();
        customCraftingSettings = new CustomCraftingSettings();
        paintingSettings = new PaintingSettings();
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
    public final PaintingSettings paintingSettings;

    //General settings
    public class MechanismSettings {

        public final boolean stopDestruction;

        private MechanismSettings() {

            stopDestruction = getBoolean("stop-mechanism-dupe", false);
        }
    }

    public class DispenserSettings {

        public final boolean enable;

        private DispenserSettings() {

            enable = getBoolean("dispenser-recipes-enable", true);
        }
    }

    public class AnchorSettings {

        public final boolean enable;

        private AnchorSettings() {

            enable = getBoolean("chunk-anchor-enable", true);
        }
    }

    public class CustomCraftingSettings {

        public final boolean enable;

        private CustomCraftingSettings() {

            enable = getBoolean("custom-crafting-enable", true);
        }
    }

    public class CookingPotSettings {

        public final boolean enable;

        private CookingPotSettings() {

            enable = getBoolean("cooking-pot-enable", true);
        }
    }

    public class BookcaseSettings {

        public final boolean enable;
        public final String readLine;

        private BookcaseSettings() {

            enable = getBoolean("bookshelf-enable", true);
            readLine = getString("bookshelf-read-text", "You pick up a book...");
        }
    }

    public class BridgeSettings {

        public final boolean enable;
        public final boolean enableRedstone;
        public final int maxLength;
        public final int maxWidth;
        public final Set<Material> allowedBlocks;

        private BridgeSettings() {

            enable = getBoolean("bridge-enable", true);
            enableRedstone = getBoolean("bridge-redstone", true);
            maxLength = getInt("bridge-max-length", 30);
            maxWidth = getInt("bridge-max-width", 5);
            allowedBlocks = getMaterialSet("bridge-blocks", Arrays.asList(4, 5, 20, 43));
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

        private DoorSettings() {

            enable = getBoolean("door-enable", true);
            enableRedstone = getBoolean("door-redstone", true);
            maxLength = getInt("door-max-length", 30);
            maxWidth = getInt("door-max-width", 5);
            allowedBlocks = getIntegerSet("door-blocks", Arrays.asList(4, 5, 20, 43));
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

        private GateSettings() {

            enable = getBoolean("gate-enable", true);
            enableRedstone = getBoolean("gate-redstone", true);
            allowedBlocks = getIntegerSet("gate-blocks", Arrays.asList(85, 101, 102, 113));
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

        private CommandSettings() {

            enable = getBoolean("command-sign-enable", true);
        }
    }

    public class ElevatorSettings {

        public final boolean enable;

        private ElevatorSettings() {

            enable = getBoolean("elevators-enable", true);
        }
    }

    public class TeleporterSettings {

        public final boolean enable;

        private TeleporterSettings() {

            enable = getBoolean("teleporter-enable", true);
        }
    }

    public class CauldronSettings {

        public final boolean enable;
        public final int cauldronBlock;
        public final boolean enableNew;
        public final boolean newSpoons;

        private CauldronSettings() {

            enable = getBoolean("cauldron-enable", false);
            cauldronBlock = getInt("cauldron-block", 1);
            enableNew = getBoolean("new-cauldron-enable", true);
            newSpoons = getBoolean("new-cauldron-spoons", true);
        }
    }

    public class LightSwitchSettings {

        public final boolean enable;
        public final int maxRange;
        public final int maxMaximum;

        private LightSwitchSettings() {

            enable = getBoolean("light-switch-enable", true);
            maxRange = getInt("light-switch-max-range", 10);
            maxMaximum = getInt("light-switch-max-lights", 20);
        }
    }

    public class LightStoneSettings {

        public final boolean enable;

        private LightStoneSettings() {

            enable = getBoolean("light-stone-enable", true);
        }
    }

    public class AmmeterSettings {

        public final boolean enable;

        private AmmeterSettings() {

            enable = getBoolean("ammeter-enable", true);
        }
    }

    public class HiddenSwitchSettings {

        public final boolean enable;

        private HiddenSwitchSettings() {

            enable = getBoolean("hidden-switches-enable", true);
        }
    }

    public class SnowSettings {

        public final boolean enable;
        public final boolean trample;
        public final boolean placeSnow;
        public final boolean jumpTrample;
        public final boolean piling;

        private SnowSettings() {

            enable = getBoolean("snow-piling-enable", true);
            trample = getBoolean("snow-trample-enable", true);
            placeSnow = getBoolean("placable-snow", true);
            jumpTrample = getBoolean("jump-trample-only", true);
            piling = getBoolean("snow-piles-high", false);
        }
    }

    public class AreaSettings {

        public final boolean enable;
        public final boolean enableRedstone;
        public final int maxAreasPerUser;
        public final int maxSizePerArea;
        public final boolean useSchematics;

        private AreaSettings() {

            enable = getBoolean("area-enable", true);
            enableRedstone = getBoolean("area-redstone", true);
            maxAreasPerUser = getInt("max-areas-per-user", 30);
            maxSizePerArea = getInt("max-size-per-area", 5000);
            useSchematics = getBoolean("area-use-schematic", true);
        }
    }

    public class CustomDropSettings {

        public final boolean enable;
        public final boolean requirePermissions;

        private CustomDropSettings() {

            enable = getBoolean("custom-drops-enable", true);
            requirePermissions = getBoolean("custom-drops-require-permissions", false);
        }
    }

    public class ChairSettings {

        public final boolean enable;
        public final boolean requireSneak;
        public final Set<Material> allowedBlocks;
        public Map<String, Block> chairs = new HashMap<String, Block>();

        private ChairSettings() {

            enable = getBoolean("chair-enable", true);
            requireSneak = getBoolean("chair-sneaking", true);
            allowedBlocks = getMaterialSet("chair-blocks", Arrays.asList(53, 67, 108, 109, 114, 128, 134, 135,
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

        private AISettings() {

            enabled = getBoolean("ai-mechanic-enable", true);
            zombieVision = getBoolean("realistic-zombie-vision", true);
        }
    }

    public class PaintingSettings {

        public final boolean enabled;

        private PaintingSettings() {

            enabled = getBoolean("painting-switch-enable", true);
        }
    }
}