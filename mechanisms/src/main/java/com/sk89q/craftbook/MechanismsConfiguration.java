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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import com.sk89q.craftbook.mech.CustomDropManager;

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
        ammeterSettings = new AmmeterSettings(new BaseConfigurationSection("Ammeter"));
        bookcaseSettings = new BookcaseSettings(new BaseConfigurationSection("Bookcase"));
        bridgeSettings = new BridgeSettings(new BaseConfigurationSection("Bridge"));
        doorSettings = new DoorSettings(new BaseConfigurationSection("Door"));
        gateSettings = new GateSettings(new BaseConfigurationSection("Gate"));
        elevatorSettings = new ElevatorSettings(new BaseConfigurationSection("Elevator"));
        teleporterSettings = new TeleporterSettings(new BaseConfigurationSection("Teleporter"));
        cauldronSettings = new CauldronSettings(new BaseConfigurationSection("Cauldron"));
        lightStoneSettings = new LightStoneSettings(new BaseConfigurationSection("Lightstone"));
        lightSwitchSettings = new LightSwitchSettings(new BaseConfigurationSection("Light Switch"));
        hiddenSwitchSettings = new HiddenSwitchSettings(new BaseConfigurationSection("Hidden Switch"));
        snowSettings = new SnowSettings(new BaseConfigurationSection("Snow"));
        areaSettings = new AreaSettings(new BaseConfigurationSection("Areas"));
        commandSettings = new CommandSettings(new BaseConfigurationSection("Command Sign"));
        customDrops = new CustomDropManager(dataFolder);
        customDropSettings = new CustomDropSettings(new BaseConfigurationSection("Custom Drops"));
        dispenserSettings = new DispenserSettings(new BaseConfigurationSection("Dispenser"));
        chairSettings = new ChairSettings(new BaseConfigurationSection("Chairs"));
        aiSettings = new AISettings(new BaseConfigurationSection("AI"));
        anchorSettings = new AnchorSettings(new BaseConfigurationSection("Chunk Anchor"));
        cookingPotSettings = new CookingPotSettings(new BaseConfigurationSection("Cooking Pot"));
        customCraftingSettings = new CustomCraftingSettings(new BaseConfigurationSection("Custom Crafting"));
        paintingSettings = new PaintingSettings(new BaseConfigurationSection("Painting Settings"));
        xpStorerSettings = new XPStorerSettings(new BaseConfigurationSection("XP Storer"));
        mapChangerSettings = new MapChangerSettings(new BaseConfigurationSection("Map Changer"));
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
    public final XPStorerSettings xpStorerSettings;
    public final MapChangerSettings mapChangerSettings;

    //General settings
    public class MechanismSettings {

        public final boolean stopDestruction;

        private MechanismSettings() {

            stopDestruction = getBoolean("stop-mechanism-dupe", false);
        }
    }

    public class DispenserSettings {

        public final boolean enable;

        private DispenserSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("dispenser-recipes-enable", true);
        }
    }

    public class AnchorSettings {

        public final boolean enable;

        private AnchorSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("chunk-anchor-enable", true);
        }
    }

    public class CustomCraftingSettings {

        public final boolean enable;

        private CustomCraftingSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("custom-crafting-enable", true);
        }
    }

    public class CookingPotSettings {

        public final boolean enable;

        private CookingPotSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("cooking-pot-enable", true);
        }
    }

    public class BookcaseSettings {

        public final boolean enable;
        public final String readLine;

        private BookcaseSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("bookshelf-enable", true);
            readLine = section.getString("bookshelf-read-text", "You pick up a book...");
        }
    }

    public class BridgeSettings {

        public final boolean enable;
        public final boolean enableRedstone;
        public final int maxLength;
        public final int maxWidth;
        public final Set<Material> allowedBlocks;

        private BridgeSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("bridge-enable", true);
            enableRedstone = section.getBoolean("bridge-redstone", true);
            maxLength = section.getInt("bridge-max-length", 30);
            maxWidth = section.getInt("bridge-max-width", 5);
            allowedBlocks = section.getMaterialSet("bridge-blocks", Arrays.asList(4, 5, 20, 43));
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

        private DoorSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("door-enable", true);
            enableRedstone = section.getBoolean("door-redstone", true);
            maxLength = section.getInt("door-max-length", 30);
            maxWidth = section.getInt("door-max-width", 5);
            allowedBlocks = section.getIntegerSet("door-blocks", Arrays.asList(4, 5, 20, 43));
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

        private GateSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("gate-enable", true);
            enableRedstone = section.getBoolean("gate-redstone", true);
            allowedBlocks = section.getIntegerSet("gate-blocks", Arrays.asList(85, 101, 102, 113));
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

        private CommandSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("command-sign-enable", true);
        }
    }

    public class ElevatorSettings {

        public final boolean enable;
        public final boolean loop;

        private ElevatorSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("elevators-enable", true);
            loop = section.getBoolean("elevators-loop-top-bottom", false);
        }
    }

    public class TeleporterSettings {

        public final boolean enable;

        private TeleporterSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("teleporter-enable", true);
        }
    }

    public class CauldronSettings {

        public final boolean enable;
        public final int cauldronBlock;
        public final boolean enableNew;
        public final boolean newSpoons;

        private CauldronSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("cauldron-enable", false);
            cauldronBlock = section.getInt("cauldron-block", 1);
            enableNew = section.getBoolean("new-cauldron-enable", true);
            newSpoons = section.getBoolean("new-cauldron-spoons", true);
        }
    }

    public class LightSwitchSettings {

        public final boolean enable;
        public final int maxRange;
        public final int maxMaximum;

        private LightSwitchSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("light-switch-enable", true);
            maxRange = section.getInt("light-switch-max-range", 10);
            maxMaximum = section.getInt("light-switch-max-lights", 20);
        }
    }

    public class LightStoneSettings {

        public final boolean enable;

        private LightStoneSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("light-stone-enable", true);
        }
    }

    public class AmmeterSettings {

        public final boolean enable;

        private AmmeterSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("ammeter-enable", true);
        }
    }

    public class HiddenSwitchSettings {

        public final boolean enable;

        private HiddenSwitchSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("hidden-switches-enable", true);
        }
    }

    public class SnowSettings {

        public final boolean enable;
        public final boolean trample;
        public final boolean placeSnow;
        public final boolean jumpTrample;
        public final boolean piling;

        private SnowSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("snow-piling-enable", true);
            trample = section.getBoolean("snow-trample-enable", true);
            placeSnow = section.getBoolean("placable-snow", true);
            jumpTrample = section.getBoolean("jump-trample-only", true);
            piling = section.getBoolean("snow-piles-high", false);
        }
    }

    public class AreaSettings {

        public final boolean enable;
        public final boolean enableRedstone;
        public final int maxAreasPerUser;
        public final int maxSizePerArea;
        public final boolean useSchematics;

        private AreaSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("area-enable", true);
            enableRedstone = section.getBoolean("area-redstone", true);
            maxAreasPerUser = section.getInt("max-areas-per-user", 30);
            maxSizePerArea = section.getInt("max-size-per-area", 5000);
            useSchematics = section.getBoolean("area-use-schematic", true);
        }
    }

    public class CustomDropSettings {

        public final boolean enable;
        public final boolean requirePermissions;

        private CustomDropSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("custom-drops-enable", true);
            requirePermissions = section.getBoolean("custom-drops-require-permissions", false);
        }
    }

    public class ChairSettings {

        public final boolean enable;
        public final boolean requireSneak;
        public final Set<Material> allowedBlocks;
        public Map<String, Block> chairs = new HashMap<String, Block>();

        private ChairSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("chair-enable", true);
            requireSneak = section.getBoolean("chair-sneaking", true);
            allowedBlocks = section.getMaterialSet("chair-blocks", Arrays.asList(53, 67, 108, 109, 114, 128, 134, 135,
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

        private AISettings(BaseConfigurationSection section) {

            enabled = section.getBoolean("ai-mechanic-enable", true);
            zombieVision = section.getBoolean("realistic-zombie-vision", true);
        }
    }

    public class PaintingSettings {

        public final boolean enabled;

        private PaintingSettings(BaseConfigurationSection section) {

            enabled = section.getBoolean("painting-switch-enable", true);
        }
    }

    public class XPStorerSettings {

        public final boolean enabled;

        private XPStorerSettings(BaseConfigurationSection section) {

            enabled = section.getBoolean("xp-storer-enable", true);
        }
    }

    public class MapChangerSettings {

        public final boolean enabled;

        private MapChangerSettings(BaseConfigurationSection section) {

            enabled = section.getBoolean("map-changer-enable", true);
        }
    }
}