package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;

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

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new FireBarrage(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Shoots a barrage of fire.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"speed:spread", "vertical gain"};
        }
    }
}