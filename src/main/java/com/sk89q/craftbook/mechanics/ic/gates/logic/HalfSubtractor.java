package com.sk89q.craftbook.mechanics.ic.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;

public class HalfSubtractor extends AbstractIC {

    public HalfSubtractor(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Half Subtractor";
    }

    @Override
    public String getSignTitle() {

        return "HALF SUBTRACTOR";
    }

    @Override
    public void trigger(ChipState chip) {

        boolean B = chip.getInput(1);
        boolean C = chip.getInput(2);

        boolean S = B ^ C;
        boolean Bo = !B & C;

        chip.setOutput(0, S);
        chip.setOutput(1, Bo);
        chip.setOutput(2, Bo);
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                    "Nothing",//Inputs
                    "Bit to subtract from (minuend)",
                    "Bit to subtract (subtrahend)",
                    "Difference",//Outputs
                    "Borrow",
                    "Borrow (Same as Output 2)"
            };
        }

        @Override
        public IC create(ChangedSign sign) {

            return new HalfSubtractor(getServer(), sign, this);
        }
    }
}