package com.sk89q.craftbook.mechanics.ic.gates.world.miscellaneous;

import java.util.EnumSet;
import java.util.Set;

import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
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

        String[] effectInfo = RegexUtil.COLON_PATTERN.split(RegexUtil.EQUALS_PATTERN.split(getLine(2),2)[0], 3);

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
        try {
            types = EntityType.getDetected(RegexUtil.EQUALS_PATTERN.split(getLine(2),2)[1]);
        } catch(Exception e) {
            types = EnumSet.of(EntityType.PLAYER);
        }

        //Converter.
        boolean converting = false;
        if(getRawLine(3).toLowerCase().endsWith("p") && (!getRawLine(2).contains("=") || converting)) {
            getSign().setLine(2, getRawLine(2) + (!getRawLine(2).contains("=") ? "=p" : "p"));
            getSign().setLine(3, getRawLine(3).substring(0, getRawLine(3).length() - 1));
            converting = true;
        }
        if(getRawLine(3).toLowerCase().endsWith("m") && (!getRawLine(2).contains("=") || converting)) {
            getSign().setLine(2, getRawLine(2) + (!getRawLine(2).contains("=") ? "=m" : "m"));
            getSign().setLine(3, getRawLine(3).substring(0, getRawLine(3).length() - 1));
            converting = true;
        }
        if(getRawLine(3).toLowerCase().endsWith("p") && (!getRawLine(2).contains("=") || converting)) {
            getSign().setLine(2, getRawLine(2) + (!getRawLine(2).contains("=") ? "=p" : "p"));
            getSign().setLine(3, getRawLine(3).substring(0, getRawLine(3).length() - 1));
            converting = true;
        }
        if(converting)
            getSign().update(false);

        area = SearchArea.createArea(CraftBookBukkitUtil.toSign(getSign()).getBlock(), getLine(3));
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

        if(!state.getInput(0))
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
                    "id:level:time=entitytypes", "range=offset"
            };
        }
    }
}