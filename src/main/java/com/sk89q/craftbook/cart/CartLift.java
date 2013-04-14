package com.sk89q.craftbook.cart;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Minecart;

import com.sk89q.craftbook.util.CartUtils;

public class CartLift extends CartMechanism {

    @Override
    public void impact(final Minecart cart, CartMechanismBlocks blocks, boolean minor) {
        // validate
        if (cart == null) return;
        if (blocks.sign == null) return;
        if (!(blocks.matches("cartlift up") || blocks.matches("cartlift down"))) return;

        // go
        boolean up = blocks.matches("cartlift up");
        Block destination = blocks.sign;

        BlockFace face;
        if (up) face = BlockFace.UP;
        else face = BlockFace.DOWN;

        while (true) { 

            if(destination.getLocation().getBlockY() <= 0 && !up)
                return;
            if(destination.getLocation().getBlockY() >= destination.getWorld().getMaxHeight()-1 && up)
                return;

            destination = destination.getRelative(face);

            if (destination.getState() instanceof Sign && blocks.base.getTypeId() == destination.getRelative(BlockFace.UP, 1).getTypeId()) {

                Sign state = (Sign) destination.getState();
                String testLine = state.getLine(1);

                if (testLine.equalsIgnoreCase("[CartLift Up]") || testLine.equalsIgnoreCase("[CartLift Down]") || testLine.equalsIgnoreCase("[CartLift]")) {
                    destination = destination.getRelative(BlockFace.UP, 2);
                    break;
                }
            }
        }

        CartUtils.teleport(cart, new Location(destination.getWorld(), destination.getX(), destination.getY(), destination.getZ(), cart.getLocation().getYaw(), cart.getLocation().getPitch()));
    }

    @Override
    public String getName() {

        return "CartLift";
    }

    @Override
    public String[] getApplicableSigns() {

        return new String[] {"CartLift Up", "CartLift Down", "CartLift"};
    }
}