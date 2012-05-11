package com.sk89q.craftbook.gates.logic;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import org.bukkit.Server;
import org.bukkit.block.Sign;

/**
 * Chooses one of two inputs as the output.
 */
public class Multiplexer extends AbstractIC {

    public Multiplexer(Server server, Sign sign) {

        super(server, sign);
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
        public IC create(Sign sign) {

            return new Multiplexer(getServer(), sign);
        }
    }
}

