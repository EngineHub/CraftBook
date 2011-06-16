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

package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;

public class LightSensor extends AbstractIC {

    protected boolean risingEdge;

    public LightSensor(Server server, Sign sign, boolean risingEdge) {
        super(server, sign);
        this.risingEdge = risingEdge;
    }

    @Override
    public String getTitle() {
        return "Light Sensor";
    }

    @Override
    public String getSignTitle() {
        return "LIGHT SENSOR";
    }

    @Override
    public void trigger(ChipState chip) {
        if (risingEdge && chip.getInput(0) || (!risingEdge && !chip.getInput(0))) {
            chip.setOutput(0, hasLight());
        }
    }

    /**
     * Returns true if the sign has a light level above the specified.
     * 
     * @return
     */
    private boolean hasLight() {
        int x = getSign().getBlock().getLocation().getBlockX();
        int y = getSign().getBlock().getLocation().getBlockY();
        int z = getSign().getBlock().getLocation().getBlockZ();
        int yOffset = 1;
        try {
            // Get Y offset from (the otherwise unused) line 4.
            // This makes LightSensor consistent with the capabilities of LavaSensor and WaterSensor.
            String yOffsetLine = getSign().getLine(3);
            if (yOffsetLine.length() > 0) {
                yOffset = Integer.parseInt(yOffsetLine);
            }
        } catch (NumberFormatException e) {
            yOffset = 1;
        }

        int lightLevel = (int)getSign().getBlock().getRelative(0, yOffset, 0).getLightLevel();
        int specifiedLevel = 0;
        try {
            String specified = getSign().getLine(2);
            if (specified.length() > 0) {
                specifiedLevel = Integer.parseInt(specified);
            }
        } catch (NumberFormatException e) {
            // eat the exception.
        }

        return lightLevel >= specifiedLevel;
    }

    public static class Factory extends AbstractICFactory {

        protected boolean risingEdge;

        public Factory(Server server, boolean risingEdge) {
            super(server);
            this.risingEdge = risingEdge;
        }

        @Override
        public IC create(Sign sign) {
            return new LightSensor(getServer(), sign, risingEdge);
        }
    }

}
