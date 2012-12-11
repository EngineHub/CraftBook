package com.sk89q.craftbook.cart;

import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
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
        if (cart.getWorld() == world && loc.getChunk().isLoaded() && loc.distanceSquared(cart.getLocation()) < 100*100) {
            cart.teleport(loc);
        } else {
            loc.getChunk().load(true);
            Minecart toCart = world.spawn(loc, Minecart.class);
            Entity passenger = cart.getPassenger();
            if (passenger != null) {
                cart.eject();
                passenger.teleport(loc);
                toCart.setPassenger(passenger);
            }
            toCart.getLocation().setYaw(cart.getLocation().getYaw());
            toCart.getLocation().setPitch(cart.getLocation().getPitch());
            toCart.setVelocity(cart.getVelocity()); // speedy thing goes in, speedy thing comes out
            cart.remove();
        }
    }

    @Override
    public boolean verify(ChangedSign sign, LocalPlayer player){

        String[] pts = COMMA_PATTERN.split(sign.getLine(2).trim(), 3);
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
    public String getName () {
        return "Teleporter";
    }

    @Override
    public String[] getApplicableSigns () {
        return new String[]{"Teleport"};
    }
}