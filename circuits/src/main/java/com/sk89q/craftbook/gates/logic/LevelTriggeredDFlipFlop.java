package com.sk89q.craftbook.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;

/**
 * Sets output based on D input while clock input is high.
 */
public class LevelTriggeredDFlipFlop extends AbstractIC {

    public LevelTriggeredDFlipFlop (Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle () {

        return "Level-triggered D flip flop";
    }

    @Override
    public String getSignTitle () {

        return "D LEVL FLIPFLOP";
    }

    @Override
    public void trigger (ChipState chip) {

        if (chip.get(0)) {
            chip.set(3, chip.get(1));
        }

        if (chip.get(2)) {
            chip.set(3, false);
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory (Server server) {

            super(server);
        }

        @Override
        public IC create (ChangedSign sign) {

            return new LevelTriggeredDFlipFlop(getServer(), sign, this);
        }
    }
}
