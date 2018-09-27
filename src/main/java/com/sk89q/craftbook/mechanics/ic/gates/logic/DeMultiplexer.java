package com.sk89q.craftbook.mechanics.ic.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;

public class DeMultiplexer extends AbstractIC {

    public DeMultiplexer (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "De-Multiplexer";
    }

    @Override
    public String getSignTitle() {

        return "DEMULTIPLEXER";
    }

    @Override
    public void trigger(ChipState chip) {

        boolean swapper = chip.getInput(0);
        chip.setOutput(swapper ? 1 : 2, chip.getInput(1));
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                    "Swaps between Output 2 and 3",//Inputs
                    "Value to carry",
                    "Nothing",
                    "Nothing",//Outputs
                    "Output if Input 1 Low",
                    "Output if Input 1 High"
            };
        }

        @Override
        public IC create(ChangedSign sign) {

            return new DeMultiplexer(getServer(), sign, this);
        }
    }
}