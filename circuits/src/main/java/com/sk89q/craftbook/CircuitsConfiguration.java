// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import com.sk89q.worldedit.blocks.BlockID;

/**
 * Configuration handler for CraftBook.
 * 
 * @author sk89q
 */
public class CircuitsConfiguration extends BaseConfiguration {

    public CircuitsConfiguration (FileConfiguration cfg, File dataFolder) {

        super(cfg, dataFolder);
    }

    public boolean enableNetherstone;
    public boolean enablePumpkins;
    public boolean enableGlowStone;
    public int glowstoneOffBlock;
    public PipeSettings pipeSettings;
    public ICSettings icSettings;

    @Override
    public void load () {

        enableNetherstone = getBoolean("redstone-netherstone", false);
        enablePumpkins = getBoolean("redstone-pumpkins", true);
        enableGlowStone = getBoolean("redstone-glowstone", false);
        glowstoneOffBlock = getInt("glowstone-off-material", BlockID.GLASS);
        icSettings = new ICSettings(new BaseConfiguration.BaseConfigurationSection("redstone-ics"));
        pipeSettings = new PipeSettings(new BaseConfiguration.BaseConfigurationSection("Pipes"));
    }

    public class ICSettings {

        public final boolean cache;
        public final boolean enabled;
        public final boolean shorthand;
        public final List<String> disabledICs;

        private ICSettings (BaseConfigurationSection section) {

            enabled = section.getBoolean("enable", true);
            cache = section.getBoolean("cache", true);
            shorthand = getBoolean("enable-shorthand", false);
            disabledICs = section.getStringList("disabled-ics", new ArrayList<String>());
        }
    }

    public class PipeSettings {

        public final boolean enabled;
        public final boolean diagonals;
        public final int insulator;

        private PipeSettings (BaseConfigurationSection section) {

            enabled = section.getBoolean("enable", true);
            diagonals = section.getBoolean("allow-diagonal", true);
            insulator = section.getInt("insulator-block", BlockID.CLOTH);
        }
    }
}
