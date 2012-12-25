// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.gates.world.sensors;

import org.bukkit.Server;
import org.bukkit.block.Block;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.util.SignUtil;

public class LightSensor extends AbstractIC {

    public LightSensor (Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle () {

        return "Light Sensor";
    }

    @Override
    public String getSignTitle () {

        return "LIGHT SENSOR";
    }

    @Override
    public void trigger (ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, getTargetLighted());
        }
    }

    @Override
    public void load () {
        try {
            String[] st = ICUtil.COLON_PATTERN.split(getSign().getLine(3));
            if (st.length != 3) throw new Exception();
            x = Integer.parseInt(st[0]);
            y = Integer.parseInt(st[1]);
            z = Integer.parseInt(st[2]);
        } catch (Exception ignored) {
            x = 0;
            y = 1;
            z = 0;
        }

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

    int x;
    int y;
    int z;
    byte min;

    protected boolean getTargetLighted () {

        return hasLight(min, x, y, z);
    }

    /**
     * Returns true if the sign has a light level above the specified.
     * 
     * @return
     */
    private boolean hasLight (byte specifiedLevel, int x, int y, int z) {

        Block signBlock = BukkitUtil.toSign(getSign()).getBlock();
        Block backBlock = signBlock.getRelative(SignUtil.getBack(signBlock));
        byte lightLevel = backBlock.getRelative(x, y, z).getLightLevel();

        return lightLevel >= specifiedLevel;
    }

    public static class Factory extends AbstractICFactory {

        public Factory (Server server) {

            super(server);
        }

        @Override
        public IC create (ChangedSign sign) {

            return new LightSensor(getServer(), sign, this);
        }

        @Override
        public String getDescription () {

            return "Outputs high if specific block is above specified light level.";
        }

        @Override
        public String[] getLineHelp () {

            String[] lines = new String[] { "minimum light", "x:y:z offset" };
            return lines;
        }
    }
}