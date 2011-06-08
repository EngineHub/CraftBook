package com.sk89q.craftbook.cart;

import org.bukkit.*;
import org.bukkit.block.*;

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.util.*;
import com.sk89q.worldedit.blocks.*;

/**
 * <p>
 * Stores the tuple of three blocks over which any typical CartMechanism is
 * implemented, and also performs detection of two of the blocks if only one is
 * given.
 * </p>
 * 
 * <p>
 * Sign text and base block type are not validated by any constructors; this
 * must be performed explicitly by calling the appropriate methods after
 * construction. Selection of signs thus does not concern itself with sign text
 * at all; but this is fine, since if you have two signs in an area where they
 * could concievably contend for control of the mechanism, you would be doing
 * something that ought be physically impossible anyway (though yes, it is
 * possible if editing the world directly without physics).
 * </p>
 * 
 * @author hash
 * 
 */
public class CartMechanismBlocks {
    /**
     * Declarative constructor. No arguments are validated in any way.
     * 
     * @param rail
     *            the block containing the rails.
     * @param base
     *            the block on which the rails sit; the type of this block is
     *            what determines the mechanism type.
     * @param sign
     *            the block containing the sign that gives additional
     *            configuration to the mechanism, or null if not interested.
     */
    private CartMechanismBlocks(Block rail, Block base, Block sign) {
        this.rail = rail;
        this.base = base;
        this.sign = sign;
    }

    /**
     * <p>
     * Detecting factory; defers to one of the other three specific detecting
     * factories based on whether the given unknown block appears to be a sign,
     * rail, or base.
     * 
     * @param unknown
     *            the block to examine.
     * @param mat
     *            to be considered a base, the unknown block must match this
     *            material type.
     */
    public CartMechanismBlocks find(Block unknown, Material mat) throws InvalidMechanismException {
        final int ti = unknown.getTypeId();
        if (SignUtil.isSign(ti))
            return findBySign(unknown);
        else if (ti == mat.getId())
            return findByBase(unknown);
        else if (BlockType.isRailBlock(ti))
            return findByRail(unknown);
        else
            throw new InvalidMechanismException("wat");
    }
    
    /**
     * <p>
     * Detecting factory, based on the position of the rails. The base must
     * be one block below and the sign two or three blocks below. Signs are
     * guaranteed to be signs and rails are guaranteed to be rails.
     * </p>
     * 
     * <p>
     * This is the most important constructor, since it is the one invoked when
     * processing cart move events.
     * </p>
     * 
     * @param rail
     *            the block containing the rails.
     */
    public CartMechanismBlocks findByRail(Block rail) throws InvalidMechanismException {
        if (!BlockType.isRailBlock(sign.getTypeId())) throw new InvalidMechanismException("rail argument must be a rail!");
        if (SignUtil.isSign(rail.getFace(BlockFace.DOWN, 2).getTypeId())) {
            return new CartMechanismBlocks(
                    rail,
                    rail.getFace(BlockFace.DOWN, 1),
                    rail.getFace(BlockFace.DOWN, 2)
            );
        } else if (SignUtil.isSign(rail.getFace(BlockFace.DOWN, 3).getTypeId())) {
            return new CartMechanismBlocks(
                    rail,
                    rail.getFace(BlockFace.DOWN, 2),
                    rail.getFace(BlockFace.DOWN, 3)
            );
        }
        throw new InvalidMechanismException("could not find sign.");
    }
    
    /**
     * Detecting factory, based on the position of the base. The rails must
     * be one block above and the sign one or two blocks below. Signs are
     * guaranteed to be signs and rails are guaranteed to be rails.
     * 
     * @param base
     *            the block on which the rails sit; the type of this block is
     *            what determines the mechanism type.
     */
    public CartMechanismBlocks findByBase(Block base) throws InvalidMechanismException {
        if (!BlockType.isRailBlock(base.getFace(BlockFace.UP, 1).getTypeId())) throw new InvalidMechanismException("could not find rails.");
        if (SignUtil.isSign(rail.getFace(BlockFace.DOWN, 1).getTypeId())) {
            return new CartMechanismBlocks(
                    base.getFace(BlockFace.UP, 1),
                    base,
                    rail.getFace(BlockFace.DOWN, 1)
            );
        } else if (SignUtil.isSign(rail.getFace(BlockFace.DOWN, 2).getTypeId())) {
            return new CartMechanismBlocks(
                    base.getFace(BlockFace.UP, 1),
                    base,
                    rail.getFace(BlockFace.DOWN, 2)
            );
        }
        throw new InvalidMechanismException("could not find sign.");
    }
    
    /**
     * Detecting factory, based on the position of the sign. The base must
     * be one or two blocks above and the rails an additional block above the
     * base. Signs are guaranteed to be signs and rails are guaranteed to be
     * rails.
     * 
     * @param sign
     *            the block containing the sign that gives additional
     *            configuration to the mechanism.
     */
    public CartMechanismBlocks findBySign(Block sign) throws InvalidMechanismException {
        if (!SignUtil.isSign(sign)) throw new InvalidMechanismException("sign argument must be a sign!");
        if (BlockType.isRailBlock(sign.getFace(BlockFace.UP, 2).getTypeId())) {
            return new CartMechanismBlocks(
                    sign.getFace(BlockFace.UP, 2),
                    sign.getFace(BlockFace.UP, 1),
                    sign
            );
        } else if (BlockType.isRailBlock(sign.getFace(BlockFace.UP, 3).getTypeId())) {
            return new CartMechanismBlocks(
                    sign.getFace(BlockFace.UP, 3),
                    sign.getFace(BlockFace.UP, 2),
                    sign
            );
        }
        throw new InvalidMechanismException("could not find rails.");
    }
    
    public final Block rail;
    public final Block base;
    public final Block sign;

    /**
     * @param mechname
     * @return true if the bracketed keyword on the sign matches the given
     *         mechname; false otherwise or if no sign.
     * @throws ClassCastException
     *             if the declarative constructor was used in such a way that a
     *             non-sign block was specified for a sign.
     */
    public boolean matches(String mechname) {
        return (sign == null) ? false : (((Sign)sign.getState()).getLine(1).equalsIgnoreCase("["+mechname+"]"));
        // the astute will notice there's a problem coming up here with the one dang thing that had to go and break the mold with second line definer.        
    }
    
    /**
     * @param mat
     * @return true if the base block is the same type as the given material.
     */
    public boolean matches(Material mat) {
        return (base.getType() == mat);
    }
}
