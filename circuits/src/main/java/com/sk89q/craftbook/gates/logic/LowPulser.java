package com.sk89q.craftbook.gates.logic;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;

/**
 * @author Silthus
 */
public class LowPulser extends Pulser {

    public LowPulser(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Low Pulser";
    }

    @Override
    public String getSignTitle() {

        return "LOW PULSER";
    }

    @Override
    protected boolean getInput(ChipState chip) {

        return !chip.getInput(0);
    }

    public static class Factory extends Pulser.Factory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new LowPulser(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Fires a (choosable) pulse of high-signals with a choosable length of the signal " +
                    "and the pause between the pulses when the input goes from high to low.";
        }
    }
}
