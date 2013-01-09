package com.sk89q.craftbook.circuits.gates.world.items;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.SelfTriggeredIC;

/**
 * @author Me4502
 */
public class ContainerCollectorST extends ContainerCollector implements SelfTriggeredIC {

    public ContainerCollectorST(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Self-triggered Container Collector";
    }

    @Override
    public String getSignTitle() {

        return "ST CONT COLLECT";
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, collect());
    }

    public static class Factory extends ContainerCollector.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ContainerCollectorST(getServer(), sign, this);
        }
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public void trigger(ChipState chip) {

    }
}