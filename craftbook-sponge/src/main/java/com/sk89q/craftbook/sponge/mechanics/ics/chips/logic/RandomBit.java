/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics.ics.chips.logic;

import com.sk89q.craftbook.core.util.RegexUtil;
import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.ICFactory;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.concurrent.ThreadLocalRandom;

public class RandomBit extends IC {

    public RandomBit(ICFactory<RandomBit> icFactory, Location<World> block) {
        super(icFactory, block);
    }

    private int maxOn, minOn;

    @Override
    public void load() {
        super.load();

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
    public void trigger() {
        if (getPinSet().getInput(0, this)) {
            int on = 0;
            int outputs = 0;
            for (short i = 0; i < getPinSet().getOutputCount(); i++) {
                outputs++;
            }
            minOn = Math.min(minOn, outputs);
            if (maxOn < minOn && maxOn >= 0) {
                maxOn = minOn;
            }
            boolean first = true;
            do {
                if (on >= maxOn && maxOn >= 0)
                    break;
                for (short i = 0; i < getPinSet().getOutputCount(); i++) {
                    if (first) {
                        getPinSet().setOutput(i, false, this); //Turn it off before changing it.
                    }
                    boolean state = ThreadLocalRandom.current().nextBoolean();
                    boolean changed = false;
                    if (on >= maxOn && maxOn >= 0) {
                        state = false;
                    }
                    if (state && !getPinSet().getOutput(i, this)) {
                        getPinSet().setOutput(i, true, this); //Only change if needed
                        changed = true;
                    }
                    if (state && changed) {
                        on++;
                    }
                }
                first = false;
            } while (on < minOn);
        }
    }

    public static class Factory implements ICFactory<RandomBit> {

        @Override
        public RandomBit createInstance(Location<World> location) {
            return new RandomBit(this, location);
        }

        @Override
        public String[] getLineHelp() {
            return new String[] {
                    "Min On:Max On",
                    ""
            };
        }

        @Override
        public String[][] getPinHelp() {

            return new String[][] {
                    new String[] {
                        "High to output random state"
                    },
                    new String[] {
                        "Outputs random state"
                    }
            };
        }
    }
}
