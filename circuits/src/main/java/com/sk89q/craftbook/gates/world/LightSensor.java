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

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class LightSensor extends AbstractIC {

    public LightSensor(Server server, Sign sign) {

        super(server, sign);
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
            chip.setOutput(0, getTargetLighted());
        }
    }

    protected boolean getTargetLighted() {

        int x = 0;
        int y = 1;
        int z = 0;
        int min = 10;
        try {
            String[] st = getSign().getLine(3).split(":");
            if (st.length != 3) throw new Exception();
            x = Integer.parseInt(st[0]);
            y = Integer.parseInt(st[1]);
            z = Integer.parseInt(st[2]);
        } catch (Exception ignored) {
        }

        try {
            min = Integer.parseInt(getSign().getLine(2));
        } catch (Exception e) {
            getSign().setLine(2, Integer.toString(min));
            getSign().update();
        }

        return hasLight(min, x, y, z);
    }


    /**
     * Returns true if the sign has a light level above the specified.
     *
     * @return
     */
    private boolean hasLight(int specifiedLevel, int x, int y, int z) {

        Block signBlock = getSign().getBlock();
        Block backBlock = signBlock.getRelative(SignUtil.getBack(signBlock));
        int lightLevel = (int) getSign()
                .getWorld()
                .getBlockAt(backBlock.getX() + x,
                        backBlock.getY() + y,
                        backBlock.getZ() + z)
                .getLightLevel();

        return lightLevel >= specifiedLevel;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new LightSensor(getServer(), sign);
        }
    }

}
