package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class DaySensorST extends AbstractIC implements
		SelfTriggeredIC {

	public DaySensorST(Server server, Sign block) {
		super(server, block);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getTitle() {
		return "Self Triggering Day Sensor";
	}

	@Override
	public String getSignTitle() {
		return "ST DAY SENSOR";
	}

	@Override
	public void trigger(ChipState chip) {
		//Empty ( No Outputs )
	}

	@Override
	public void think(ChipState state) {
			state.setOutput(0, isDay());
	}
	
    private boolean isDay() {
        long time = getSign().getBlock().getWorld().getTime() % 24000;
        if (time < 0)
            time += 24000;
        return (time < 13000l);
    }
	
	public static class Factory extends AbstractICFactory
	{

		public Factory(Server server) {
			super(server);
		}

		@Override
		public IC create(Sign sign) {
			return new DaySensorST(getServer(), sign);
		}
		
	}

}
