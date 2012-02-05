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


package com.sk89q.craftbook.circuits;

import com.sk89q.craftbook.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.*;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * This mechanism allow players to toggle GlowStone.
 *
 * @author sk89q
 */
public class GlowStone extends AbstractMechanic {
    public static class Factory extends AbstractMechanicFactory<GlowStone> {
        public Factory() {
        }

        @Override
        public GlowStone detect(BlockWorldVector pt) {
            int type = BukkitUtil.toWorld(pt).getBlockTypeIdAt(BukkitUtil.toLocation(pt));

            if (type == BlockID.GLASS || type == BlockID.LIGHTSTONE) {
                return new GlowStone(pt);
            }

            return null;
        }
    }

    /**
     * Construct the mechanic for a location.
     *
     * @param pt
     */
    private GlowStone(BlockWorldVector pt) {
        super();
    }

    /**
     * Raised when an input redstone current changes.
     */
    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {
        byte data;

        data = event.getBlock().getData();
        if (event.getNewCurrent() > 0) {
            event.getBlock().setTypeId(BlockID.LIGHTSTONE);
        } else {
            event.getBlock().setTypeId(BlockID.GLASS);
        }
        event.getBlock().setData(data, false);
    }
    
    /**
     * Raised when clicked.
     */
    @Override
    public void onLeftClick(PlayerInteractEvent event) {
        if (event.getClickedBlock().isBlockPowered() && event.getClickedBlock().getTypeId() == BlockID.LIGHTSTONE) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Unload this mechanic.
     */
    @Override
    public void unload() {
    }

    /**
     * Check if this mechanic is still active.
     */
    @Override
    public boolean isActive() {
        return false;
    }
}
