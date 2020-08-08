/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
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

package com.sk89q.craftbook.mechanics.ic.gates.world.miscellaneous;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.craftbook.util.RegexUtil;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Server;

/**
 * @author Me4502
 */
public class ParticleEffect extends AbstractSelfTriggeredIC {

    public ParticleEffect(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Particle Effect";
    }

    @Override
    public String getSignTitle() {

        return "PARTICLE EFFECT";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            doEffect();
        }
    }

    @Override
    public void think(ChipState state) {

        if (state.getInput(0)) {
            doEffect();
        }
    }

    int effectID;
    int effectData;
    int times;
    Location offset;

    @Override
    public void load() {

        String[] eff = RegexUtil.COLON_PATTERN.split(RegexUtil.EQUALS_PATTERN.split(getSign().getLine(2))[0], 2);
        try {
            effectID = Integer.parseInt(eff[0]);
        } catch (Exception e) {
            try {
                effectID = Effect.valueOf(eff[0]).getId();
            }
            catch(Exception ignored){}
        }
        if (Effect.getById(effectID) == null) return;
        try {
            effectData = Integer.parseInt(eff[1]);
        } catch (Exception e) {
            effectData = 0;
        }

        try {
            times = Integer.parseInt(getSign().getLine(3));
        } catch (Exception ignored) {
            times = 1;
        }
        if(getLine(2).contains("=")) {
            String extra = getLine(2).split("=")[1];
            getSign().setLine(2, getLine(2).split("=")[0]);
            getSign().setLine(3, getLine(3) + "=" + extra);
            getSign().update(false);
        }
        if(getLine(3).contains("="))
            offset = ICUtil.parseBlockLocation(getSign(), 3).getLocation();
        else
            offset = getBackBlock().getLocation().add(0, 1, 0);
    }

    public void doEffect() {

        if (effectID == 0) return;

        for (int i = 0; i < times; i++) {
            offset.getWorld().playEffect(offset, Effect.getById(effectID), effectData, 50);
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ParticleEffect(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Creates particle effects.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"effectID:effectData=xOff:yOff:zOff", "amount of particles=offset"};
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            try {
                String[] eff = RegexUtil.COLON_PATTERN.split(RegexUtil.EQUALS_PATTERN.split(sign.getLine(2))[0], 2);
                int effectID = 0, effectData;
                try {
                    effectID = Integer.parseInt(eff[0]);
                } catch (Exception e) {
                    try {
                        effectID = Effect.valueOf(eff[0]).getId();
                    }
                    catch(Exception ignored){}
                }
                if (Effect.getById(effectID) == null) throw new ICVerificationException("Invalid effect!");
                try {
                    effectData = Integer.parseInt(eff[1]);
                } catch (Exception e) {
                    effectData = 0;
                }
            }
            catch(Exception e) {
                throw new ICVerificationException("Invalid effect!");
            }
        }
    }
}
