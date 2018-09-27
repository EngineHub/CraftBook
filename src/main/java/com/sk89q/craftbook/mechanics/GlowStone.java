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

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.events.SourcedBlockRedstoneEvent;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

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

        if(!offBlock.equalsFuzzy(BukkitAdapter.adapt(event.getBlock().getBlockData())) && event.getBlock().getType() != Material.GLOWSTONE) return;

        if(event.isOn() == (event.getBlock().getType() == Material.GLOWSTONE))
            return;

        if (event.isOn()) {
            event.getBlock().setType(Material.GLOWSTONE);
        } else {
            event.getBlock().setBlockData(BukkitAdapter.adapt(offBlock));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!offBlock.equalsFuzzy(BukkitAdapter.adapt(event.getBlock().getBlockData())) && event.getBlock().getType() != Material.GLOWSTONE) return;

        if (event.getBlock().getType() == Material.GLOWSTONE && (event.getBlock().isBlockIndirectlyPowered() || event.getBlock().isBlockPowered()))
            event.setCancelled(true);
    }

    private BlockStateHolder offBlock;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "glowstone-off-block", "Sets the block that the redstone glowstone mechanic turns into when turned off.");
        offBlock = BlockSyntax.getBlock(config.getString(path + "glowstone-off-block", BlockTypes.GLASS.getId()), true);
    }
}