package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.circuits.ic.SelfTriggeredIC;

/**
 * @author Me4502
 */
public class SentryGunST extends SentryGun implements SelfTriggeredIC {

    public SentryGunST(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Self Triggered Sentry Gun";
    }

    @Override
    public String getSignTitle() {

        return "SENTRY GUN ST";
    }

    @Override
    public boolean isActive() {

        return false;
    }

    @Override
    public void think(ChipState state) {

        shoot();
    }

    public static class Factory extends SentryGun.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new SentryGunST(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            try {
                String line = sign.getLine(3);
                if (line != null && !line.contains("")) {
                    Integer.parseInt(line);
                }
            } catch (Exception e) {
                throw new ICVerificationException("You need to give a radius in line four.");
            }
        }
    }
}
