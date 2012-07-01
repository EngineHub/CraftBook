package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class RangedOutput extends AbstractIC implements SelfTriggeredIC {

	int ticks = 0;
	int maxTicks = 0;
	boolean hasStarted = false;
	int amountDone = 0;
	int maxAmount = 0;

    public RangedOutput(Server server, Sign sign) {
        super(server, sign);
    }

    @Override
    public String getTitle() {
        return "Ranged Output";
    }

    @Override
    public String getSignTitle() {
        return "RANGE OUTPUT";
    }
    

    @Override
    public void think(ChipState chip) {
    	chip.setOutput(0, shouldOutput(chip));
    }

    protected boolean shouldOutput(ChipState chip) {
        if (chip.getInput(0)) {
            int min = Integer.parseInt(getSign().getLine(2).split("-")[0]);
            int max = Integer.parseInt(getSign().getLine(2).split("-")[1]);
            maxAmount = min + (int)(Math.random() * ((max - min) + 1));
            amountDone = 0;
            ticks = 0;
            if(getSign().getLine(3)!=null || getSign().getLine(3).equals(""))
            	maxTicks = Integer.parseInt(getSign().getLine(3));
            else
            	maxTicks = 10;
            hasStarted = true;
            return false;
        }
        else if(hasStarted)
        {
        	if(ticks>=maxTicks)
        	{
        		ticks = 0;
        		amountDone++;
        		if(amountDone>=maxAmount)
        		{
        			hasStarted = false;
        			amountDone = 0;
                    ticks = 0;
                    maxAmount = 0;
        		}
        		return true;
        	}
        	else
        	{
        		ticks++;
        		return false;
        	}
        }
        return false;
    }

    public static class Factory extends AbstractICFactory {
        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new RangedOutput(getServer(), sign);
        }
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void trigger(ChipState chip) {
    }
}
