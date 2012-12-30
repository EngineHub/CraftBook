package com.sk89q.craftbook.circuits.gates.world.sensors;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.SelfTriggeredIC;
import org.bukkit.Server;

public class MovementSensorST extends MovementSensor implements SelfTriggeredIC {

    public MovementSensorST(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Self-Triggered Movement Sensor";
    }

    @Override
    public String getSignTitle() {

        return "MOVING SENSOR ST";
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public void think(ChipState chip) {

        check();
    }

    public static class Factory extends MovementSensor.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new MovementSensor(getServer(), sign, this);
        }
    }
}