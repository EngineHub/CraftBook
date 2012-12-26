package com.sk89q.craftbook.gates.logic;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;

public class Dispatcher extends AbstractIC {

    public Dispatcher(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Dispatcher";
    }

    @Override
    public String getSignTitle() {

        return "DISPATCHER";
    }

    @Override
    public void trigger(ChipState chip) {

        boolean value = chip.getInput(0);
        boolean targetB = chip.getInput(1);
        boolean targetC = chip.getInput(2);

        if (targetB) {
            chip.setOutput(1, value);
        }
        if (targetC) {
            chip.setOutput(2, value);
        }

    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Dispatcher(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Send middle signal out high sides.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {null, null};
            return lines;
        }
    }
}
