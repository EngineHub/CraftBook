package com.sk89q.craftbook.circuits.gates.world.sensors;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.*;
import org.bukkit.Server;

/**
 * @author Silthus
 */
public class ItemSensorST extends ItemSensor implements SelfTriggeredIC {

    public ItemSensorST(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Self Triggered Item Sensor";
    }

    @Override
    public String getSignTitle() {

        return "ST ITEM SENSOR";
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, isDetected());
    }

    @Override
    public boolean isActive() {

        return true;
    }

    public static class Factory extends ItemSensor.Factory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ItemSensorST(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            ICUtil.verifySignSyntax(sign);
        }
    }
}
