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

import java.util.Locale;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.util.RegexUtil;

public class Monostable extends AbstractSelfTriggeredIC {

    // "Temp docs": nn:[HL] nn - time for pulse (1 = 2t) H: trigger on high L: trigger on low

    public Monostable(Server server, ChangedSign psign, ICFactory factory) {

        super(server, psign, factory);
    }

    @Override
    public String getTitle() {

        return "Monostable";
    }

    @Override
    public String getSignTitle() {

        return "MONOSTABLE";
    }

    @Override
    public boolean isAlwaysST() {

        return true;
    }

    @Override
    public void trigger(ChipState chip) {

        String setting = getSign().getLine(2).toUpperCase(Locale.ENGLISH);
        boolean triggered = chip.getInput(0);
        if (triggered && setting.contains("H") || !triggered && setting.contains("L")) {
            // Trigger condition!
            int colon = setting.indexOf(':');
            if (colon <= 0) return;

            chip.setOutput(0, true);
            getSign().setLine(3, setting.substring(0, colon));
            getSign().update(false);
        }
    }

    @Override
    public void think(ChipState chip) {

        int tick;

        try {
            tick = Integer.parseInt(getSign().getLine(3));
        } catch (NumberFormatException e) {
            tick = 0;
        }

        if (tick == 0) {
            chip.setOutput(0, false);
        } else {
            tick--;
        }

        getSign().setLine(3, Integer.toString(tick));
        getSign().update(false);
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Monostable(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Outputs a pulse for a set amount of time on high.";
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            int ticks;
            boolean hi;
            boolean lo;

            try {
                String set = sign.getLine(2).toUpperCase(Locale.ENGLISH);

                if (!set.contains(":")) throw new ICVerificationException("Invalid syntax");

                String[] settings = RegexUtil.COLON_PATTERN.split(set);

                if (settings.length != 2) throw new ICVerificationException("Invalid syntax");

                ticks = Integer.parseInt(settings[0]);

                hi = settings[1].contains("H");
                lo = settings[1].contains("L");
                if (!(hi || lo)) throw new ICVerificationException("Missing trigger levels");

            } catch (NumberFormatException e) {
                throw new ICVerificationException("Invalid number format");
            }

            ticks = Math.max(ticks, 2);
            ticks = Math.min(ticks, 6000);

            sign.setLine(2, ticks + ":" + (hi ? "H" : "") + (lo ? "L" : ""));
            sign.setLine(3, "0");
            sign.update(false);
        }
    }

}
