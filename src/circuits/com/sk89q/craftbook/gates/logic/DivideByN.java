package com.sk89q.craftbook.gates.logic;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;

public class DivideByN extends BothTriggeredIC {

    private final int limit;
    private int ticks;
    
    public DivideByN(Server server, Sign block, boolean selfTriggered, Boolean risingEdge) {
        super(server, block, selfTriggered, risingEdge, "Divide-by-N", "DIVIDE BY N");
        int limitTemp = 10;
        try {
            limitTemp = Integer.parseInt(block.getLine(2));
        } catch (NumberFormatException nfe) {
            // Jerks invalid IC! How to tell?
        }
        this.limit = limitTemp;
        this.ticks = 0;
    }

    @Override
    public void think(ChipState state) {
        if (this.ticks > this.limit) {
            state.setOutput(0, true);
            this.ticks = 0;
        } else {
            state.setOutput(0, false);
            this.ticks++;
        }
    }
    
    public static class Factory extends AbstractICFactory {

        protected boolean risingEdge;

        public Factory(Server server, boolean risingEdge) {
            super(server);
            this.risingEdge = risingEdge;
        }

        @Override
        public IC create(Sign sign) {
            return new DivideByN(getServer(), sign, false, risingEdge);
        }
    }
    
    public static class FactoryST extends AbstractICFactory {

        public FactoryST(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new DivideByN(getServer(), sign, true, null);
        }
    }
}
