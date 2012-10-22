package com.sk89q.craftbook.gates.logic;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;

/**
 * A JK flip flop.
 * A JK Flip Flop is like a SR Latch (S = J, R = K), but if both J and K is
 * high, it toggles, and it has a clock.
 *
 * @author sindreij
 */
public class JkFlipFlop extends AbstractIC {

    public JkFlipFlop(Server server, Sign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "JK negative edge-triggered flip flop";
    }

    @Override
    public String getSignTitle() {

        return "JK EDGE FLIP FLOP";
    }

    @Override
    public void trigger(ChipState chip) {

        boolean j = chip.get(1); //Set
        boolean k = chip.get(2); //Reset
        if (chip.isTriggered(0) && !chip.get(0)) if (j && k) {
            chip.set(3, !chip.get(3));
        } else if (j && !k) {
            chip.set(3, true);
        } else if (k) {
            chip.set(3, false);
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new JkFlipFlop(getServer(), sign, this);
        }
    }
}

