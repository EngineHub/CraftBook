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
import com.sk89q.craftbook.ic.SelfTriggeredIC;
import com.sk89q.craftbook.util.SignUtil;

public class WeatherFaker extends AbstractIC implements SelfTriggeredIC{

    protected boolean risingEdge;

    public WeatherFaker(Server server, Sign sign, boolean risingEdge) {
        super(server, sign);
        this.risingEdge = risingEdge;
    }

	@Override
	public String getTitle() {
		return "Weather Faker";
	}

	@Override
	public String getSignTitle() {
		return "WEATHER FAKER";
	}

    public static class Factory extends AbstractICFactory {

        protected boolean risingEdge;

        public Factory(Server server, boolean risingEdge) {
            super(server);
            this.risingEdge = risingEdge;
        }

        @Override
        public IC create(Sign sign) {
            return new WeatherFaker(getServer(), sign, risingEdge);
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
			if(!getSign().getWorld().hasStorm())
				((CraftServer)getServer()).getHandle().sendPacketNearby(b.getX(), b.getY()+1,b.getZ(), dist+2, ((CraftWorld) getSign().getWorld()).getHandle().dimension, new Packet70Bed(2,0));
    		((CraftServer)getServer()).getHandle().sendPacketNearby(b.getX(), b.getY()+1,b.getZ(), dist, ((CraftWorld) getSign().getWorld()).getHandle().dimension, new Packet70Bed(1,0));
		}
		else if(!chip.getInput(0))
		{
			int dist = Integer.parseInt(getSign().getLine(2));
			if(!getSign().getWorld().hasStorm())
				((CraftServer)getServer()).getHandle().sendPacketNearby(b.getX(), b.getY()+1,b.getZ(), dist, ((CraftWorld) getSign().getWorld()).getHandle().dimension, new Packet70Bed(2,0));
			else
			{
				((CraftServer)getServer()).getHandle().sendPacketNearby(b.getX(), b.getY()+1,b.getZ(), dist+2, ((CraftWorld) getSign().getWorld()).getHandle().dimension, new Packet70Bed(1,0));
				((CraftServer)getServer()).getHandle().sendPacketNearby(b.getX(), b.getY()+1,b.getZ(), dist, ((CraftWorld) getSign().getWorld()).getHandle().dimension, new Packet70Bed(2,0));
			}
		}
	}
}