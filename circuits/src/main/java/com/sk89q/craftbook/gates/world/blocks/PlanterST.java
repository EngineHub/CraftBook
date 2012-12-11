package com.sk89q.craftbook.gates.world.blocks;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

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
