package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;

/**
 * @author Me4502
 */
public class FireBarrage extends FireShooter {

    public FireBarrage(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Fire Barrage";
    }

    @Override
    public String getSignTitle() {

        return "FIRE BARRAGE";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            shootFire(5);
        }
    }

    public static class Factory extends AbstractICFactory implements
    RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new FireBarrage(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Shoots a barrage of fire.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "speed:spread",
                    "vertical gain"
            };
            return lines;
        }
    }
}