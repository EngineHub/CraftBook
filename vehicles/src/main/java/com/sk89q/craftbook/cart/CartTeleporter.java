package com.sk89q.craftbook.cart;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;


public class CartTeleporter extends CartMechanism {
    @Override
    public void impact(Minecart cart, CartMechanismBlocks blocks, boolean minor) {
        // validate
        if (cart == null) return;
        if (blocks.sign == null) return;

        // go
        World world = cart.getWorld();
        String line = blocks.getSign().getLine(2);
        String[] pts = line.split(",");
        if (pts.length != 3) return;
        if (!blocks.getSign().getLine(3).equals("")) {
            world = cart.getServer().getWorld(blocks.getSign().getLine(3));
        }

        Double x = new Double(0D);
        Double y = new Double(0D);
        Double z = new Double(0D);
        try {
            x = Double.parseDouble(pts[0]);
            y = Double.parseDouble(pts[1]);
            z = Double.parseDouble(pts[2]);
        } catch (NumberFormatException e) {
            // incorrect format, just set them still and let them figure it out
            if (blocks.from != null) {
                x = blocks.from.getLocation().getX();
                y = blocks.from.getLocation().getY();
                z = blocks.from.getLocation().getZ();
            } else {
                x = (double) blocks.rail.getX();
                y = (double) blocks.rail.getY();
                z = (double) blocks.rail.getZ();
            }
            cart.setVelocity(new Vector(0D, 0D, 0D));
        }

        Location loc = com.sk89q.worldedit.bukkit.BukkitUtil.center(new Location(world, x, y, z, 0, 0) {});
        if(!loc.getChunk().isLoaded())
            loc.getChunk().load(true);
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
            toCart.setVelocity(cart.getVelocity()); // speedy thing goes in, speedy thing comes out <- Nice portal quote :)
            cart.remove();
        }
    }

    @Override
    public void enter(Minecart cart, Entity entity, CartMechanismBlocks blocks,
            boolean minor) {

    }
}