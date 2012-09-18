package com.sk89q.craftbook.gates.logic;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;

public class Dispatcher extends AbstractIC {

    public Dispatcher(Server server, Sign block) {
        super(server, block);
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
        public IC create(Sign sign) {

            return new Dispatcher(getServer(), sign);
        }
    }
}
