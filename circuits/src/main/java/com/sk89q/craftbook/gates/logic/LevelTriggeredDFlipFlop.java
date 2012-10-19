package com.sk89q.craftbook.gates.logic;

import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;
import org.bukkit.block.Sign;

/**
 * Sets output based on D input while clock input is high.
 */
public class LevelTriggeredDFlipFlop extends AbstractIC {

    public LevelTriggeredDFlipFlop(Server server, Sign sign, ICFactory factory) {

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

        if (chip.get(0)) {
            chip.set(3, chip.get(1));
        }

        if (chip.get(2)) {
            chip.set(3, false);
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new LevelTriggeredDFlipFlop(getServer(), sign, this);
        }
    }
}

