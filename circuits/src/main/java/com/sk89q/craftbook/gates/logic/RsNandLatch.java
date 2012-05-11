package com.sk89q.craftbook.gates.logic;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import org.bukkit.Server;
import org.bukkit.block.Sign;

/**
 * Simulates the function of a SR latch made from NAND gates.
 */
public class RsNandLatch extends AbstractIC {

    public RsNandLatch(Server server, Sign sign) {

        super(server, sign);
    }

    @Override
    public String getTitle() {

        return "RS NAND latch";
    }

    @Override
    public String getSignTitle() {

        return "RS NAND LATCH";
    }

    @Override
    public void trigger(ChipState chip) {

        boolean set = !chip.get(0);
        boolean reset = !chip.get(1);
        if (!set && !reset) {
            chip.set(3, true);
        } else if (!set && reset) {
            chip.set(3, true);
        } else if (set && !reset) {
            chip.set(3, false);
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new RsNandLatch(getServer(), sign);
        }
    }
}

