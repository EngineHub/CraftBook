package com.sk89q.craftbook.cart;
import static com.sk89q.craftbook.cart.CartUtils.reverse;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.RedstoneUtil.Power;
import com.sk89q.craftbook.util.SignUtil;

public class CartReverser extends CartMechanism {

    @Override
    public void impact(Minecart cart, CartMechanismBlocks blocks, boolean minor) {
        // validate
        if (cart == null) return;

        // care?
        if (minor) return;

        // enabled?
        if (Power.OFF == isActive(blocks.rail, blocks.base, blocks.sign)) return;

        if (blocks.sign == null || !blocks.matches("reverse")) {
            reverse(cart);
            return;
        }

        BlockFace dir = SignUtil.getFacing(blocks.sign);

        Vector normalVelocity = cart.getVelocity().normalize();

        if (CraftBookPlugin.inst().useOldBlockFace()) {
            switch (dir) {
                case NORTH:
                    if (normalVelocity.getBlockX() != -1) {
                        reverse(cart);
                    }
                    break;
                case SOUTH:
                    if (normalVelocity.getBlockX() != 1) {
                        reverse(cart);
                    }
                    break;
                case EAST:
                    if (normalVelocity.getBlockZ() != 1) {
                        reverse(cart);
                    }
                    break;
                case WEST:
                    if (normalVelocity.getBlockZ() != -1) {
                        reverse(cart);
                    }
                    break;
                default:
                    reverse(cart);
            }
        } else {
            switch (dir) {
                case NORTH:
                    if (normalVelocity.getBlockZ() != -1) {
                        reverse(cart);
                    }
                    break;
                case SOUTH:
                    if (normalVelocity.getBlockZ() != 1) {
                        reverse(cart);
                    }
                    break;
                case EAST:
                    if (normalVelocity.getBlockX() != -1) {
                        reverse(cart);
                    }
                    break;
                case WEST:
                    if (normalVelocity.getBlockX() != 1) {
                        reverse(cart);
                    }
                    break;
                default:
                    reverse(cart);
            }
        }
    }

    @Override
    public String getName() {

        return "Reverser";
    }

    @Override
    public String[] getApplicableSigns() {

        return new String[] {"reverse"};
    }
}