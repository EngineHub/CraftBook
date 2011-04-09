package com.sk89q.craftbook.cart;

import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.util.*;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.World;
import org.bukkit.Material;

import com.sk89q.craftbook.util.*;
import static com.sk89q.craftbook.cart.CartUtils.*;

public class CartTeleporter extends CartMechanism {
    public void impact(Minecart cart, Block entered, Block from) {
        Block thingy = entered.getFace(BlockFace.DOWN, 1);
        Block director = pickDirector(thingy, "teleport");
        if (director == null) return;

        World world = cart.getWorld();
        Sign sign = ((Sign) director.getState());
        String line = sign.getLine(2);
        String[] pts = line.split(",");
        if (pts.length != 3) return;
        if (!sign.getLine(3).equals("")) {
           world = cart.getServer().getWorld(sign.getLine(3));
        }

        Double x = new Double(0D);
        Double y = new Double(0D);
        Double z = new Double(0D);
        try {
            x = Double.parseDouble(pts[0]);
            y = Double.parseDouble(pts[1]);
            z = Double.parseDouble(pts[2]);
        } catch (NumberFormatException e) {
            x = from.getLocation().getX();
            y = from.getLocation().getY();
            z = from.getLocation().getZ();
            cart.setVelocity(new Vector(0D, 0D, 0D));
        }
        Location loc = centerBlock(new Location(world, x, y, z));
        cart.teleport(loc);
    }
}
