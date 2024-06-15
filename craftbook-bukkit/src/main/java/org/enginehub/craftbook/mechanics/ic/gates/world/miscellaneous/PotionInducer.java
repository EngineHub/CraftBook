/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.mechanics.ic.gates.world.miscellaneous;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;
import org.enginehub.craftbook.util.EntityType;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.SearchArea;

import java.util.Set;

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

        String[] effectInfo = RegexUtil.COLON_PATTERN.split(RegexUtil.EQUALS_PATTERN.split(getLine(2), 2)[0], 3);

        int effectID, effectAmount, effectTime;

        try {
            effectID = Integer.parseInt(effectInfo[0]);
        } catch (Exception e) {
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
            types = EntityType.getDetected(RegexUtil.EQUALS_PATTERN.split(getLine(2), 2)[1]);
        } catch (Exception e) {
            types = Set.of(EntityType.PLAYER);
        }

        //Converter.
        boolean converting = false;
        if (getRawLine(3).toLowerCase().endsWith("p") && (!getRawLine(2).contains("=") || converting)) {
            getSign().setLine(2, Component.text(getRawLine(2) + (!getRawLine(2).contains("=") ? "=p" : "p")));
            getSign().setLine(3, Component.text(getRawLine(3).substring(0, getRawLine(3).length() - 1)));
            converting = true;
        }
        if (getRawLine(3).toLowerCase().endsWith("m") && (!getRawLine(2).contains("=") || converting)) {
            getSign().setLine(2, Component.text(getRawLine(2) + (!getRawLine(2).contains("=") ? "=m" : "m")));
            getSign().setLine(3, Component.text(getRawLine(3).substring(0, getRawLine(3).length() - 1)));
            converting = true;
        }
        if (getRawLine(3).toLowerCase().endsWith("p") && (!getRawLine(2).contains("=") || converting)) {
            getSign().setLine(2, Component.text(getRawLine(2) + (!getRawLine(2).contains("=") ? "=p" : "p")));
            getSign().setLine(3, Component.text(getRawLine(3).substring(0, getRawLine(3).length() - 1)));
            converting = true;
        }
        if (converting)
            getSign().update(false);

        area = SearchArea.createArea(getSign().getBlock(), getLine(3));
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

        if (!state.getInput(0))
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
                String[] bits = RegexUtil.COLON_PATTERN.split(PlainTextComponentSerializer.plainText().serialize(sign.getLine(2)), 3);
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