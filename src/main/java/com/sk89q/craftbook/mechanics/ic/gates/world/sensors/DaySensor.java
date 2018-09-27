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

package com.sk89q.craftbook.mechanics.ic.gates.world.sensors;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;

public class DaySensor extends AbstractSelfTriggeredIC {

    public DaySensor(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Day Sensor";
    }

    @Override
    public String getSignTitle() {

        return "DAY SENSOR";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0))
            chip.setOutput(0, isDay());
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, isDay());
    }

    long day;
    long night;

    @Override
    public void load() {

        try {
            night = Long.parseLong(getSign().getLine(3));
        } catch (Exception ignored) {
            night = 13000L;
        }
        try {
            day = Long.parseLong(getSign().getLine(2));
        } catch (Exception ignored) {
            day = 0L;
        }
    }

    /**
     * Returns true if the current time is day.
     *
     * @return
     */
    protected boolean isDay() {

        long time = getBackBlock().getWorld().getTime();
        while (time < 0) {
            time += 24000;
        }
        while (time > 24000) {
            time -= 24000;
        }

        if (day < night) {
            return time >= day && time <= night;
        } else if (day > night) {
            return time <= day || time >= night;
        }
        return time < night;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new DaySensor(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Outputs high if it is day.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"custom day start", "custom night start (day end)"};
        }
    }
}