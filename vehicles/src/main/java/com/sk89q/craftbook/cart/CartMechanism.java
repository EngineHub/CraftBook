package com.sk89q.craftbook.cart;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;

import com.sk89q.craftbook.RedstoneUtil;
import com.sk89q.craftbook.RedstoneUtil.Power;

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
    /**
     * Called by MinecartManager after either a vehicle move event or a redstone
     * event results in CartMechanismBlocks concluding there is a base block of
     * this CartMechanism's material type involved.
     * 
     * @param cart
     *            if triggered by a move event, the cart involved; if triggered
     *            by redstone, a cart in the rail block or null if none.
     * @param blocks
     * @param minor
     *            true if the triggering event is somehow 'minor' (namely, a
     *            move event from one part of the same block to another); false
     *            otherwise (i.e. redstone events or move events that cross
     *            block boundaries). Most CartMechanism can safely ignore minor
     *            events; CartStation is an example of an exception because its
     *            brake-locking functionality.
     */
    public abstract void impact(Minecart cart, CartMechanismBlocks blocks, boolean minor);

    public abstract void enter(Minecart cart, Entity entity, CartMechanismBlocks blocks, boolean minor);

    protected Material material; void setMaterial(Material mat) { material = mat; }
    public static final BlockFace[] powerSupplyOptions = new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };

    /**
     * Determins if a cart mechanism should be enabled.
     * 
     * @param base the block on which the rails sit (the type of this block is generally what determines the mechanism type), or null if not interested.
     * @param rail the block containing the rails, or null if not interested.
     * @param sign the block containing the signpost that gives additional configuration to the mechanism, or null if not interested.
     * @return the appropriate Power state (see the documentation for {@link RedstoneUtil.Power}'s members).
     */
    public Power isActive(Block rail, Block base, Block sign) {
        boolean isWired = false;
        if (sign != null) {
            //System.out.println("\tsign:");
            switch (isActive(sign)) {
            case ON: return Power.ON;
            case NA: break;
            case OFF: isWired = true;
            }
        }
        if (base != null) {
            //System.out.println("\tbase:");
            switch (isActive(base)) {
            case ON: return Power.ON;
            case NA: break;
            case OFF: isWired = true;
            }
        }
        if (rail != null) {
            //System.out.println("\trail:");
            switch (isActive(rail)) {
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
            //System.out.println("\t\tdirection:"+face);
            Power p = RedstoneUtil.isPowered(block, face);
            //switch (p) {
            //    case ON:  System.out.println("\t\t\tpower:ON");  break;
            //    case NA:  System.out.println("\t\t\tpower:NA");  break;
            //    case OFF: System.out.println("\t\t\tpower:OFF"); break;
            //}
            switch (p) {
            case ON: return Power.ON;
            case NA: break;
            case OFF: isWired = true;
            }
        }
        return (isWired ? Power.OFF : Power.NA);
    }

    /**
     * @param rail
     *            the block we're searching for carts (mostly likely containing
     *            rails generally, though it's not strictly relevant).
     * @return a Minecart if one is found within the given block, or null if
     *         none found. (If there is more than one minecart within the block,
     *         the first one encountered when traversing the list of Entity in
     *         the Chunk is the one returned.)
     */
    public static Minecart getCart(Block rail) {
        for (Entity ent : rail.getChunk().getEntities()) {
            if (!(ent instanceof Minecart)) continue;
            if (ent.getLocation().getBlockX() != rail.getLocation().getBlockX()) continue;
            if (ent.getLocation().getBlockY() != rail.getLocation().getBlockY()) continue;
            if (ent.getLocation().getBlockZ() != rail.getLocation().getBlockZ()) continue;
            return (Minecart) ent;
        }
        return null;
    }
}
