package com.sk89q.craftbook.mechanics.ic.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;

/**
 * Chooses one of two inputs as the output.
 */
public class Multiplexer extends AbstractIC {

    public Multiplexer(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Multiplexer";
    }

    @Override
    public String getSignTitle() {

        return "MULTIPLEXER";
    }

    @Override
    public void trigger(ChipState chip) {

        boolean swapper = chip.getInput(2);
        chip.setOutput(0, swapper ? chip.getInput(0) : chip.getInput(1));
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                    "Value to Carry if Input 3 High",//Inputs
                    "Value to Carry if Input 3 Low",
                    "Swaps between Input 1 and 2",
                    "Carried Value"//Outputs
            };
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Multiplexer(getServer(), sign, this);
        }
    }
}
