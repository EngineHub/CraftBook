package com.sk89q.craftbook.gates.world.weather;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.SignUtil;
import net.minecraft.server.v1_4_6.Packet70Bed;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_4_6.CraftServer;
import org.bukkit.craftbukkit.v1_4_6.CraftWorld;

/**
 * @author Me4502
 */
public class WeatherFaker extends AbstractIC implements SelfTriggeredIC {

    public WeatherFaker(Server server, ChangedSign sign, ICFactory factory) {

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
        public IC create(ChangedSign sign) {

            return new WeatherFaker(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Fakes a players weather in radius.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"radius", null};
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

        try {
            Block b = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());
            if (chip.getInput(0)) {
                int dist = Integer.parseInt(getSign().getLine(2));
                if (!BukkitUtil.toSign(getSign()).getWorld().hasStorm()) {
                    ((CraftServer) getServer()).getHandle().sendPacketNearby(b.getX(), b.getY() + 1, b.getZ(), dist + 2,
                            ((CraftWorld) BukkitUtil.toSign(getSign()).getWorld()).getHandle().dimension,
                            new Packet70Bed(2, 0));
                    ((CraftServer) getServer()).getHandle().sendPacketNearby(b.getX(), b.getY() + 1, b.getZ(), dist,
                            ((CraftWorld) BukkitUtil.toSign(getSign()).getWorld()).getHandle().dimension,
                            new Packet70Bed(1, 0));
                }
            } else if (!chip.getInput(0)) {
                int dist = Integer.parseInt(getSign().getLine(2));
                if (!BukkitUtil.toSign(getSign()).getWorld().hasStorm()) {
                    ((CraftServer) getServer()).getHandle().sendPacketNearby(b.getX(), b.getY() + 1, b.getZ(), dist,
                            ((CraftWorld) BukkitUtil.toSign(getSign()).getWorld()).getHandle().dimension,
                            new Packet70Bed(2, 0));
                } else {
                    ((CraftServer) getServer()).getHandle().sendPacketNearby(b.getX(), b.getY() + 1, b.getZ(), dist + 2,
                            ((CraftWorld) BukkitUtil.toSign(getSign()).getWorld()).getHandle().dimension,
                            new Packet70Bed(1, 0));
                    ((CraftServer) getServer()).getHandle().sendPacketNearby(b.getX(), b.getY() + 1, b.getZ(), dist,
                            ((CraftWorld) BukkitUtil.toSign(getSign()).getWorld()).getHandle().dimension,
                            new Packet70Bed(2, 0));
                }
            }
        } catch (Throwable ignored) {
        }
    }
}