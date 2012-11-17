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

import java.util.Arrays;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;

import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.PersistentMechanic;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

/**
 * This mechanism allow players to toggle Jack-o-Lanterns.
 *
 * @author sk89q
 */
public class JackOLantern extends PersistentMechanic {

    public static class Factory extends AbstractMechanicFactory<JackOLantern> {

        CircuitsPlugin plugin;

        public Factory(CircuitsPlugin plugin) {

            this.plugin = plugin;
        }

        @Override
        public JackOLantern detect(BlockWorldVector pt) {

            int type = BukkitUtil.toWorld(pt).getBlockTypeIdAt(BukkitUtil.toLocation(pt));

            if (type == BlockID.PUMPKIN || type == BlockID.JACKOLANTERN) return new JackOLantern(plugin, pt);

            return null;
        }
    }

    CircuitsPlugin plugin;
    BlockWorldVector pt;

    /**
     * Construct the mechanic for a location.
     *
     * @param pt
     */
    private JackOLantern(CircuitsPlugin plugin, BlockWorldVector pt) {

        super();
        this.plugin = plugin;
        this.pt = pt;
    }

    /**
     * Raised when an input redstone current changes.
     */
    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        setPowered(event.getBlock(), event.getNewCurrent() > 0);

        event.getBlock().setData(event.getBlock().getData(), false);
    }

    /**
     * Unload this mechanic.
     */
    @Override
    public void unload() {

    }

    public void setPowered(Block block, boolean on) {

        byte data = block.getData();
        block.setTypeId(on ? BlockID.JACKOLANTERN : BlockID.PUMPKIN);
        block.setData(data);
    }

    /**
     * Check if this mechanic is still active.
     */
    @Override
    public boolean isActive() {

        return BukkitUtil.toBlock(pt).getTypeId() == BlockID.JACKOLANTERN;
    }

    @Override
    public List<BlockWorldVector> getWatchedPositions() {

        return Arrays.asList(pt);
    }

    @Override
    public void onWatchBlockNotification(BlockEvent evt) {

        if (evt instanceof BlockBreakEvent)
            if (evt.getBlock().getTypeId() == BlockID.JACKOLANTERN && (evt.getBlock().isBlockIndirectlyPowered() ||
                    evt.getBlock().isBlockPowered())) {
                ((BlockBreakEvent) evt).setCancelled(true);
            }
    }
}