// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.mechanics.ic.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;

public class ClockDivider extends AbstractIC {

    public ClockDivider(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
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
        } catch (Exception ignored) {
        }
        try {
            count = Integer.parseInt(getSign().getLine(3));
        } catch (Exception ignored) {
        }
        reset = Math.min(100000, Math.max(1, reset));

        // toggled, so increment count
        count++;

        // check if counter is about to reset, if it isn't, save and return
        if (count < reset) {
            getSign().setLine(3, Integer.toString(count));
            return;
        }
        // if time to reset, toggle state
        chip.setOutput(0, !chip.getOutput(0));
        // reset count
        count = 0;
        getSign().setLine(3, Integer.toString(count));
        getSign().update(false);
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ClockDivider(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Clock that toggles output when reset.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"ticks required", "current ticks"};
        }
    }
}