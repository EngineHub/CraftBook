package com.sk89q.craftbook.gates.weather;

import net.minecraft.server.Packet70Bed;

import org.bukkit.Server;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

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
    	if(chip.getInput(1))
    	{
	    	int dist = Integer.parseInt(getSign().getLine(2));

	    	for(Player player: getServer().getOnlinePlayers())
	        {
	    		if(player.getLocation().distance(getSign().getLocation())<=dist)
	    		{
	    			((CraftPlayer)player).getHandle().netServerHandler.sendPacket(new Packet70Bed(1,0));
	    		}
	        }
    	}
    	else if(!chip.getInput(1))
    	{
	    	int dist = Integer.parseInt(getSign().getLine(2));

	    	for(Player player: getServer().getOnlinePlayers())
	        {
	    		if(player.getLocation().distance(getSign().getLocation())<=dist)
	    		{
	    			((CraftPlayer)player).getHandle().netServerHandler.sendPacket(new Packet70Bed(2,0));
	    		}
	        }
    	}
	}
}