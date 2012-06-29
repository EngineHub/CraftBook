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

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.bukkit.MechanismsPlugin;

import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.bukkit.BukkitUtil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * This allows users to Right-click to check the light levels.
 */
public class LightMeter extends AbstractMechanic {

    protected MechanismsPlugin plugin;

    public LightMeter(MechanismsPlugin plugin) {
	super();
	this.plugin = plugin;
    }

    @Override
    public void onRightClick(PlayerInteractEvent event) {
	if (!plugin.wrap(event.getPlayer()).hasPermission("craftbook.mech.lightmeter.use")) {
	    return;
	}

	Block block = event.getClickedBlock();
	if (event.getPlayer().getItemInHand().getType() == Material.GLOWSTONE_DUST) {
	    byte data = getSpecialData(block);
	    event.getPlayer().sendMessage(ChatColor.YELLOW + "LightMeter: " + ChatColor.WHITE + data + " L");
	}
    }

    private byte getSpecialData(Block block) {
	return block.getLightLevel();
    }

    public void unload() {
    }

    public boolean isActive() {
	return false; // this isn't a persistent mechanic, so the manager will
	// never keep it around long enough to even check this.
    }


    public static class Factory extends AbstractMechanicFactory<LightMeter> {

	protected MechanismsPlugin plugin;

	public Factory(MechanismsPlugin plugin) {
	    this.plugin = plugin;
	}

	public LightMeter detect(BlockWorldVector pt) {
	    Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
	    if(block.getTypeId() != 0 && block.getLightLevel() < 15 && block.getLightLevel() > 0)
		return new LightMeter(plugin);
	    else
		return null;
	}
    }


    @Override
    public void onBlockBreak(BlockBreakEvent event) {

    }
}
