package com.sk89q.craftbook.mechanics.minecart.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.CartUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;

public class CartLift extends CartBlockMechanism {

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

                ChangedSign state = CraftBookBukkitUtil.toChangedSign(destination);
                String testLine = state.getLine(1);

                if (testLine.equalsIgnoreCase("[CartLift Up]") || testLine.equalsIgnoreCase("[CartLift Down]") || testLine.equalsIgnoreCase("[CartLift]")) {
                    destination = destination.getRelative(BlockFace.UP, 2);
                    break;
                }
            }
        }

        CartUtil.teleport(cart, new Location(destination.getWorld(), destination.getX(), destination.getY(), destination.getZ(), cart.getLocation().getYaw(), cart.getLocation().getPitch()));
    }

    @Override
    public String getName() {

        return "CartLift";
    }

    @Override
    public String[] getApplicableSigns() {

        return new String[] {"CartLift Up", "CartLift Down", "CartLift"};
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "block", "Sets the block that is the base of the elevator mechanic.");
        material = BlockSyntax.getBlock(config.getString(path + "block", BlockTypes.NETHER_BRICKS.getId()), true);
    }
}