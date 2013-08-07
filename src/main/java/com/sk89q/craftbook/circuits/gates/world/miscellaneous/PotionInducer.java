package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
import com.sk89q.craftbook.util.EntityType;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SearchArea;

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

    SearchArea area;
    Set<EntityType> types;
    PotionEffect effect;

    @Override
    public void load() {

        String[] effectInfo = RegexUtil.COLON_PATTERN.split(getLine(2), 3);

        int effectID, effectAmount, effectTime;

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
        effect = new PotionEffect(PotionEffectType.getById(effectID), effectTime * 20, effectAmount - 1, true);
        String line4 = getSign().getLine(3).toLowerCase(Locale.ENGLISH);
        types = new HashSet<EntityType>();
        if (line4.startsWith("m")) {
            types.add(EntityType.MOB_ANY);
            line4 = line4.substring(1);
        }
        if (line4.contains("p")) {
            types.add(EntityType.PLAYER);
            line4 = line4.substring(1);
        }
        if (line4.startsWith("m") && !types.contains(EntityType.MOB_ANY)) {
            types.add(EntityType.MOB_ANY);
            line4 = line4.substring(1);
        }

        line4 = line4.replace("m", "").replace("p", "");

        area = SearchArea.createArea(BukkitUtil.toSign(getSign()).getBlock(), line4);
    }

    public boolean induce() {

        boolean value = false;

        for (Entity entity : area.getEntitiesInArea(types)) {
            if (entity.isValid() && entity instanceof LivingEntity) {
                LivingEntity liv = (LivingEntity) entity;
                liv.addPotionEffect(effect, true);
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

            return new String[] {
                    "id:level:time", "range=offset (add a m to the end to only induce mobs or p for players (pm for both))"
            };
        }
    }
}
