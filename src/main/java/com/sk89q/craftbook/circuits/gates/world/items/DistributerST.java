package com.sk89q.craftbook.circuits.gates.world.items;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.SelfTriggeredIC;

public class DistributerST extends Distributer implements SelfTriggeredIC {

    public DistributerST(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Self-Triggered Distributer";
    }

    @Override
    public String getSignTitle() {

        return "DISTRIBUTER ST";
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, distribute());
    }

    public static class Factory extends Distributer.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new DistributerST(getServer(), sign, this);
        }
    }
}