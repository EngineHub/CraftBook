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

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.util.ICUtil;

public class LavaSensor extends AbstractSelfTriggeredIC {

    private Block center;

    public LavaSensor(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void load() {

        center = ICUtil.parseBlockLocation(getSign());
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
    public void think(ChipState chip) {

        chip.setOutput(0, hasLava());
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

        Material blockID = center.getType();

        return blockID == Material.LAVA;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new LavaSensor(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            ICUtil.verifySignSyntax(sign);
        }

        @Override
        public String getShortDescription() {

            return "Outputs high if lava is at given offset.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"x:y:z Offset", null};
        }
    }
}