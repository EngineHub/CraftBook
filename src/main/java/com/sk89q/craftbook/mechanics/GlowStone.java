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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;

/**
 * This mechanism allow players to toggle GlowStone.
 *
 * @author sk89q
 */
public class GlowStone extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(event.isMinor())
            return;

        if(!CraftBookPlugin.inst().getConfiguration().glowstoneOffBlock.isSame(event.getBlock()) && event.getBlock().getType() != Material.GLOWSTONE) return;

        if(event.isOn() == (event.getBlock().getType() == Material.GLOWSTONE))
            return;

        event.getBlock().setType(event.isOn() ? Material.GLOWSTONE : CraftBookPlugin.inst().getConfiguration().glowstoneOffBlock.getType());
        event.getBlock().setData((byte) (event.isOn() ? event.getBlock().getData() : CraftBookPlugin.inst().getConfiguration().glowstoneOffBlock.getData() == -1 ? event.getBlock().getData() : CraftBookPlugin.inst().getConfiguration().glowstoneOffBlock.getData()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!CraftBookPlugin.inst().getConfiguration().glowstoneOffBlock.isSame(event.getBlock()) && event.getBlock().getType() != Material.GLOWSTONE) return;

        if (event.getBlock().getType() == Material.GLOWSTONE && (event.getBlock().isBlockIndirectlyPowered() || event.getBlock().isBlockPowered()))
            event.setCancelled(true);
    }
}