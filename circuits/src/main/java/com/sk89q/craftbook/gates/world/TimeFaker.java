package com.sk89q.craftbook.gates.world;

import net.minecraft.server.Packet4UpdateTime;

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
public class TimeFaker extends AbstractIC implements SelfTriggeredIC {

    public TimeFaker(Server server, Sign sign, ICFactory factory) {

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
        public IC create(Sign sign) {

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
            Block b = SignUtil.getBackBlock(getSign().getBlock());
            if (chip.getInput(0)) {
                int dist = Integer.parseInt(getSign().getLine(2));
                long time = Long.parseLong(getSign().getLine(3));
                ((CraftServer) getServer()).getHandle().sendPacketNearby(b.getX(), b.getY() + 1, b.getZ(), dist,
                        ((CraftWorld) getSign().getWorld()).getHandle().dimension, new Packet4UpdateTime(time));
            }
        } catch (Exception e) {
            getSign().getBlock().breakNaturally();
        }
    }
}