package com.sk89q.craftbook.gates.weather;

import net.minecraft.server.Packet70Bed;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;
import com.sk89q.craftbook.util.SignUtil;

/**
 * @author Me4502
 */
public class WeatherFaker extends AbstractIC implements SelfTriggeredIC {

    public WeatherFaker(Server server, Sign sign, ICFactory factory) {

        super(server, sign, factory);
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

            return new WeatherFaker(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Fakes a players weather in radius.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "radius",
                    null
            };
            return lines;
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