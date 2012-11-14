package com.sk89q.craftbook.cart;

import com.sk89q.worldedit.bukkit.BukkitUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;

import java.util.regex.Pattern;


public class CartTeleporter extends CartMechanism {

    private static final Pattern COMMA_PATTERN = Pattern.compile(",", Pattern.LITERAL);

    @Override
    public void impact(Minecart cart, CartMechanismBlocks blocks, boolean minor) {
        // validate
        if (cart == null) return;
        if (blocks.sign == null) return;
        if (!blocks.matches("teleport")) return;

        // go
        World world = cart.getWorld();
        String line = blocks.getSign().getLine(2);
        String[] pts = COMMA_PATTERN.split(line);
        if (pts.length != 3) return;
        if (!blocks.getSign().getLine(3).isEmpty()) {
            world = cart.getServer().getWorld(blocks.getSign().getLine(3));
        }

        double x;
        double y;
        double z;
        try {
            x = Double.parseDouble(pts[0]);
            y = Double.parseDouble(pts[1]);
            z = Double.parseDouble(pts[2]);
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
            cart.setVelocity(new Vector(0D, 0D, 0D));
        }

        Location loc = BukkitUtil.center(new Location(world, x, y, z, 0, 0) {

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
            toCart.setVelocity(cart.getVelocity()); // speedy thing goes in, speedy thing comes out <- Nice portal
            // quote :)
            cart.remove();
        }
    }

    @Override
    public void enter(Minecart cart, Entity entity, CartMechanismBlocks blocks,
            boolean minor) {

    }
}