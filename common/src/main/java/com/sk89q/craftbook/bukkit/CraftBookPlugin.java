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

package com.sk89q.craftbook.bukkit;

import com.sk89q.craftbook.CommonConfiguration;

/**
 * Plugin for CraftBook's core.
 *
 * @author sk89q
 */
public class CraftBookPlugin extends BaseBukkitPlugin {

	private static CraftBookPlugin instance;

	public static CraftBookPlugin getInstance() {
		return instance;
	}

	private CommonConfiguration config;

    @Override
    protected void registerEvents() {

	    instance = this;
	    config = new CommonConfiguration(getConfig(), getDataFolder());
	    saveConfig();
    }

	@Override
	public CommonConfiguration getLocalConfiguration() {
		return config;
	}
}
