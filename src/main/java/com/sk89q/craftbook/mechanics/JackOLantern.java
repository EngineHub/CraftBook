// $Id$
/*
 * CraftBook Copyright (C) 2010 sk89q <http://www.sk89q.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.mechanics;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.util.yaml.YAMLProcessor;

/**
 * This mechanism allow players to toggle Jack-o-Lanterns.
 *
 * @author sk89q
 */
public class JackOLantern extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(event.isMinor())
            return;

        if (event.getBlock().getType() != Material.CARVED_PUMPKIN && event.getBlock().getType() != Material.JACK_O_LANTERN) return;

        if(event.isOn() == (event.getBlock().getType() == Material.JACK_O_LANTERN))
            return;

        setPowered(event.getBlock(), event.isOn());
    }

    private static void setPowered(Block block, boolean on) {

        BlockFace data = ((Directional) block.getBlockData()).getFacing();
        block.setType(on ? Material.JACK_O_LANTERN : Material.CARVED_PUMPKIN);
        Directional directional = (Directional) block.getBlockData();
        directional.setFacing(data);
        block.setBlockData(directional);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (event.getBlock().getType() != Material.CARVED_PUMPKIN && event.getBlock().getType() != Material.JACK_O_LANTERN) return;

        if (event.getBlock().getType() == Material.JACK_O_LANTERN && (event.getBlock().isBlockIndirectlyPowered() || event.getBlock().isBlockPowered()))
            event.setCancelled(true);
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

    }
}