package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.SelfTriggeredIC;
import org.bukkit.Server;
import org.bukkit.block.Sign;

public class PumpST extends Pump implements SelfTriggeredIC {

    public PumpST(Server server, Sign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Self-Triggered Pump";
    }

    @Override
    public String getSignTitle() {

        return "PUMP ST";
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public void think(ChipState state) {

        if (state.getInput(0)) {
            state.setOutput(0, scan());
        }
    }

    public static class Factory extends Pump.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new PumpST(getServer(), sign, this);
        }
    }
}