package com.sk89q.craftbook.circuits.gates.world.entity;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.SelfTriggeredIC;

public class AnimalHarvesterST extends AnimalHarvester implements SelfTriggeredIC {

    public AnimalHarvesterST (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public String getTitle () {
        return "Self-Triggered Animal Harvester";
    }

    @Override
    public String getSignTitle () {
        return "ANIMAL HARVEST ST";
    }

    @Override
    public boolean isActive () {
        return true;
    }

    @Override
    public void think (ChipState chip) {

        if(chip.getInput(0))
            chip.setOutput(0, harvest());
    }

    public static class Factory extends AnimalHarvester.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new AnimalHarvesterST(getServer(), sign, this);
        }
    }
}
