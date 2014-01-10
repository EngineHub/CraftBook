package com.sk89q.craftbook.vehicles.cart.blocks;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.CartUtil;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.vehicles.cart.events.CartBlockImpactEvent;

public class CartTeleporter extends CartBlockMechanism {

    public CartTeleporter (ItemInfo material) {
        super(material);
    }

    @EventHandler
    public void onVehicleImpact(CartBlockImpactEvent event) {
        // validate
        if (!event.getBlocks().matches(getMaterial())) return;
        if (!event.getBlocks().hasSign()) return;
        if (event.isMinor()) return;
        if (!event.getBlocks().matches("teleport")) return;

        // go
        World world = event.getMinecart().getWorld();
        String[] pts = RegexUtil.COMMA_PATTERN.split(event.getBlocks().getSign().getLine(2).trim(), 3);
        if (!event.getBlocks().getSign().getLine(3).trim().isEmpty()) {
            world = event.getMinecart().getServer().getWorld(event.getBlocks().getSign().getLine(3).trim());
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
            if (event.getBlocks().from != null) {
                x = event.getBlocks().from.getX();
                y = event.getBlocks().from.getY();
                z = event.getBlocks().from.getZ();
            } else {
                x = event.getBlocks().rail.getX();
                y = event.getBlocks().rail.getY();
                z = event.getBlocks().rail.getZ();
            }
            CartUtil.stop(event.getMinecart());
        }

        Location loc = BukkitUtil.center(new Location(world, x, y, z, event.getMinecart().getLocation().getYaw(), event.getMinecart().getLocation().getPitch()));
        loc.getChunk().load(true);
        CartUtil.teleport(event.getMinecart(), loc);
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