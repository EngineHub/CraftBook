package com.sk89q.craftbook.cart;

import static com.sk89q.craftbook.cart.CartUtils.stop;

import java.util.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.vehicle.*;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.RedstoneUtil.Power;
import com.sk89q.worldedit.blocks.*;

/**
 * Implementers of CartMechanism are intended to be singletons and do all their
 * logic at interation time (like non-persistant mechanics, but allowed zero
 * state even in RAM). In order to be effective, configuration loading in
 * MinecartManager must be modified to include an implementer.
 * 
 * @author hash
 * 
 */
public abstract class CartMechanism {
    protected static final BlockFace[] powerSupplyOptions = new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
    
    public abstract void impact(Minecart cart, Block entered, Block from);
    
    /**
     * Determins if a cart mechanism should be enabled.
     * 
     * @param base the block on which the rails sit (the type of this block is generally what determines the mechanism type), or null if not interested.
     * @param rail the block containing the rails, or null if not interested.
     * @param sign the block containing the signpost that gives additional configuration to the mechanism, or null if not interested.
     * @return the appropriate Power state (see the documentation for {@link RedstoneUtil.Power}'s members).
     */
    public Power isActive(Block base, Block rail, Block sign) {
        boolean isWired = false;
        if (sign != null) {
            switch (isActive(sign)) {
                case ON: return Power.ON; 
                case NA: break;
                case OFF: isWired = true;
            }
        }
        if (base != null) {
            switch (isActive(base)) {
                case ON: return Power.ON; 
                case NA: break;
                case OFF: isWired = true;
            }
        }
        if (rail != null) {
            switch (isActive(base)) {
                case ON: return Power.ON; 
                case NA: break;
                case OFF: isWired = true;
            }
        }
        return (isWired ? Power.OFF : Power.NA);
    }
    /**
     * Checks if any of the blocks horizonally adjacent to the given block are powered wires.
     * @param block
     * @return the appropriate Power state (see the documentation for {@link RedstoneUtil.Power}'s members).
     */
    private Power isActive(Block block) {
        boolean isWired = false;
        for (BlockFace face : powerSupplyOptions) {
            switch (RedstoneUtil.isPowered(block, face)) {
                case ON: return Power.ON;
                case NA: break;
                case OFF: isWired = true;
            }
        }
        return (isWired ? Power.OFF : Power.NA);
    }
}
