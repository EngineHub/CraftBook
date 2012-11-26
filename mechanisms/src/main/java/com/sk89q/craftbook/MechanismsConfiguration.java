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
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.sk89q.craftbook.mech.CustomDropManager;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;

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
    }

    @Override
    public void load() {

        aiSettings = new AISettings(new BaseConfigurationSection("AI Mechanics"));
        ammeterSettings = new AmmeterSettings(new BaseConfigurationSection("Ammeter"));
        anchorSettings = new AnchorSettings(new BaseConfigurationSection("Chunk Anchor"));
        areaSettings = new AreaSettings(new BaseConfigurationSection("Toggle Areas"));
        bookcaseSettings = new BookcaseSettings(new BaseConfigurationSection("Bookcase"));
        bridgeSettings = new BridgeSettings(new BaseConfigurationSection("Bridge"));
        cauldronSettings = new CauldronSettings(new BaseConfigurationSection("Cauldron"));
        chairSettings = new ChairSettings(new BaseConfigurationSection("Chairs"));
        commandSettings = new CommandSettings(new BaseConfigurationSection("Command Sign"));
        cookingPotSettings = new CookingPotSettings(new BaseConfigurationSection("Cooking Pot"));
        customCraftingSettings = new CustomCraftingSettings(new BaseConfigurationSection("Custom Crafting"));
        customDropSettings = new CustomDropSettings(new BaseConfigurationSection("Custom Drops"));
        dispenserSettings = new DispenserSettings(new BaseConfigurationSection("Dispenser Recipes"));
        doorSettings = new DoorSettings(new BaseConfigurationSection("Door"));
        //TODO elementalArrowSettings = new ElementalArrowsSettings(new BaseConfigurationSection("Elemental Arrows"));
        elevatorSettings = new ElevatorSettings(new BaseConfigurationSection("Elevator"));
        gateSettings = new GateSettings(new BaseConfigurationSection("Gate"));
        hiddenSwitchSettings = new HiddenSwitchSettings(new BaseConfigurationSection("Hidden Switch"));
        lightStoneSettings = new LightStoneSettings(new BaseConfigurationSection("Lightstone"));
        lightSwitchSettings = new LightSwitchSettings(new BaseConfigurationSection("Light Switch"));
        mapChangerSettings = new MapChangerSettings(new BaseConfigurationSection("Map Changer"));
        paintingSettings = new PaintingSettings(new BaseConfigurationSection("Painting Settings"));
        paymentSettings = new PaymentSettings(new BaseConfigurationSection("Payment Settings"));
        snowSettings = new SnowSettings(new BaseConfigurationSection("Snow"));
        teleporterSettings = new TeleporterSettings(new BaseConfigurationSection("Teleporter"));
        xpStorerSettings = new XPStorerSettings(new BaseConfigurationSection("XP Storer"));

        //Do this last, so it shows first.
        mechSettings = new MechanismSettings(new BaseConfigurationSection("Mechanisms"));

        //Non-config stuff
        customDrops = new CustomDropManager(dataFolder);
    }

    public class AISettings {

        public final boolean enabled;
        public final boolean zombieVision;
        public final boolean skeletonCriticals;

        private AISettings(BaseConfigurationSection section) {

            enabled = section.getBoolean("enable", true);
            zombieVision = section.getBoolean("realistic-zombie-vision", true);
            skeletonCriticals = section.getBoolean("skeleton-critical-shot", true);
        }
    }

    public class AmmeterSettings {

        public final boolean enable;
        public final int id;

        private AmmeterSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("enable", true);
            id = section.getInt("item-id", ItemID.COAL);
        }
    }

    public class AnchorSettings {

        public final boolean enable;

        private AnchorSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("enable", true);
        }
    }
    public class AreaSettings {

        public final boolean enable;
        public final boolean enableRedstone;
        public final int maxAreasPerUser;
        public final int maxSizePerArea;
        public final boolean useSchematics;

        private AreaSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("enable", true);
            enableRedstone = section.getBoolean("redstone", true);
            maxAreasPerUser = section.getInt("max-areas-per-user", 30);
            maxSizePerArea = section.getInt("max-size-per-area", 5000);
            useSchematics = section.getBoolean("use-schematic", true);
        }
    }
    public class BookcaseSettings {

        public final boolean enable;
        public final String readLine;

        private BookcaseSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("enable", true);
            readLine = section.getString("read-text", "You pick up a book...");
        }
    }
    public class BridgeSettings {

        public final boolean enable;
        public final boolean enableRedstone;
        public final int maxLength;
        public final int maxWidth;
        public final Set<Integer> allowedBlocks;

        private BridgeSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("enable", true);
            enableRedstone = section.getBoolean("redstone", true);
            maxLength = section.getInt("max-length", 30);
            maxWidth = section.getInt("max-width", 5);
            allowedBlocks = section.getIntegerSet("blocks", Arrays.asList(4, 5, 20, 43));
        }

        /**
         * @param b
         *
         * @return true if the given block type can be used for a bridge; false
         *         otherwise.
         */
        public boolean canUseBlock(Integer b) {

            return allowedBlocks.contains(b);
        }
    }
    public class CauldronSettings {

        public final boolean enable;
        public final int cauldronBlock;
        public final boolean enableNew;
        public final boolean newSpoons;

        private CauldronSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("legacy-enable", false);
            cauldronBlock = section.getInt("legacy-block", 1);
            enableNew = section.getBoolean("new-enable", true);
            newSpoons = section.getBoolean("new-spoons", true);
        }
    }
    public class ChairSettings {

        public final boolean enable;
        public final boolean requireSneak;
        public final Set<Integer> allowedBlocks;
        public BiMap<String, Block> chairs = HashBiMap.create(new HashMap<String, Block>());

        private ChairSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("enable", true);
            requireSneak = section.getBoolean("sneaking", true);
            allowedBlocks = section.getIntegerSet("blocks", Arrays.asList(53, 67, 108, 109, 114, 128, 134, 135,
                    136));
        }

        /**
         * @param b
         *
         * @return true if the given block type can be used for a bridge; false
         *         otherwise.
         */
        public boolean canUseBlock(Integer b) {

            return allowedBlocks.contains(b);
        }
    }
    public class CommandSettings {

        public final boolean enable;

        private CommandSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("enable", true);
        }
    }
    public class CookingPotSettings {

        public final boolean enable;
        public final boolean requiresfuel;
        public final boolean cooksOres;
        public final boolean openClick;

        private CookingPotSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("enable", true);
            requiresfuel = section.getBoolean("requires-fuel", false);
            cooksOres = section.getBoolean("cooks-everything", false);
            openClick = section.getBoolean("open-on-click", false);
        }
    }
    public class CustomCraftingSettings {

        public final boolean enable;

        private CustomCraftingSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("enable", true);
        }
    }
    public class CustomDropSettings {

        public final boolean enable;
        public final boolean requirePermissions;

        private CustomDropSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("enable", true);
            requirePermissions = section.getBoolean("require-permissions", false);
        }
    }
    public class DispenserSettings {

        public final boolean enable;

        private DispenserSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("enable", true);
        }
    }
    public class DoorSettings {

        public final boolean enable;
        public final boolean enableRedstone;
        public final int maxLength;
        public final int maxWidth;
        public final Set<Integer> allowedBlocks;

        private DoorSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("enable", true);
            enableRedstone = section.getBoolean("redstone", true);
            maxLength = section.getInt("max-length", 30);
            maxWidth = section.getInt("max-width", 5);
            allowedBlocks = section.getIntegerSet("blocks", Arrays.asList(4, 5, 20, 43));
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
    public class ElementalArrowsSettings {

        public final boolean enable;

        private ElementalArrowsSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("enable", true);
        }
    }
    public class ElevatorSettings {

        public final boolean enable;
        public final boolean loop;
        public final boolean buttons;

        private ElevatorSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("enable", true);
            loop = section.getBoolean("loop-top-bottom", false);
            buttons = section.getBoolean("allow-button-on-back", true);
        }
    }
    public class GateSettings {

        public final boolean enable;
        public final boolean enableRedstone;
        public final boolean limitColumns;
        public final int maxColumns;
        public final Set<Integer> allowedBlocks;

        private GateSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("enable", true);
            enableRedstone = section.getBoolean("redstone", true);
            limitColumns = section.getBoolean("limit-columns", true);
            maxColumns = section.getInt("max-columns", 14);
            allowedBlocks = section.getIntegerSet("blocks", Arrays.asList(85, 101, 102, 113));
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
    public class HiddenSwitchSettings {

        public final boolean enable;
        public final boolean anyside;

        private HiddenSwitchSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("enable", true);
            anyside = section.getBoolean("any-side", true);
        }
    }
    public class LightStoneSettings {

        public final boolean enable;
        public final int id;

        private LightStoneSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("enable", true);
            id = section.getInt("item-id", ItemID.LIGHTSTONE_DUST);
        }
    }
    public class LightSwitchSettings {

        public final boolean enable;
        public final int maxRange;
        public final int maxMaximum;

        private LightSwitchSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("enable", true);
            maxRange = section.getInt("max-range", 10);
            maxMaximum = section.getInt("max-lights", 20);
        }
    }
    public class MapChangerSettings {

        public final boolean enabled;

        private MapChangerSettings(BaseConfigurationSection section) {

            enabled = section.getBoolean("enable", true);
        }
    }
    public class PaintingSettings {

        public final boolean enabled;

        private PaintingSettings(BaseConfigurationSection section) {

            enabled = section.getBoolean("enable", true);
        }
    }
    public class PaymentSettings {

        public final boolean enabled;

        private PaymentSettings(BaseConfigurationSection section) {

            enabled = section.getBoolean("enable", true);
        }
    }
    public class SnowSettings {

        public final boolean enable;
        public final boolean trample;
        public final boolean placeSnow;
        public final boolean jumpTrample;
        public final boolean piling;
        public final boolean realistic;

        private SnowSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("piling-enable", true);
            trample = section.getBoolean("trample-enable", true);
            placeSnow = section.getBoolean("placable-snow", true);
            jumpTrample = section.getBoolean("jump-trample-only", true);
            piling = section.getBoolean("pile-high", false);
            realistic = section.getBoolean("realistic-piling", false);
        }
    }
    public class TeleporterSettings {

        public final boolean enable;
        public final int maxrange;
        public final boolean requiresign;

        private TeleporterSettings(BaseConfigurationSection section) {

            enable = section.getBoolean("enable", true);
            maxrange = section.getInt("max-range", -1);
            requiresign = section.getBoolean("need-sign-destination", false);

        }
    }
    public class XPStorerSettings {

        public final boolean enabled;
        public final int material;

        private XPStorerSettings(BaseConfigurationSection section) {

            enabled = section.getBoolean("enable", true);
            material = section.getInt("block", BlockID.MOB_SPAWNER);
        }
    }
    //General settings
    public class MechanismSettings {

        public final boolean stopDestruction;

        private MechanismSettings(BaseConfigurationSection section) {

            stopDestruction = section.getBoolean("stop-mechanism-dupe", true);
        }
    }

    public MechanismSettings mechSettings;

    public AmmeterSettings ammeterSettings;

    public BookcaseSettings bookcaseSettings;

    public BridgeSettings bridgeSettings;

    public DoorSettings doorSettings;

    public GateSettings gateSettings;

    public ElementalArrowsSettings elementalArrowSettings;

    public ElevatorSettings elevatorSettings;

    public TeleporterSettings teleporterSettings;

    public CauldronSettings cauldronSettings;

    public LightStoneSettings lightStoneSettings;

    public LightSwitchSettings lightSwitchSettings;

    public HiddenSwitchSettings hiddenSwitchSettings;

    public SnowSettings snowSettings;

    public AreaSettings areaSettings;

    public CommandSettings commandSettings;

    public CustomDropManager customDrops;

    public CustomDropSettings customDropSettings;

    public DispenserSettings dispenserSettings;

    public ChairSettings chairSettings;

    public AISettings aiSettings;

    public AnchorSettings anchorSettings;

    public CookingPotSettings cookingPotSettings;

    public CustomCraftingSettings customCraftingSettings;

    public PaintingSettings paintingSettings;

    public PaymentSettings paymentSettings;

    public XPStorerSettings xpStorerSettings;

    public MapChangerSettings mapChangerSettings;
}
