package com.sk89q.craftbook.gates.logic;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;

/**
 * @author Silthus
 */
public class NotPulser extends Pulser {

    public NotPulser(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Not Pulser";
    }

    @Override
    public String getSignTitle() {

        return "NOT PULSER";
    }

    @Override
    protected void setOutput(ChipState chip, boolean on) {

        chip.setOutput(0, !on);
    }

    public static class Factory extends Pulser.Factory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new NotPulser(getServer(), sign, this);
        }

        @Override
        public String getDescription() {
            return "Fires a (choosable) pulse of low-signals with a choosable length of the signal " +
                    "and the pause between the pulses when the input goes from low to high.";
        }
    }
}
