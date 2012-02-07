package com.sk89q.craftbook.gates.weather;


import org.bukkit.Server;
import org.bukkit.block.Sign;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;

public class WeatherControl extends AbstractIC {

    protected boolean risingEdge;

    public WeatherControl(Server server, Sign sign, boolean risingEdge) {
        super(server, sign);
        this.risingEdge = risingEdge;
    }

    @Override
    public String getTitle() {
        return "Weather Control";
    }

    @Override
    public String getSignTitle() {
        return "WEATHER CONTROL";
    }

    @Override
    public void trigger(ChipState chip) {
    	
    	boolean tstorm = false;
    	int duration = 24000;
    	int thunderDuration = duration;
    	try {
    		String[] st = getSign().getLine(1).split("]");
    		if(st.length > 1) tstorm = st[1].equalsIgnoreCase("r");
    		duration = Integer.parseInt(getSign().getLine(2));
    	} catch (Exception e) {}
    	try {
    		thunderDuration = Integer.parseInt(getSign().getLine(3));
    	} catch (Exception e) {}

    	if(duration > 24000) duration = 24000;
    	if(duration < 1) duration = 1;
    	
    	if(thunderDuration > 24000) thunderDuration = 24000;
    	if(thunderDuration < 1) thunderDuration = 1;

    	
    	if(chip.getInput(0)) {
    		getSign().getWorld().setStorm(true);
    		getSign().getWorld().setWeatherDuration(duration);
    		if(tstorm) {
    			getSign().getWorld().setThundering(true);
    			getSign().getWorld().setThunderDuration(thunderDuration);
    		}
        	chip.setOutput(0, true);
        }
    	else {
			getSign().getWorld().setThundering(false);
    		getSign().getWorld().setStorm(false);
    		chip.setOutput(0, false);
    	}
    }


    public static class Factory extends AbstractICFactory {

        protected boolean risingEdge;

        public Factory(Server server, boolean risingEdge) {
            super(server);
            this.risingEdge = risingEdge;
        }

        @Override
        public IC create(Sign sign) {
            return new WeatherControl(getServer(), sign, risingEdge);
        }
    }

}
