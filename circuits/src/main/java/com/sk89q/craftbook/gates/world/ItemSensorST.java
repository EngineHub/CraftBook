package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;
import org.bukkit.block.Sign;

/**
 * @author Silthus
 */
public class ItemSensorST extends ItemSensor implements SelfTriggeredIC {

    public ItemSensorST(Server server, Sign block, ICFactory factory) {

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

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new ItemSensorST(getServer(), sign, this);
        }

        @Override
        public void verify(Sign sign) throws ICVerificationException {

            ICUtil.verifySignSyntax(sign);
        }
    }
}
