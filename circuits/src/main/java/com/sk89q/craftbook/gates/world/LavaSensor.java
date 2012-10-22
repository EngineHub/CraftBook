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

import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class LavaSensor extends AbstractIC {

    private Block center;

    public LavaSensor(Server server, Sign sign, ICFactory factory) {

        super(server, sign, factory);
        try {
            center = ICUtil.parseBlockLocation(sign);
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getTitle() {

        return "Lava Sensor";
    }

    @Override
    public String getSignTitle() {

        return "LAVA SENSOR";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, hasLava());
        }
    }

    /**
     * Returns true if the sign has lava at the specified location.
     *
     * @return
     */
    protected boolean hasLava() {

        int blockID = center.getTypeId();

        return blockID == 10 || blockID == 11;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new LavaSensor(getServer(), sign, this);
        }

        @Override
        public void verify(Sign sign) throws ICVerificationException {

            ICUtil.verifySignSyntax(sign);
        }
    }

}
