package com.sk89q.craftbook.gates.weather;


import org.bukkit.Server;
import org.bukkit.block.Sign;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;

public class WeatherControlAdvanced extends AbstractIC {

    protected boolean risingEdge;

    public WeatherControlAdvanced(Server server, Sign sign, boolean risingEdge) {
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
    	
    	int duration = 24000;
    	int thunderDuration = duration;
    	try {
    		duration = Integer.parseInt(getSign().getLine(2));
    	} catch (Exception e) {}
    	try {
    		thunderDuration = Integer.parseInt(getSign().getLine(3));
    	} catch (Exception e) {}

    	if(duration > 24000) duration = 24000;
    	if(duration < 1) duration = 1;
    	
    	if(thunderDuration > 24000) thunderDuration = 24000;
    	if(thunderDuration < 1) thunderDuration = 1;

    	
    	if(chip.isTriggered(0) && chip.getInput(0)) {
    		boolean storm = chip.getInput(1);
    		boolean tstorm = chip.getInput(2);
    		getSign().getWorld().setStorm(storm);
    		if(storm) getSign().getWorld().setWeatherDuration(duration);
    		getSign().getWorld().setThundering(tstorm);
    		if(tstorm) getSign().getWorld().setThunderDuration(thunderDuration);
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
            return new WeatherControlAdvanced(getServer(), sign, risingEdge);
        }
    }

}
