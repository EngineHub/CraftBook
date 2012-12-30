package com.sk89q.craftbook.circuits.gates.logic;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import org.bukkit.Server;

/**
 * @author Silthus
 */
public class LowNotPulser extends NotPulser {

    public LowNotPulser(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Low Not Pulser";
    }

    @Override
    public String getSignTitle() {

        return "LOW NOT PULSER";
    }

    @Override
    protected boolean getInput(ChipState chip) {

        return !chip.getInput(0);
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

            return new LowNotPulser(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Fires a (choosable) pulse of low-signals with a choosable length of the signal "
                    + "and the pause between the pulses when the input goes from high to low.";
        }
    }
}
