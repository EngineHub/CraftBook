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

package org.enginehub.craftbook.mechanics.ic.gates.world.miscellaneous;

import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.block.Block;

import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;
import org.enginehub.craftbook.util.RegexUtil;

public class SoundEffect extends AbstractIC {

    public SoundEffect(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    float volume;
    byte pitch;
    Sound sound;

    @Override
    public void load() {

        String[] split = RegexUtil.COLON_PATTERN.split(getSign().getLine(2));
        try {
            volume = Float.parseFloat(split[0]) / 100f;
        } catch (Exception e) {
            volume = 100;
        }
        try {
            pitch = Byte.parseByte(split[1]);
        } catch (Exception e) {
            pitch = 0;
        }

        String soundName = getSign().getLine(3).trim();
        try {
            sound = Sound.valueOf(soundName);
        } catch(Exception e){}
        if(sound == null && getSign().getLine(3).trim().length() == 15) {
            for(Sound s : Sound.values()) {

                if(s.name().length() > 15 && s.name().startsWith(getSign().getLine(3))) {
                    sound = s;
                    break;
                }
            }
        }
        if(sound == null)
            sound = Sound.ENTITY_COW_AMBIENT;
    }

    @Override
    public String getTitle() {

        return "Sound Effect";
    }

    @Override
    public String getSignTitle() {

        return "SOUND EFFECT";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0))
            doSound();
    }

    public void doSound() {

        Block b = getBackBlock();
        b.getWorld().playSound(b.getLocation(), sound, volume, pitch);
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new SoundEffect(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Plays a sound effect on high.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"volume:pitch", "sound name"};
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            Sound sound = null;
            try {
                sound = Sound.valueOf(sign.getLine(3).trim());
            } catch(Exception e) {
            }
            if(sound == null && sign.getLine(3).trim().length() == 15) {
                for(Sound s : Sound.values()) {

                    if(s.name().length() > 15 && s.name().startsWith(sign.getLine(3))) {
                        sound = s;
                        break;
                    }
                }
            }
            if(sound == null)
                throw new ICVerificationException("Unknown Sound!");
        }
    }
}