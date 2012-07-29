package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;
import org.bukkit.block.Sign;

/**
 * @author Silthus
 */
public class PowerSensorST extends PowerSensor implements SelfTriggeredIC {

    public PowerSensorST(Server server, Sign block) {
        super(server, block);
    }

    @Override
    public String getTitle() {
        return "Self Triggered Power Sensor";
    }

    @Override
    public String getSignTitle() {

        return "ST POWER SENSOR";
    }

    @Override
    public void think(ChipState state) {
        state.setOutput(0, isPowered());
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
            return new PowerSensorST(getServer(), sign);
        }
    }
}
