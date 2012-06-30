package com.sk89q.craftbook.gates.weather;


import org.bukkit.Server;
import org.bukkit.block.Sign;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class RainSensorST extends AbstractIC implements SelfTriggeredIC {

    public RainSensorST(Server server, Sign sign) {
        super(server, sign);
    }

    @Override
    public String getTitle() {
        return "Is It Rain";
    }

    @Override
    public String getSignTitle() {
        return "IS IT RAIN";
    }
    
    @Override
    public void trigger(ChipState chip)
    {}
    
    @Override
    public void think(ChipState chip) {
        	chip.setOutput(0, getSign().getWorld().hasStorm());
    }
    
	@Override
	public boolean isActive() {
		return true;
	}


    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new RainSensorST(getServer(), sign);
        }
    }

}
