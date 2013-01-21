package com.sk89q.craftbook.circuits.gates.world.sensors;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.SelfTriggeredIC;

public class ContentsSensorST extends ContentsSensor implements SelfTriggeredIC {

    public ContentsSensorST (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public String getTitle () {
        return "Self-Triggered Container Content Sensor";
    }

    @Override
    public String getSignTitle () {
        return "CONTENT SENSOR ST";
    }

    @Override
    public boolean isActive () {
        return true;
    }

    @Override
    public void think (ChipState chip) {

        chip.setOutput(0, sense());
    }

    public static class Factory extends ContentsSensor.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ContentsSensorST(getServer(), sign, this);
        }
    }
}