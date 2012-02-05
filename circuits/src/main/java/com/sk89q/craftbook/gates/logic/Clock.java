// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.craftbook.gates.logic;

import org.bukkit.Server;
import org.bukkit.block.Sign;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICVerificationException;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class Clock extends AbstractIC implements SelfTriggeredIC{

	Sign sign;
	
    public Clock(Server server, Sign psign) {
        super(server, psign);
        sign = psign;
    }

    @Override
    public String getTitle() {
        return "Clock";
    }

    @Override
    public String getSignTitle() {
        return "CLOCK";
    }

    @Override
    public void trigger(ChipState chip) {}
    
    @Override
    public void think(ChipState chip) {

    	int tick, reset;
    	try
    	{
    		reset = Integer.parseInt(sign.getLine(2));
    	} catch (NumberFormatException e) {
    		return;
		}
    	
    	try
    	{
    		tick = Integer.parseInt(sign.getLine(3));
    	} catch (NumberFormatException e) {
    		tick = 0;
		}
    	
    	tick ++;
    	
    	if (tick == reset) {
    		tick = 0;
    		chip.setOutput(0, !chip.getOutput(0));
    	}
    	
    	// don't update, would cause lag!
    	sign.setLine(3, Integer.toString(tick));    	
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
            return new Clock(getServer(), sign);
        }
        
        @Override
        public void verify(Sign sign) throws ICVerificationException 
        {
        	int lol = -1;
        	try
        	{
        		lol = Integer.parseInt(sign.getLine(2));
        	} catch (NumberFormatException e) {
				throw new ICVerificationException("The fourth line must be a number between 5 and 15.");
			}
        	
        	lol = Math.max(lol, 5);
        	lol = Math.min(lol, 50);
        	
        	sign.setLine(2, Integer.toString(lol));
        	sign.setLine(3, "0");
        }
    }

}
