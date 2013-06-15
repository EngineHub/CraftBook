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

package com.sk89q.craftbook.circuits.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;

public class RandomBit extends AbstractSelfTriggeredIC {

    public RandomBit(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Random Bit";
    }

    @Override
    public String getSignTitle() {

        return "RANDOM BIT";
    }

    int maxOn;

    @Override
    public void load() {

        try {
            maxOn = Integer.parseInt(getLine(2));
        } catch(Exception e){
            maxOn = -1;
        }
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0))
            randomize(chip);
    }

    @Override
    public void think(ChipState chip) {

        if (chip.getInput(0))
            randomize(chip);
    }

    public void randomize(ChipState chip) {
        int on = 0;
        for (short i = 0; i < chip.getOutputCount(); i++) {
            boolean state = CraftBookPlugin.inst().getRandom().nextBoolean();
            if(on >= maxOn && maxOn > 0)
                state = false;
            chip.setOutput(i, state);
            if(state)
                on++;
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new RandomBit(getServer(), sign, this);
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"Maximum On Outputs", null};
        }
    }

    @Override
    public boolean isActive () {
        return true;
    }
}