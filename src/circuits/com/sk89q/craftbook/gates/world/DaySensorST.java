// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;
import com.sk89q.craftbook.ic.*;

public class DaySensorST extends AbstractIC implements SelfTriggeredIC {

    protected boolean risingEdge;

    public DaySensorST(Server server, Sign sign) {
        super(server, sign);
    }

    @Override
    public String getTitle() {
        return "Self-triggered Day Sensor";
    }

    @Override
    public String getSignTitle() {
        return "ST DAY SENSOR";
    }

    @Override
    public void think(ChipState chip) {
        if (risingEdge && chip.getInput(0)
                || (!risingEdge && !chip.getInput(0))) {
            chip.setOutput(0, isDay());
        }
    }

    /**
     * @return true if the current time is day.
     */
    private boolean isDay() {
        long time = getSign().getBlock().getWorld().getTime() % 24000;
        if (time < 0)
            time += 24000;
        return (time < 13000l);
    }

    public static class Factory extends AbstractICFactory {
        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new DaySensorST(getServer(), sign);
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
