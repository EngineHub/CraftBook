package com.sk89q.craftbook.gates.weather;

import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.SignUtil;
import net.minecraft.server.Packet70Bed;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

/**
 * @author Me4502
 */
public class WeatherFaker extends AbstractIC implements SelfTriggeredIC {

    public WeatherFaker(Server server, Sign sign) {

        super(server, sign);
    }

    @Override
    public String getTitle() {

        return "Weather Faker";
    }

    @Override
    public String getSignTitle() {

        return "WEATHER FAKER";
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new WeatherFaker(getServer(), sign);
        }
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public void trigger(ChipState chip) {

    }

    @Override
    public void think(ChipState chip) {

        Block b = SignUtil.getBackBlock(getSign().getBlock());
        if (chip.getInput(0)) {
            int dist = Integer.parseInt(getSign().getLine(2));
            if (!getSign().getWorld().hasStorm()) {
                ((CraftServer) getServer()).getHandle().sendPacketNearby(b.getX(), b.getY() + 1, b.getZ(), dist + 2,
                        ((CraftWorld) getSign().getWorld()).getHandle().dimension, new Packet70Bed(2, 0));
                ((CraftServer) getServer()).getHandle().sendPacketNearby(b.getX(), b.getY() + 1, b.getZ(), dist,
                        ((CraftWorld) getSign().getWorld()).getHandle().dimension, new Packet70Bed(1, 0));
            }
        } else if (!chip.getInput(0)) {
            int dist = Integer.parseInt(getSign().getLine(2));
            if (!getSign().getWorld().hasStorm()) {
                ((CraftServer) getServer()).getHandle().sendPacketNearby(b.getX(), b.getY() + 1, b.getZ(), dist,
                        ((CraftWorld) getSign().getWorld()).getHandle().dimension, new Packet70Bed(2, 0));
            } else {
                ((CraftServer) getServer()).getHandle().sendPacketNearby(b.getX(), b.getY() + 1, b.getZ(), dist + 2,
                        ((CraftWorld) getSign().getWorld()).getHandle().dimension, new Packet70Bed(1, 0));
                ((CraftServer) getServer()).getHandle().sendPacketNearby(b.getX(), b.getY() + 1, b.getZ(), dist,
                        ((CraftWorld) getSign().getWorld()).getHandle().dimension, new Packet70Bed(2, 0));
            }
        }
    }
}