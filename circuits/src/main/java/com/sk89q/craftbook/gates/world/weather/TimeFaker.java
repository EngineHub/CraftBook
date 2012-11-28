package com.sk89q.craftbook.gates.world.weather;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.SignUtil;
import net.minecraft.server.Packet4UpdateTime;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

/**
 * @author Me4502
 */
public class TimeFaker extends AbstractIC implements SelfTriggeredIC {

    public TimeFaker(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Time Faker";
    }

    @Override
    public String getSignTitle() {

        return "TIME FAKER";
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new TimeFaker(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Radius based fake time.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "radius",
                    "time"
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

        try {
            Block b = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());
            if (chip.getInput(0)) {
                int dist = Integer.parseInt(getSign().getLine(2));
                long time = Long.parseLong(getSign().getLine(3));
                ((CraftServer) getServer()).getHandle().sendPacketNearby(b.getX(), b.getY() + 1, b.getZ(), dist,
                        ((CraftWorld) BukkitUtil.toSign(getSign()).getWorld()).getHandle().dimension, new Packet4UpdateTime(time, time));
            }
        } catch (Exception e) {
            BukkitUtil.toSign(getSign()).getBlock().breakNaturally();
        }
    }
}