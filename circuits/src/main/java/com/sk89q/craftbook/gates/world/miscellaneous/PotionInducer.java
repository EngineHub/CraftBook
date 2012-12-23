package com.sk89q.craftbook.gates.world.miscellaneous;

import org.bukkit.Server;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.ic.ICVerificationException;
import com.sk89q.craftbook.ic.RestrictedIC;

/**
 * @author Me4502
 */
public class PotionInducer extends AbstractIC {

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

    int radius = 10, effectID = 1, effectAmount = 1, effectTime = 10;
    boolean mobs = false;
    boolean players = true;

    @Override
    public void load() {

        String[] effectInfo = ICUtil.COLON_PATTERN.split(getSign().getLine(2), 3);
        try {
            effectID = Integer.parseInt(effectInfo[0]);
        }
        catch(Exception e){}
        try {
            effectAmount = Integer.parseInt(effectInfo[1]);
        }
        catch(Exception e){}
        try {
            effectTime = Integer.parseInt(effectInfo[2]);
        }
        catch(Exception e){}
        String line4 = getSign().getLine(3);
        if(line4.contains("pm")) {
            mobs = true;
            players = true;
        } else if(line4.contains("m")) {
            mobs = true;
            players = false;
        }
        else if(line4.contains("p")) {
            players = true;
            mobs = false;
        }
        line4 = line4.replace("m", "").replace("p", "");
        try {
            radius = Integer.parseInt(line4);
        }
        catch(Exception e){
            radius = 10;
        }
    }

    public void induce() {
        for (LivingEntity p : BukkitUtil.toSign(getSign()).getWorld().getLivingEntities()) {
            if(!mobs && !(p instanceof Player))
                continue;
            if(!players && p instanceof Player)
                continue;
            if (p.getLocation().distanceSquared(BukkitUtil.toSign(getSign()).getLocation()) > radius * radius)
                continue;
            p.addPotionEffect(new PotionEffect(PotionEffectType.getById(effectID), effectTime * 20, effectAmount - 1), true);
        }
    }

    @Override
    public void trigger(ChipState chip) {

        if(chip.getInput(0))
            induce();
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
                String[] bits = ICUtil.COLON_PATTERN.split(sign.getLine(2), 2);
                int effectId = Integer.parseInt(bits[0]);

                if (PotionEffectType.getById(effectId) == null)
                    throw new ICVerificationException("The third line must be a valid potion effect id.");
                if(bits.length > 1) {
                    try {
                        Integer.parseInt(bits[1]);
                    } catch (NumberFormatException e) {
                        throw new ICVerificationException("Invalid potion level.");
                    }
                }
                if(bits.length > 2) {
                    try {
                        Integer.parseInt(bits[2]);
                    } catch (NumberFormatException e) {
                        throw new ICVerificationException("Invalid potion length.");
                    }
                }
            } catch (NumberFormatException e) {
                throw new ICVerificationException("The third line must be a valid potion effect id.");
            }
        }

        @Override
        public String getDescription() {

            return "Gives nearby entities a potion effect.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "id:level:time",
                    "range (add a m to the end to only induce mobs or p for players (pm for both))"
            };
            return lines;
        }
    }
}
