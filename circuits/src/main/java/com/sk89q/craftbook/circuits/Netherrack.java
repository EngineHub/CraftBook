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

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

/**
 * This mechanism allow players to toggle the fire on top of Netherrack.
 *
 * @author sk89q
 */
public class Netherrack extends AbstractMechanic {

    public static class Factory extends AbstractMechanicFactory<Netherrack> {

        public Factory() {

        }

        @Override
        public Netherrack detect(BlockWorldVector pt) {

            int type = BukkitUtil.toWorld(pt).getBlockTypeIdAt(BukkitUtil.toLocation(pt));

            if (type == BlockID.NETHERRACK) return new Netherrack(pt);

            return null;
        }
    }

    /**
     * Construct the mechanic for a location.
     *
     * @param pt
     */
    private Netherrack(BlockWorldVector pt) {

        super();
    }

    /**
     * Raised when an input redstone current changes.
     */
    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        Block above = event.getBlock().getRelative(0, 1, 0);

        if (event.getNewCurrent() > 0 && above != null && canPassThrough(above.getTypeId())) {
            above.setTypeId(BlockID.FIRE, false);
        }
        else if (above.getTypeId() == BlockID.FIRE) {
            above.setTypeId(BlockID.AIR, false);
        }
    }

    /**
     * Raised when clicked.
     */
    @Override
    public void onLeftClick(PlayerInteractEvent event) {

        if (event.getBlockFace() != BlockFace.UP) return;

        Block block = event.getClickedBlock();

        if (block.isBlockIndirectlyPowered()) {
            event.setCancelled(true);
            return;
        }

        block = block.getRelative(0, -1, 0);
        if (block.isBlockIndirectlyPowered()) {
            event.setCancelled(true);
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

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

    }

    @Override
    public void unloadWithEvent(ChunkUnloadEvent event) {

    }

    private boolean canPassThrough(int t) {

        int[] passableBlocks = new int[9];
        passableBlocks[0] = BlockID.WATER;
        passableBlocks[1] = BlockID.STATIONARY_WATER;
        passableBlocks[2] = BlockID.LAVA;
        passableBlocks[3] = BlockID.STATIONARY_LAVA;
        passableBlocks[4] = BlockID.SNOW;
        passableBlocks[5] = BlockID.LONG_GRASS;
        passableBlocks[6] = BlockID.VINE;
        passableBlocks[7] = BlockID.DEAD_BUSH;
        passableBlocks[8] = BlockID.AIR;

        for (int aPassableBlock : passableBlocks)
            if (aPassableBlock == t) return true;

        return false;
    }
}