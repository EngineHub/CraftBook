package com.sk89q.craftbook.circuits.gates.world.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.SelfTriggeredIC;
import org.bukkit.Server;

public class BlockBreakerST extends BlockBreaker implements SelfTriggeredIC {

    public BlockBreakerST(Server server, ChangedSign block, boolean above, ICFactory factory) {

        super(server, block, above, factory);
    }

    @Override
    public String getTitle() {

        return "Block Breaker ST";
    }

    @Override
    public String getSignTitle() {

        return "BLOCK BREAK ST";
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, breakBlock());
    }

    public static class Factory extends BlockBreaker.Factory {

        public Factory(Server server, boolean above) {

            super(server, above);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new BlockBreakerST(getServer(), sign, above, this);
        }
    }
}