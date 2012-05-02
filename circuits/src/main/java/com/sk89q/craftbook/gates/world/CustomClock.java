package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class CustomClock extends AbstractIC implements SelfTriggeredIC{

	boolean risingEdge;
	int tickRate = 0;
	
    public CustomClock(Server server, Sign sign) {
        super(server, sign);
    }

    @Override
    public String getTitle() {
        return "Self-triggered Custom Clock";
    }

    @Override
    public String getSignTitle() {
        return "ST CUST CLOCK";
    }

    @Override
    public void think(ChipState chip) {
    	chip.setOutput(0, tick());
    }
    
    public boolean tick()
    {
    	if(tickRate>=Integer.parseInt(getSign().getLine(2)))
    	{
    		tickRate = 0;
    		return true;
    	}
    	tickRate++;
    	return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new CustomClock(getServer(), sign);
        }
    }

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public void trigger(ChipState chip) {}

	@Override
	public void unload() {
		
	}
}
