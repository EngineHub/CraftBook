/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
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

package org.enginehub.craftbook.mechanics.ic.gates.logic;

import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Server;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.ConfigurableIC;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;

public class Clock extends AbstractSelfTriggeredIC {

    public Clock(Server server, ChangedSign psign, ICFactory factory) {

        super(server, psign, factory);
    }

    @Override
    public boolean isAlwaysST() {

        return true;
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

    @Override
    public void think(ChipState chip) {

        if (((Factory) getFactory()).inverted == chip.getInput(0)) {
            triggerClock(chip);
        }
    }

    short tick, reset;

    protected void triggerClock(ChipState chip) {

        tick++;

        if (tick == reset) {
            tick = 0;
            chip.setOutput(0, !chip.getOutput(0));
        }

        getSign().setLine(3, Short.toString(tick));
        getSign().update(false);
    }

    @Override
    public void load() {
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
    }

    public static class Factory extends AbstractICFactory implements ConfigurableIC {

        public boolean inverted = false;

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
                throw new ICVerificationException("The third line must be a number between 5 and 100000.");
            }

            interval = Math.max(Math.min(interval, 100000), 5);

            sign.setLine(2, Integer.toString(interval));

            int tick;
            try {
                tick = Integer.parseInt(sign.getLine(3));
            } catch (NumberFormatException e) {
                tick = 0;
            }

            tick = Math.max(tick, 0);
            tick = Math.min(tick, interval);

            sign.setLine(3, Integer.toString(tick));
            sign.update(false);
        }

        @Override
        public String getShortDescription() {

            return "Outputs high every X ticks when input is high.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "ticks required", "current ticks" };
        }

        @Override
        public void addConfiguration(YAMLProcessor config, String path) {

            inverted = config.getBoolean("inverted", false);
        }
    }
}