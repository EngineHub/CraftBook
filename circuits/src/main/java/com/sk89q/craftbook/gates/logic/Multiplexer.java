package com.sk89q.craftbook.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;

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

        boolean swapper = chip.get(2);
        chip.set(3, swapper ? chip.get(0) : chip.get(1));
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Multiplexer(getServer(), sign, this);
        }
    }
}

