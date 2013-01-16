package com.sk89q.craftbook.circuits.gates.world.sensors;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.circuits.ic.SelfTriggeredIC;
import com.sk89q.craftbook.util.ICUtil;

/**
 * @author Silthus
 */
public class EntitySensorST extends EntitySensor implements SelfTriggeredIC {

    public EntitySensorST(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Self-triggered Entity Sensor";
    }

    @Override
    public String getSignTitle() {

        return "ST ENTITY SENSOR";
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, isDetected());
    }

    @Override
    public boolean isActive() {

        return true;
    }

    public static class Factory extends EntitySensor.Factory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new EntitySensorST(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            ICUtil.verifySignSyntax(sign);
        }
    }
}
