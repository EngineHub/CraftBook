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

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

/**
 * Configuration handler for CraftBook.
 *
 * @author sk89q
 */
public class CircuitsConfiguration extends BaseConfiguration {

    public CircuitsConfiguration(FileConfiguration cfg, File dataFolder) {

        super(cfg, dataFolder);
        this.dataFolder = dataFolder;

        enableNetherstone = getBoolean(cfg, "redstone-netherstone", false);
        enablePumpkins = getBoolean(cfg, "redstone-pumpkins", true);
        enableICs = getBoolean(cfg, "redstone-ics", true);
        enableGlowStone = getBoolean(cfg, "redstone-glowstone", false);
    }

    public final File dataFolder;

    // public

    public final boolean enableNetherstone;
    public final boolean enablePumpkins;
    public final boolean enableICs;
    public final boolean enableGlowStone;
}
