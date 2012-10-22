package com.sk89q.craftbook.gates.logic;

import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;
import org.bukkit.block.Sign;

/**
 * @author Silthus
 */
public class NotPulser extends Pulser {

    public NotPulser(Server server, Sign block, ICFactory factory) {

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

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new NotPulser(getServer(), sign, this);
        }
    }
}
