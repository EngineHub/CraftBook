package com.sk89q.craftbook.gates.world.sensors;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;

/**
 * @author Silthus
 */
public class PlayerSensorST extends PlayerSensor implements SelfTriggeredIC {

    public PlayerSensorST (Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle () {

        return "Self Triggered Player Detection";
    }

    @Override
    public String getSignTitle () {

        return "ST P-DETECTION";
    }

    @Override
    public void think (ChipState state) {

        state.setOutput(0, isDetected());
    }

    @Override
    public boolean isActive () {

        return true;
    }

    public static class Factory extends PlayerSensor.Factory implements RestrictedIC {

        public Factory (Server server) {

            super(server);
        }

        @Override
        public IC create (ChangedSign sign) {

            return new PlayerSensorST(getServer(), sign, this);
        }
    }
}
