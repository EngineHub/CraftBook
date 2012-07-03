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
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;
import com.sk89q.craftbook.util.SignUtil;

public class TimeFaker extends AbstractIC implements SelfTriggeredIC{

    public TimeFaker(Server server, Sign sign) {
        super(server, sign);
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
            return new TimeFaker(getServer(), sign);
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
        if(chip.getInput(0))
        {
            int dist = Integer.parseInt(getSign().getLine(2));
            long time = Long.parseLong(getSign().getLine(3));
            ((CraftServer)getServer()).getHandle().sendPacketNearby(b.getX(), b.getY()+1,b.getZ(), dist, ((CraftWorld) getSign().getWorld()).getHandle().dimension, new Packet4UpdateTime(time));
        }
    }
}