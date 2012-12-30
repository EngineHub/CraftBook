package com.sk89q.craftbook.circuits.gates.world.sensors;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.*;
import org.bukkit.Server;

/**
 * @author Silthus
 */
public class ItemNotSensor extends ItemSensor {

    public ItemNotSensor(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Item Not Sensor";
    }

    @Override
    public String getSignTitle() {

        return "ITEM NOT SENSOR";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, !isDetected());
        }
    }

    public static class Factory extends ItemSensor.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ItemNotSensor(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            ICUtil.verifySignSyntax(sign);
        }

        @Override
        public String getDescription() {

            return "Detects if an item is NOT within a given radius";
        }
    }
}
