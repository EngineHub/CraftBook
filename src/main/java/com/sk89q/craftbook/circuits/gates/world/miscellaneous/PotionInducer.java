package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.Vector;

/**
 * @author Me4502
 */
public class PotionInducer extends AbstractSelfTriggeredIC {

    public PotionInducer(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Potion Inducer";
    }

    @Override
    public String getSignTitle() {

        return "POTION INDUCER";
    }

    Vector radius;
    int effectID, effectAmount, effectTime;
    boolean mobs;
    boolean players;

    @Override
    public void load() {

        String[] effectInfo = RegexUtil.COLON_PATTERN.split(getLine(2), 3);
        try {
            effectID = Integer.parseInt(effectInfo[0]);
        }
        catch (Exception e) {
            effectID = 1;
        }
        try {
            effectAmount = Integer.parseInt(effectInfo[1]);
        } catch (Exception e) {
            effectAmount = 1;
        }
        try {
            effectTime = Integer.parseInt(effectInfo[2]);
        } catch (Exception e) {
            effectTime = 10;
        }
        String line4 = getSign().getLine(3).toLowerCase();
        if (line4.contains("pm")) {
            mobs = true;
            players = true;
        } else if (line4.contains("m")) {
            mobs = true;
            players = false;
        } else if (line4.contains("p")) {
            players = true;
            mobs = false;
        } else {
            players = true;
            mobs = false;
        }
        line4 = line4.replace("m", "").replace("p", "");
        radius = ICUtil.parseRadius(getSign(), 3);
    }

    public boolean induce() {

        boolean value = false;
        // chunks
        for (Entity entity : LocationUtil.getNearbyEntities(SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()).getLocation(), radius)) {
            if (entity.isValid() && entity instanceof LivingEntity) {
                LivingEntity liv = (LivingEntity) entity;
                if (!mobs && !(liv instanceof Player)) continue;
                if (!players && liv instanceof Player) continue;
                if(!LocationUtil.isWithinRadius(liv.getLocation(), BukkitUtil.toSign(getSign()).getLocation(), radius))
                    continue;
                liv.addPotionEffect(new PotionEffect(PotionEffectType.getById(effectID), effectTime * 20,
                        effectAmount - 1), true);
                value = true;
            }
        }
        return value;
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, induce());
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, induce());
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
        public IC create(ChangedSign sign) {

            return new PotionInducer(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            try {
                String[] bits = RegexUtil.COLON_PATTERN.split(sign.getLine(2), 3);
                int effectId = Integer.parseInt(bits[0]);

                if (PotionEffectType.getById(effectId) == null)
                    throw new ICVerificationException("The third line must be a valid potion effect id.");
            } catch (NumberFormatException e) {
                throw new ICVerificationException("The third line must be a valid potion effect id.");
            }
        }

        @Override
        public String getShortDescription() {

            return "Gives nearby entities a potion effect.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "id:level:time", "range (add a m to the end to only induce mobs or p for " +
                            "players (pm for both))"
            };
            return lines;
        }
    }
}
