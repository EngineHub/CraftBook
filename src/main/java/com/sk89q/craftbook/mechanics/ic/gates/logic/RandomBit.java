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

package com.sk89q.craftbook.mechanics.ic.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.util.RegexUtil;

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

    int maxOn,minOn;

    @Override
    public void load() {

        try {
            if(getLine(2).contains(":")) {
                String[] parts = RegexUtil.COLON_PATTERN.split(getLine(2));
                maxOn = Integer.parseInt(parts[1]);
                minOn = Integer.parseInt(parts[0]);
            } else {
                maxOn = Integer.parseInt(getLine(2));
                minOn = 0;
            }
        } catch(Exception e){
            maxOn = -1;
            minOn = 0;
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
        int outputs = 0;
        for(short i = 0; i < chip.getInputCount(); i++)
            if(chip.isValid(i))
                outputs++;
        minOn = Math.min(minOn, outputs);
        if(maxOn < minOn && maxOn >= 0)
            maxOn = minOn;
        boolean first = true;
        do {
            if(on >= maxOn && maxOn >= 0) break;
            for (short i = 0; i < chip.getOutputCount(); i++) {
                if(!chip.isValid(i)) continue;
                if(first)
                    chip.setOutput(i, false); //Turn it off before changing it.
                boolean state = CraftBookPlugin.inst().getRandom().nextBoolean();
                boolean changed = false;
                if(on >= maxOn && maxOn >= 0)
                    state = false;
                if(state && !chip.getOutput(i)) {
                    chip.setOutput(i, state); //Only change if needed
                    changed = true;
                }
                if(state && changed)
                    on++;
                else if(!state && changed)
                    on--;
            }
            first = false;
        } while(on < minOn);
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
        public String[] getLongDescription() {

            return new String[]{
                    "The '''MC1020''' generates a random state whenever the input (the \"clock\") goes from low to high."
            };
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            String[] pins = new String[state.getInputCount() + state.getOutputCount()];

            pins[0] = "Trigger IC";

            for(int i = 1; i < pins.length; i++)
                pins[i] = "Random Output";

            return pins;
        }

        @Override
        public String getShortDescription() {

            return "Randomly sets the output on high.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"Min Outputs:Max Outputs", null};
        }
    }
}