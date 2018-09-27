package com.sk89q.craftbook.mechanics.ic.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;

/**
 * Sets output based on D input while clock input is high.
 */
public class LevelTriggeredDFlipFlop extends AbstractIC {

    public LevelTriggeredDFlipFlop(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Level-triggered D flip flop";
    }

    @Override
    public String getSignTitle() {

        return "D LEVL FLIPFLOP";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, chip.getInput(1));
        }

        if (chip.getInput(2)) {
            chip.setOutput(0, false);
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                    "Sets Output to Input 2",//Inputs
                    "Carries over to Output 1",
                    "Sets Output 1 to Low.",
                    "Carried Value"//Outputs
            };
        }

        @Override
        public IC create(ChangedSign sign) {

            return new LevelTriggeredDFlipFlop(getServer(), sign, this);
        }
    }
}
