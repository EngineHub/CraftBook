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

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;

import java.util.regex.Pattern;

public class Marquee extends AbstractIC {

    private static final Pattern RIGHT_BRACKET_PATTERN = Pattern.compile("]", Pattern.LITERAL);

    public Marquee(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Marquee";
    }

    @Override
    public String getSignTitle() {

        return "MARQUEE";
    }

    @Override
    public void trigger(ChipState chip) {

        if (!chip.getInput(0)) return;

        boolean reverse = false;
        int next = 0;
        try {
            String[] st = RIGHT_BRACKET_PATTERN.split(getSign().getLine(1), 2);
            if (st.length > 1) {
                reverse = st[1].equalsIgnoreCase("r");
            }
            next = Integer.parseInt(getSign().getLine(2));
        } catch (Exception ignored) {
        }

        if (next == 0) {
            next = reverse ? 3 : 1;
        }
        for (short i = 0; i < chip.getOutputCount(); i++) {
            chip.setOutput(i, false); // Clear all pins
        }

        switch (next) {
            case 1:
                chip.setOutput(1, true);
                break;
            case 2:
                chip.setOutput(0, true);
                break;
            case 3:
                chip.setOutput(2, true);
                break;
        }


        if (reverse) {
            next--;
        } else {
            next++;
        }

        if (next == 0) {
            next = 3;
        } else if (next == 4) {
            next = 1;
        }

        // set the next output and update sign
        getSign().setLine(2, Integer.toString(next));

    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Marquee(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Sequentially sets all pins.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "next pin to set",
                    null
            };
            return lines;
        }
    }
}