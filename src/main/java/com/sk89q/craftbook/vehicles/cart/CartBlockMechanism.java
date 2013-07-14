package com.sk89q.craftbook.vehicles.cart;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.Listener;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.util.EntityUtil;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.RedstoneUtil;
import com.sk89q.craftbook.util.RedstoneUtil.Power;

/**
 * Implementers of CartMechanism are intended to be singletons and do all their logic at interation time (like
 * non-persistant mechanics, but allowed
 * zero state even in RAM). In order to be effective, configuration loading in MinecartManager must be modified to
 * include an implementer.
 *
 * @author hash
 */
public abstract class CartBlockMechanism implements Listener {

    protected final ItemInfo material;

    public ItemInfo getMaterial() {

        return material;
    }

    public CartBlockMechanism(ItemInfo material) {
        this.material = material;
    }

    public static final BlockFace[] powerSupplyOptions = new BlockFace[] {
        BlockFace.NORTH, BlockFace.EAST,
        BlockFace.SOUTH, BlockFace.WEST
    };

    /**
     * Determins if a cart mechanism should be enabled.
     *
     * @param blocks The {@link CartMechanismBlocks} that represents the blocks that are being checked for activity on.
     *
     * @return the appropriate Power state (see the documentation for {@link RedstoneUtil.Power}'s members).
     */
    public Power isActive(CartMechanismBlocks blocks) {

        boolean isWired = false;
        if (blocks.hasSign()) {
            switch (isActive(blocks.sign)) {
                case ON:
                    return Power.ON;
                case NA:
                    break;
                case OFF:
                    isWired = true;
            }
        }
        if (blocks.hasBase()) {
            switch (isActive(blocks.base)) {
                case ON:
                    return Power.ON;
                case NA:
                    break;
                case OFF:
                    isWired = true;
            }
        }
        if (blocks.hasRail()) {
            switch (isActive(blocks.rail)) {
                case ON:
                    return Power.ON;
                case NA:
                    break;
                case OFF:
                    isWired = true;
            }
        }
        return isWired ? Power.OFF : Power.NA;
    }

    /**
     * Checks if any of the blocks horizonally adjacent to the given block are powered wires.
     *
     * @param block
     *
     * @return the appropriate Power state (see the documentation for {@link RedstoneUtil.Power}'s members).
     */
    private Power isActive(Block block) {

        boolean isWired = false;
        for (BlockFace face : powerSupplyOptions) {
            Power p = RedstoneUtil.isPowered(block, face);
            switch (p) {
                case ON:
                    return Power.ON;
                case NA:
                    break;
                case OFF:
                    isWired = true;
            }
        }
        return isWired ? Power.OFF : Power.NA;
    }

    /**
     * @param rail the block we're searching for carts (mostly likely containing rails generally,
     *             though it's not strictly relevant).
     *
     * @return a Minecart if one is found within the given block, or null if none found. (If there is more than one
     *         minecart within the block, the
     *         first one encountered when traversing the list of Entity in the Chunk is the one returned.)
     */
    public static Minecart getCart(Block rail) {

        for (Entity ent : rail.getChunk().getEntities()) {
            if (!(ent instanceof Minecart))
                continue;
            if(EntityUtil.isEntityInBlock(ent, rail))
                return (Minecart) ent;
        }
        return null;
    }

    public abstract String getName();

    public abstract String[] getApplicableSigns();

    public boolean verify(ChangedSign sign, LocalPlayer player) {

        return true;
    }
}