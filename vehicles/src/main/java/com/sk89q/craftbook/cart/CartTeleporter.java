package com.sk89q.craftbook.cart;

import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;

import com.sk89q.worldedit.bukkit.BukkitUtil;


public class CartTeleporter extends CartMechanism {

    private static final Pattern COMMA_PATTERN = Pattern.compile(",", Pattern.LITERAL);

    @Override
    public void impact(Minecart cart, CartMechanismBlocks blocks, boolean minor) {
        // validate
        if (cart == null) return;

        if (!blocks.matches("teleport")) return;

        // go
        World world = cart.getWorld();
        String[] pts = COMMA_PATTERN.split(blocks.getSign().getLine(2).trim(), 3);
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

        Location loc = BukkitUtil.center(new Location(world, x, y, z, cart.getLocation().getYaw(), cart.getLocation().getPitch()) {

        });
        if (!loc.getChunk().isLoaded()) {
            loc.getChunk().load(true);
        }
        if (cart.getWorld() == world) {
            cart.teleport(loc);
        } else {
            Minecart toCart = world.spawn(loc, Minecart.class);
            Entity passenger = cart.getPassenger();
            if (passenger != null) {
                cart.eject();
                passenger.teleport(loc);
                toCart.setPassenger(passenger);
            }
            toCart.setVelocity(cart.getVelocity()); // speedy thing goes in, speedy thing comes out
            cart.remove();
        }
    }
}