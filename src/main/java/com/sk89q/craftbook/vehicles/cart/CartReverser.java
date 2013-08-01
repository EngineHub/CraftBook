package com.sk89q.craftbook.vehicles.cart;
import static com.sk89q.craftbook.util.CartUtils.reverse;

import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.RedstoneUtil.Power;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.vehicles.cart.events.CartBlockImpactEvent;

public class CartReverser extends CartBlockMechanism {

    public CartReverser (ItemInfo material) {
        super(material);
    }

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {

        // validate
        if (!event.getBlocks().matches(getMaterial())) return;
        if (event.isMinor()) return;

        // enabled?
        if (Power.OFF == isActive(event.getBlocks())) return;

        if (!event.getBlocks().hasSign() || !event.getBlocks().matches("reverse")) {
            reverse(event.getMinecart());
            return;
        }

        BlockFace dir = SignUtil.getFacing(event.getBlocks().sign);

        Vector normalVelocity = event.getMinecart().getVelocity().normalize();

        if (CraftBookPlugin.inst().useOldBlockFace()) {
            switch (dir) {
                case NORTH:
                    if (normalVelocity.getBlockX() != -1) {
                        reverse(event.getMinecart());
                    }
                    break;
                case SOUTH:
                    if (normalVelocity.getBlockX() != 1) {
                        reverse(event.getMinecart());
                    }
                    break;
                case EAST:
                    if (normalVelocity.getBlockZ() != -1) {
                        reverse(event.getMinecart());
                    }
                    break;
                case WEST:
                    if (normalVelocity.getBlockZ() != 1) {
                        reverse(event.getMinecart());
                    }
                    break;
                default:
                    reverse(event.getMinecart());
            }
        } else {
            switch (dir) {
                case NORTH:
                    if (normalVelocity.getBlockZ() != -1) {
                        reverse(event.getMinecart());
                    }
                    break;
                case SOUTH:
                    if (normalVelocity.getBlockZ() != 1) {
                        reverse(event.getMinecart());
                    }
                    break;
                case EAST:
                    if (normalVelocity.getBlockX() != 1) {
                        reverse(event.getMinecart());
                    }
                    break;
                case WEST:
                    if (normalVelocity.getBlockX() != -1) {
                        reverse(event.getMinecart());
                    }
                    break;
                default:
                    reverse(event.getMinecart());
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