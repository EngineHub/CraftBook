package com.sk89q.craftbook.cart;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Minecart;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.CartUtils;
import com.sk89q.craftbook.util.RegexUtil;

public class CartTeleporter extends CartMechanism {

    @Override
    public void impact(final Minecart cart, CartMechanismBlocks blocks, boolean minor) {
        // validate
        if (cart == null) return;

        if (!blocks.matches("teleport")) return;

        // go
        World world = cart.getWorld();
        String[] pts = RegexUtil.COMMA_PATTERN.split(blocks.getSign().getLine(2).trim(), 3);
        if (!blocks.getSign().getLine(3).trim().isEmpty()) {
            world = cart.getServer().getWorld(blocks.getSign().getLine(3).trim());
        }

        double x;
        double y;
        double z;
        try {
            x = Double.parseDouble(pts[0].trim());
            y = Double.parseDouble(pts[1].trim());
            z = Double.parseDouble(pts[2].trim());
        } catch (NumberFormatException e) {
            // incorrect format, just set them still and let them figure it out
            if (blocks.from != null) {
                x = blocks.from.getX();
                y = blocks.from.getY();
                z = blocks.from.getZ();
            } else {
                x = blocks.rail.getX();
                y = blocks.rail.getY();
                z = blocks.rail.getZ();
            }
            CartUtils.stop(cart);
        }

        Location loc = BukkitUtil.center(new Location(world, x, y, z, cart.getLocation().getYaw(), cart.getLocation().getPitch()));
        loc.getChunk().load(true);
        CartUtils.teleport(cart, loc);
    }

    @Override
    public boolean verify(ChangedSign sign, LocalPlayer player) {

        String[] pts = RegexUtil.COMMA_PATTERN.split(sign.getLine(2).trim(), 3);
        try {
            Double.parseDouble(pts[0].trim());
            Double.parseDouble(pts[1].trim());
            Double.parseDouble(pts[2].trim());
        } catch (NumberFormatException e) {
            player.printError("Line 3 must contain coordinates seperated by a comma! (x,y,z)");
            return false;
        }
        return true;
    }

    @Override
    public String getName() {

        return "Teleporter";
    }

    @Override
    public String[] getApplicableSigns() {

        return new String[] {"Teleport"};
    }
}
