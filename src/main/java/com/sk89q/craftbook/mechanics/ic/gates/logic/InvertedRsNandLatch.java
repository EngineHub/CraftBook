package com.sk89q.craftbook.mechanics.ic.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;

public class InvertedRsNandLatch extends AbstractIC {

    public InvertedRsNandLatch(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Inverted RS NAND latch";
    }

    @Override
    public String getSignTitle() {

        return "INV RS NAND LAT";
    }

    @Override
    public void trigger(ChipState chip) {

        boolean set = chip.getInput(0);
        boolean reset = chip.getInput(1);

        if (!set && !reset) {
            chip.setOutput(0, true);
        } else if (set && !reset) {
            chip.setOutput(0, false);
        } else if (!set && reset) {
            chip.setOutput(0, true);
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                    "Set",//Inputs
                    "Reset",
                    "Nothing",
                    "Output",//Outputs
            };
        }

        @Override
        public IC create(ChangedSign sign) {

            return new InvertedRsNandLatch(getServer(), sign, this);
        }
    }
}
