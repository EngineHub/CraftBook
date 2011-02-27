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

package com.sk89q.craftbook;

import org.bukkit.block.*;
import org.bukkit.event.block.*;

/**
 * This event is raised when redstone current is detected for a block
 * that the regular redstone event won't trigger on. This is a temporary
 * fix for the fact that Bukkit doesn't call the redstone events on blocks
 * we care about (signs, netherrack, pumpkins), and this class cannot be
 * depended upon to existin the future.
 *  
 * @author hash
 */
public class BlockRedstoneNeighborEvent extends BlockRedstoneEvent {
    private static final long serialVersionUID = 5973553903502817198L;
    public static final int KITTENS = 7;
    
    /**
     * Holds the block that is being detected for.
     */
    private final Block neighbor;

    /**
     * Construct the event.
     * 
     * @param towrap
     * @param neighbor
     */
    public BlockRedstoneNeighborEvent(BlockRedstoneEvent towrap, Block neighbor) {
        super(neighbor, towrap.getOldCurrent(), towrap.getNewCurrent());
        this.neighbor = towrap.getBlock();
        
        // We invert the blocks in order to keep with craftbook's 
        // running scheme of dispatching events to mechanics based
        // on the locations obtained via getBlock method
    }
    
    /**
     * Get the neighbor block.
     * 
     * @return
     */
    public Block getCause() {
        return neighbor;
    }
    
    
    /**
     * @param source
     * @return a new BlockRedstoneNeighborEvent for each position around the
     * source event (the original event is in index 0).
     */
    public static BlockRedstoneEvent[] haveKittens(BlockRedstoneEvent source) {
        BlockRedstoneEvent[] kittens = new BlockRedstoneNeighborEvent[KITTENS];
        kittens[0] = source;
        // it would probably be wise to check if the neighbor block is a redstone
        // block itself and not send these if it is, since it'll presumably be getting 
        // its very own BlockRedstoneEvent momentarily (or just did!) and it probably 
        // doesn't really care about its neighbor a big.
        kittens[1] = new BlockRedstoneNeighborEvent(source, source.getBlock().getFace(BlockFace.NORTH));
        kittens[2] = new BlockRedstoneNeighborEvent(source, source.getBlock().getFace(BlockFace.EAST));
        kittens[3] = new BlockRedstoneNeighborEvent(source, source.getBlock().getFace(BlockFace.SOUTH));
        kittens[4] = new BlockRedstoneNeighborEvent(source, source.getBlock().getFace(BlockFace.WEST));
        kittens[5] = new BlockRedstoneNeighborEvent(source, source.getBlock().getFace(BlockFace.UP));
        kittens[6] = new BlockRedstoneNeighborEvent(source, source.getBlock().getFace(BlockFace.DOWN));
        return kittens;
    } 
    
}
