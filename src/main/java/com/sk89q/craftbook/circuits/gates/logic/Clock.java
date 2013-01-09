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

package com.sk89q.craftbook.circuits.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.circuits.ic.SelfTriggeredIC;

public class Clock extends AbstractIC implements SelfTriggeredIC {

    public Clock(Server server, ChangedSign psign, ICFactory factory) {

        super(server, psign, factory);
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
    public void trigger(ChipState chip) {

    }

    short tick, reset;

    protected void triggerClock(ChipState chip) {

        try {
            reset = Short.parseShort(getSign().getLine(2));
        } catch (NumberFormatException e) {
            reset = 5;
            getSign().setLine(2, Short.toString(reset));
            getSign().update(false);
        }

        try {
            tick = Short.parseShort(getSign().getLine(3));
        } catch (NumberFormatException e) {
            tick = 0;
            getSign().setLine(3, Short.toString(tick));
            getSign().update(false);
        }

        tick++;

        if (tick == reset) {
            tick = 0;
            chip.setOutput(0, !chip.getOutput(0));
        }

        getSign().setLine(3, Short.toString(tick));
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Clock(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            int interval;
            try {
                interval = Integer.parseInt(sign.getLine(2));
            } catch (NumberFormatException e) {
                throw new ICVerificationException("The third line must be a number between 5 and 150.");
            }

            interval = Math.max(interval, 5);
            interval = Math.min(interval, 150);

            sign.setLine(2, Integer.toString(interval));
            sign.setLine(3, "0");
            sign.update(false);
        }

        @Override
        public String getShortDescription() {

            return "Outputs high every X ticks when input is high.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"ticks required", "current ticks"};
            return lines;
        }
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public void think(ChipState chip) {

        if (chip.getInput(0)) {
            triggerClock(chip);
        }
    }
}