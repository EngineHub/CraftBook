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

import java.util.Random;
import org.bukkit.Server;
import org.bukkit.block.Sign;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;

public class ClockDivider extends AbstractIC {
    
    protected Random random = new Random();

    public ClockDivider(Server server, Sign sign) {
        super(server, sign);
    }

    @Override
    public String getTitle() {
        return "Clock Divider";
    }

    @Override
    public String getSignTitle() {
        return "CLOCK DIVIDER";
    }

    @Override
    public void trigger(ChipState chip) {
    	int reset = 0;
    	int count = 0;
    	
    	try {
    		reset = Integer.parseInt(getSign().getLine(2));
    	} catch (Exception e) {}
    	try {
    		count = Integer.parseInt(getSign().getLine(3));
    	} catch (Exception e) {}
    	if(reset < 2) reset=2;
    	if(reset > 128) reset=128;
    	
    	// toggled, so increment count
		count++;

    	// check if counter is about to reset, if it isn't, save and return
    	if(count < reset) {
    		getSign().setLine(3, Integer.toString(count));
    		return;
    	}
    	// if time to reset, toggle state
    	chip.setOutput(0, !(chip.getOutput(0)));
    	// reset count
    	count = 0;
    	getSign().setLine(3, Integer.toString(count));
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new ClockDivider(getServer(), sign);
        }
    }

}
