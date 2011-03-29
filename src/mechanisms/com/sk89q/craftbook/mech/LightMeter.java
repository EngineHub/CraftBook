// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.mech;

import org.bukkit.Material;
import org.bukkit.event.player.*;

import com.sk89q.craftbook.Mechanic;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.MechanismsConfiguration;

/**
 * This allows users to right-click to check the light level of any block
 *
 * @author wizjany
 */
public class LightMeter extends Mechanic {

    protected MechanismsPlugin plugin;
    private MechanismsConfiguration.LightMeterSettings settings;

    public LightMeter(MechanismsPlugin plugin) {
        super();
        this.plugin = plugin;
    }


    @Override
    public void onRightClick(PlayerInteractEvent event) {
        if (!plugin.getLocalConfiguration().lightMeterSettings.enable) return;
    	if(event.getPlayer().getItemInHand().getType() == Material.GLOWSTONE_DUST)
    	{
    		String line = getLightLevelLine(event.getClickedBlock().getLightLevel());
    		event.getPlayer().sendMessage("Light level is: " + org.bukkit.ChatColor.RED + event.getClickedBlock().getLightLevel() + " " + org.bukkit.ChatColor.YELLOW + line);
    	}
    }
    private String getLightLevelLine(byte data) {
		String line = "[";
		for(int i = 0;i < data;i++)
		{
			line = line + "|";
		}
		line = line + "]";
		return line;
	}

    @Override
    public void unload() {
    }

    @Override
    public boolean isActive() {
        return false;   // this isn't a persistent mechanic, so the manager will never keep it around long enough to even check this.
    }

}
