package com.sk89q.craftbook.mechanics.ic.gates.world.miscellaneous;

import org.bukkit.Server;
import org.bukkit.entity.ExperienceOrb;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;

public class XPSpawner extends AbstractIC {

    public XPSpawner (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    int amount, orbs;

    @Override
    public void load() {

        try {
            amount = Integer.parseInt(getLine(2));
        }
        catch(Exception e){
            amount = 1;
        }

        try {
            orbs = Integer.parseInt(getLine(3));
        }
        catch(Exception e){
            orbs = 1;
        }
    }

    @Override
    public String getTitle () {
        return "Experience Orb Spawner";
    }

    @Override
    public String getSignTitle () {
        return "XP SPAWNER";
    }

    @Override
    public void trigger (ChipState chip) {

        if(chip.getInput(0)) {

            for(int i = 0; i < orbs; i++) {
                ExperienceOrb orb = getLocation().getWorld().spawn(getLocation().add(0.5, 1.5, 0.5), ExperienceOrb.class);
                orb.setExperience(amount);
            }
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new XPSpawner(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Spawns an XP Orb.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"amount of xp", "amount of orbs"};
        }
    }
}
