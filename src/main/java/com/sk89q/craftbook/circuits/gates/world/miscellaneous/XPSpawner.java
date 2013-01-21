package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import org.bukkit.Server;
import org.bukkit.entity.ExperienceOrb;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.SignUtil;

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
                ExperienceOrb orb = BukkitUtil.toSign(getSign()).getWorld().spawn(SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()).getRelative(0, 1, 0).getLocation(), ExperienceOrb.class);
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

            String[] lines = new String[] {"amount of xp", "amount of orbs"};
            return lines;
        }
    }
}
