package com.sk89q.craftbook;

import org.bukkit.block.*;
import org.bukkit.event.block.*;

import com.sk89q.craftbook.util.*;

public class BlockRedstoneNeighborEvent extends BlockRedstoneEvent {
    public BlockRedstoneNeighborEvent(BlockRedstoneEvent towrap, Block neighbor) {
        super(neighbor, towrap.getOldCurrent(), towrap.getNewCurrent());
        this.neighbor = towrap.getBlock();
        // we invert the blocks in order to keep with craftbook's 
        // running scheme of disbatching events to mechanics based
        // on the locations obtained via getBlock method
    }
    
    private final Block neighbor;
    
    public Block getCause() {
        return neighbor;
    }
    
    public boolean isDirect() {
        return false;   //TODO
    }
    
    
    
    // convenience methods for the typical usage in managers.
    /**
     * @param source
     * @return a new BlockRedstoneNeighborEvent for each position around the
     * source event (the original event is in index 0).
     */
    public static BlockRedstoneEvent[] haveKittens(BlockRedstoneEvent source) {
        BlockRedstoneEvent[] kittens = new BlockRedstoneNeighborEvent[KITTENS];
        kittens[0] = source;
        kittens[1] = new BlockRedstoneNeighborEvent(source, source.getBlock().getFace(BlockFace.NORTH));
        kittens[2] = new BlockRedstoneNeighborEvent(source, source.getBlock().getFace(BlockFace.EAST));
        kittens[3] = new BlockRedstoneNeighborEvent(source, source.getBlock().getFace(BlockFace.SOUTH));
        kittens[4] = new BlockRedstoneNeighborEvent(source, source.getBlock().getFace(BlockFace.WEST));
        kittens[5] = new BlockRedstoneNeighborEvent(source, source.getBlock().getFace(BlockFace.UP));
        kittens[6] = new BlockRedstoneNeighborEvent(source, source.getBlock().getFace(BlockFace.DOWN));
        return kittens;
    }
    public static final int KITTENS = 7; 
    
}
