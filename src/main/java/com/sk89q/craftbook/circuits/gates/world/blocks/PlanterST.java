package com.sk89q.craftbook.circuits.gates.world.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.SelfTriggeredIC;
import org.bukkit.Server;

public class PlanterST extends Planter implements SelfTriggeredIC {

    public PlanterST(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public String getTitle() {

        return "Self-Triggered Planter";
    }

    @Override
    public String getSignTitle() {

        return "PLANTER ST";
    }

    @Override
    public void think(ChipState state) {

        plant();
    }

    public static class Factory extends Planter.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new PlanterST(getServer(), sign, this);
        }
    }
}
