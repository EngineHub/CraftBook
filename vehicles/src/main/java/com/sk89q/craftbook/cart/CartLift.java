package com.sk89q.craftbook.cart;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Minecart;

public class CartLift extends CartMechanism {

    @Override
    public void impact(Minecart cart, CartMechanismBlocks blocks, boolean minor) {
        // validate
        if (cart == null) return;
        if (blocks.sign == null) return;
        if (!(blocks.matches("lift up") || blocks.matches("lift down"))) return;

        // go
        boolean up = blocks.matches("lift up");
        Block destination = blocks.sign;

        BlockFace face;
        if (up) face = BlockFace.UP;
        else face = BlockFace.DOWN;

        while (true) {

            destination = destination.getRelative(face);

            BlockState state = destination.getState();
            if (state instanceof Sign && blocks.base.getTypeId() == destination.getRelative(BlockFace.UP).getTypeId()) {
                String testLine = ((Sign) state).getLine(2);

                if (testLine.equalsIgnoreCase("[Lift Up]") || testLine.equalsIgnoreCase("[Lift Down]")
                        || testLine.equalsIgnoreCase("[Lift]")) {
                    destination.getRelative(BlockFace.UP, 2);
                    break;
                }
            }
        }

        cart.teleport(destination.getLocation());
    }
}
