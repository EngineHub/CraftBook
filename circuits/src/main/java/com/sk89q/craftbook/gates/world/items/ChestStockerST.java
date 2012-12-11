package com.sk89q.craftbook.gates.world.items;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class ChestStockerST extends ChestStocker implements SelfTriggeredIC {

    public ChestStockerST (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public String getTitle () {
        return "Self-Triggered Chest Stocker";
    }

    @Override
    public String getSignTitle () {
        return "STOCKER ST";
    }

    @Override
    public boolean isActive () {
        return true;
    }

    @Override
    public void think (ChipState chip) {

        chip.setOutput(0, stock());
    }

    public static class Factory extends ChestStocker.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ChestStockerST(getServer(), sign, this);
        }
    }
}