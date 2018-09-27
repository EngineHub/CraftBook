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

import org.bukkit.Location;
import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.SignUtil;

public class LightSensor extends AbstractSelfTriggeredIC {

    public LightSensor(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
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

        if (chip.getInput(0)) {
            chip.setOutput(0, hasLight());
        }
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, hasLight());
    }

    @Override
    public void load() {

        if(!getLine(3).isEmpty())
            centre = ICUtil.parseBlockLocation(getSign(), 3).getLocation();
        else
            centre = SignUtil.getBackBlock(CraftBookBukkitUtil.toSign(getSign()).getBlock()).getLocation().add(0, 1, 0);

        try {
            min = Byte.parseByte(getSign().getLine(2));
        } catch (Exception e) {
            min = 10;
            try {
                getSign().setLine(2, Integer.toString(min));
                getSign().update(false);
            } catch (Exception ignored) {
            }
        }
    }

    Location centre;
    byte min;

    /**
     * Returns true if the sign has a light level above the specified.
     *
     * @return
     */
    private boolean hasLight() {

        byte lightLevel = centre.getBlock().getLightLevel();

        return lightLevel >= min;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new LightSensor(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Outputs high if specific block is above specified light level.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"minimum light", "x:y:z offset"};
        }
    }
}
