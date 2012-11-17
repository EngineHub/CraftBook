package com.sk89q.craftbook.gates.world.items;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;

/**
 * @author Silthus
 */
public class ItemNotSensorST extends ItemSensor implements SelfTriggeredIC {

    public ItemNotSensorST(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Self Triggered Item Not Sensor";
    }

    @Override
    public String getSignTitle() {

        return "ST ITEM NOT SENSOR";
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, !isDetected());
    }

    @Override
    public boolean isActive() {

        return true;
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ItemNotSensorST(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            ICUtil.verifySignSyntax(sign);
        }
    }
}
