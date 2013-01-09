package com.sk89q.craftbook.circuits.gates.world.sensors;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.SelfTriggeredIC;

public class BlockSensorST extends BlockSensor implements SelfTriggeredIC {

    public BlockSensorST(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Self-triggered Block Sensor";
    }

    @Override
    public String getSignTitle() {

        return "ST BLOCK SENSOR";
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, ((Factory) getFactory()).invert ? !hasBlock() : hasBlock());
    }

    @Override
    public boolean isActive() {

        return true;
    }

    public static class Factory extends BlockSensor.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new BlockSensorST(getServer(), sign, this);
        }
    }
}
