package com.sk89q.craftbook.vehicles.cart;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.CartUtils;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.vehicles.cart.events.CartBlockImpactEvent;

public class CartLift extends CartBlockMechanism {

    public CartLift (ItemInfo material) {
        super(material);
    }

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {

        // validate
        if (!event.getBlocks().matches(getMaterial())) return;
        if (!event.getBlocks().hasSign()) return;
        if (event.isMinor()) return;
        if (!(event.getBlocks().matches("cartlift up") || event.getBlocks().matches("cartlift down"))) return;

        Minecart cart = (Minecart) event.getVehicle();

        // go
        boolean up = event.getBlocks().matches("cartlift up");
        Block destination = event.getBlocks().sign;

        BlockFace face;
        if (up) face = BlockFace.UP;
        else face = BlockFace.DOWN;

        while (true) {

            if(destination.getLocation().getBlockY() <= 0 && !up)
                return;
            if(destination.getLocation().getBlockY() >= destination.getWorld().getMaxHeight()-1 && up)
                return;

            destination = destination.getRelative(face);

            if (SignUtil.isSign(destination) && event.getBlocks().base.getType() == destination.getRelative(BlockFace.UP, 1).getType()) {

                ChangedSign state = BukkitUtil.toChangedSign(destination);
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