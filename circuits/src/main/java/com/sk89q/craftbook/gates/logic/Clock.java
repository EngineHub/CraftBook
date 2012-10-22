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
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICVerificationException;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class Clock extends AbstractIC implements SelfTriggeredIC {

    final Sign sign;

    public Clock(Server server, Sign psign, ICFactory factory) {

        super(server, psign, factory);
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
    public void trigger(ChipState chip) {

    }

    protected void triggerClock(ChipState chip) {

        short tick, reset;
        try {
            reset = Short.parseShort(sign.getLine(2));
        } catch (NumberFormatException e) {
            return;
        }

        try {
            tick = Short.parseShort(sign.getLine(3));
        } catch (NumberFormatException e) {
            tick = 0;
        }

        tick++;

        if (tick == reset) {
            tick = 0;
            chip.setOutput(0, !chip.getOutput(0));
        }

        sign.setLine(3, Short.toString(tick));
        //sign.update(); Laggggy
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new Clock(getServer(), sign, this);
        }

        @Override
        public void verify(Sign sign) throws ICVerificationException {

            int lol;
            try {
                lol = Integer.parseInt(sign.getLine(2));
            } catch (NumberFormatException e) {
                throw new ICVerificationException("The third line must be a number between 5 and 150.");
            }

            lol = Math.max(lol, 5);
            lol = Math.min(lol, 150);

            sign.setLine(2, Integer.toString(lol));
            sign.setLine(3, "0");
            sign.update();
        }

        @Override
        public String getDescription() {

            return "Outputs hight every X ticks when input is high.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "ticks required",
                    "current ticks"
            };
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