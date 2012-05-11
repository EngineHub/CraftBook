// $Id$
/*
 * CraftBook
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

package com.sk89q.craftbook.gates;

import com.sk89q.craftbook.ic.families.*;
import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;
import org.bukkit.block.Sign;

import java.util.Random;

import static com.sk89q.craftbook.gates.GateUtil.*;

/**
 * Simple ICs which don't affect the world, and only act as logical gates.
 */
public class LogicICSet extends ICSet {
    public LogicICSet(Server s) {
        super(s);
    }

    /*------------------\
    | Random Generators |
    \------------------*/

    private final Random random = new Random();

    @ICSetTrigger(
        name      = "MC1020",
        title     = "Random Bit",
        signTitle = "RANDOM BIT",
        family    = FamilySISO.class
    )
    public void MC1020_trigger(ChipState chip, Sign sign) {
        if (chip.getInput(0)) {
            chip.setOutput(0, random.nextBoolean());
        }
    }

    @ICSetTrigger(
        name      = "MC2020",
        title     = "Random 3-bit",
        signTitle = "3-BIT RANDOM",
        family    = FamilySI3O.class
    )
    public void MC2020_trigger(ChipState chip, Sign sign) {
        if (chip.getInput(0)) {
            chip.setOutput(0, random.nextBoolean());
            chip.setOutput(1, random.nextBoolean());
            chip.setOutput(2, random.nextBoolean());
        }
    }

    /*------------\
    | Logic Gates |
    \------------*/

    @ICSetTrigger(
        name      = "MC1000",
        title     = "Repeater",
        signTitle = "REPEATER",
        family    = FamilySISO.class
    )
    public void MC1000_trigger(ChipState chip, Sign sign) {
        chip.setOutput(0, chip.getInput(0));
    }

    @ICSetTrigger(
        name      = "MC1001",
        title     = "Inverter",
        signTitle = "INVERTER",
        family    = FamilySISO.class
    )
    public void MC1001_trigger(ChipState chip, Sign sign) {
        chip.setOutput(0, !chip.getInput(0));
    }

    @ICSetTrigger(
        name      = "MC1017",
        title     = "Toggle Flip Flop",
        signTitle = "TOGGLE",
        family    = FamilySISO.class
    )
    public void MC1017_trigger(ChipState chip, Sign sign) {
        if (chip.getInput(0)) chip.setOutput(0, !chip.getOutput(0));
    }

    @ICSetTrigger(
        name      = "MC1018",
        title     = "Toggle Flip Flop",
        signTitle = "TOGGLE",
        family    = FamilySISO.class
    )
    public void MC1018_trigger(ChipState chip, Sign sign) {
        if (!chip.getInput(0)) chip.setOutput(0, !chip.getOutput(0));
    }

    @ICSetTrigger(
        name      = "MC1420",
        title     = "Clock Divider",
        signTitle = "CLOCK DIVIDER",
        family    = FamilySISO.class
    )
    public void MC1420_trigger(ChipState chip, Sign sign) {
    	int reset = getIntOrElse(sign,2,2);
    	int count = getIntOrElse(sign,3,0);
        reset = clamp(reset, 2, 128);

		count = (count+1) % reset;
        if(count==0) chip.setOutput(0, !(chip.getOutput(0)));
        sign.setLine(3, Integer.toString(count));
    }

    @ICSetTrigger(
        name      = "MC3002",
        title     = "And Gate",
        signTitle = "AND",
        family    = Family3ISO.class
    )
    public void MC3002_trigger(ChipState chip, Sign sign) {
        boolean on = true, valid = false;
        for (int i = 0; i < chip.getInputCount(); i++) {
        	if (chip.isValid(i)) {
        		valid = true;
                on &= chip.getInput(i);
            }
        }

        // Condition; all valid must be ON, at least one valid.
        chip.setOutput(0, valid && on);
    }

    @ICSetTrigger(
        name      = "MC3003",
        title     = "Nand Gate",
        signTitle = "NAND",
        family    = Family3ISO.class
    )
    public void MC3003_trigger(ChipState chip, Sign sign) {
        boolean on = true, valid = false;
        for (int i = 0; i < chip.getInputCount(); i++) {
        	if (chip.isValid(i)) {
        		valid = true;
                on &= !chip.getInput(i);
            }
        }

        // Condition; all valid must NOT be ON, at least one valid.
        chip.setOutput(0, valid && on);
    }

