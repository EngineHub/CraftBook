package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;

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

    public static class Factory extends AbstractICFactory implements
    RestrictedIC {

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
