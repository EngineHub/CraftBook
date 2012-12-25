package com.sk89q.craftbook.gates.world.blocks;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class CombineHarvesterST extends CombineHarvester implements SelfTriggeredIC {

    public CombineHarvesterST (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public String getTitle () {
        return "Self-Triggered Combine Harvester";
    }

    @Override
    public String getSignTitle () {
        return "HARVEST ST";
    }

    @Override
    public boolean isActive () {
        return true;
    }

    @Override
    public void think (ChipState chip) {

        chip.setOutput(0, harvest());
    }

    public static class Factory extends CombineHarvester.Factory {

        public Factory (Server server) {

            super(server);
        }

        @Override
        public IC create (ChangedSign sign) {

            return new CombineHarvesterST(getServer(), sign, this);
        }
    }
}