package com.sk89q.craftbook.mechanics.minecart.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.minecart.events.CartBlockImpactEvent;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.CartUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;

public class CartTeleporter extends CartBlockMechanism {

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

        Location loc = CraftBookBukkitUtil
                .center(new Location(world, x, y, z, event.getMinecart().getLocation().getYaw(), event.getMinecart().getLocation().getPitch()));
        loc.getChunk().load(true);
        CartUtil.teleport(event.getMinecart(), loc);
    }

    @Override
    public boolean verify(ChangedSign sign, CraftBookPlayer player) {

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

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "block", "Sets the block that is the base of the teleport mechanic.");
        material = BlockSyntax.getBlock(config.getString(path + "block", BlockTypes.EMERALD_BLOCK.getId()), true);
    }
}