    @ICSetTrigger(
        name      = "MC3020",
        title     = "Xor Gate",
        signTitle = "XOR",
        family    = Family3ISO.class
    )
    public void MC3020_trigger(ChipState chip, Sign sign) {
        boolean value = false;
    	for (int i = 0; i < chip.getInputCount(); i++) {
    		if (chip.isValid(i)) {
                value ^= chip.getInput(i);
    		}
    	}

        chip.setOutput(0, value);
    }

    @ICSetTrigger(
        name      = "MC3021",
        title     = "Xnor Gate",
        signTitle = "XNOR",
        family    = Family3ISO.class
    )
    public void MC3021_trigger(ChipState chip, Sign sign) {
        boolean value = false;
    	for (int i = 0; i < chip.getInputCount(); i++) {
    		if (chip.isValid(i)) {
                value ^= chip.getInput(i);
    		}
    	}

        chip.setOutput(0, !value);
    }

    @ICSetTrigger(
        name      = "MC3030",
        title     = "RS-NOR flip-flop",
        signTitle = "RS-NOR",
        family    = Family3ISO.class
    )
    public void MC3030_trigger(ChipState chip, Sign sign) {
        boolean set = chip.get(0);
    	boolean reset = chip.get(1) || chip.get(2);

    	if (reset) chip.set(3, false);
    	else if (set) chip.set(3, true);
    }

    @ICSetTrigger(
        name      = "MC3031",
        title     = "Inverted RS NAND latch",
        signTitle = "INV RS NAND LAT",
        family    = Family3ISO.class
    )
    public void MC3031_trigger(ChipState chip, Sign sign) {
        boolean set = chip.get(0);
        boolean reset = chip.get(1);
        if (!set && !reset) {
            chip.set(3, true);
        } else if (set && !reset) {
            chip.set(3, false);
        } else if (!set && reset) {
            chip.set(3, true);
        }
    }

    @ICSetTrigger(
        name      = "MC3032",
        title     = "JK negative edge-triggered flip flop",
        signTitle = "JK EDGE",
        family    = Family3ISO.class
    )
    public void MC3032_trigger(ChipState chip, Sign sign) {
        boolean j = chip.get(1); //Set
        boolean k = chip.get(2); //Reset
        if (chip.isTriggered(0) && !chip.get(0)) {
            if (j && k) {
                chip.set(3, !chip.get(3));
            } else if (j && !k) {
                chip.set(3, true);
            } else if (!j && k) {
                chip.set(3, false);
            }
        }
    }

    @ICSetTrigger(
        name      = "MC3033",
        title     = "RS NAND latch",
        signTitle = "RS NAND LATCH",
        family    = Family3ISO.class
    )
    public void MC3033_trigger(ChipState chip, Sign sign) {
        boolean set = !chip.get(0);
        boolean reset = !chip.get(1);
        if (!set && !reset) {
            chip.set(3, true);
        } else if (!set && reset) {
            chip.set(3, true);
        } else if (set && !reset) {
            chip.set(3, false);
        }
    }

    @ICSetTrigger(
        name      = "MC3034",
        title     = "Edge triggered D flip-flop",
        signTitle = "EDGE-D",
        family    = Family3ISO.class
    )
    public void MC3034_trigger(ChipState chip, Sign sign) {
        if (chip.get(2)) // reset
    		chip.set(3, false);
    	else if (chip.get(1) && chip.isTriggered(1)) // clock, rising signal only
    		chip.set(3, chip.get(0));
    }

    @ICSetTrigger(
        name      = "MC3036",
        title     = "Level-triggered D flip flop",
        signTitle = "D LEVL FLIPFLOP",
        family    = Family3ISO.class
    )
    public void MC3036_trigger(ChipState chip, Sign sign) {
        if (chip.get(0))
            chip.set(3, chip.get(1));

        if (chip.get(2))
            chip.set(3, false);
    }

    @ICSetTrigger(
        name      = "MC3040",
        title     = "Multiplexer",
        signTitle = "MULTIPLEXER",
        family    = Family3ISO.class
    )
    public void MC3040_trigger(ChipState chip, Sign sign) {
        boolean swapper = chip.get(2);
        chip.set(3, swapper ? chip.get(0) : chip.get(1));
    }
}