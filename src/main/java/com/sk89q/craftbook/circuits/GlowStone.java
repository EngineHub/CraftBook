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

package com.sk89q.craftbook.circuits;

import java.util.Arrays;
import java.util.List;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;

import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.PersistentMechanic;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

/**
 * This mechanism allow players to toggle GlowStone.
 *
 * @author sk89q
 */
public class GlowStone extends PersistentMechanic {

    public static class Factory extends AbstractMechanicFactory<GlowStone> {

        @Override
        public GlowStone detect(BlockWorldVector pt) {

            int type = BukkitUtil.toWorld(pt).getBlockTypeIdAt(BukkitUtil.toLocation(pt));

            if (type == CraftBookPlugin.inst().getConfiguration().glowstoneOffBlock || type == BlockID.LIGHTSTONE)
                return new GlowStone(pt);

            return null;
        }
    }

    BlockWorldVector pt;

    /**
     * Construct the mechanic for a location.
     *
     * @param pt
     */
    private GlowStone(BlockWorldVector pt) {

        super();
        this.pt = pt;
    }

    /**
     * Raised when an input redstone current changes.
     */
    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        event.getBlock().setTypeIdAndData(event.getNewCurrent() > 0 ? BlockID.LIGHTSTONE : CraftBookPlugin.inst().getConfiguration().glowstoneOffBlock, event.getBlock().getData(), true);
    }

    /**
     * Check if this mechanic is still active.
     */
    @Override
    public boolean isActive() {

        return BukkitUtil.toBlock(pt).getTypeId() == BlockID.LIGHTSTONE;
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

        if (event.getBlock().getTypeId() == BlockID.LIGHTSTONE && (event.getBlock().isBlockIndirectlyPowered() || event.getBlock().isBlockPowered()))
            event.setCancelled(true);
    }

    @Override
    public List<BlockWorldVector> getWatchedPositions() {

        return Arrays.asList(pt);
    }

    @Override
    public void onWatchBlockNotification(BlockEvent evt) {

        if (evt instanceof BlockBreakEvent)
            if (evt.getBlock().getTypeId() == BlockID.LIGHTSTONE && (evt.getBlock().isBlockIndirectlyPowered() || evt.getBlock().isBlockPowered()))
                ((BlockBreakEvent) evt).setCancelled(true);
    }
}