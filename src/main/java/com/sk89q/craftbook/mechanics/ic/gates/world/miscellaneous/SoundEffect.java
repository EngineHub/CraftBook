package com.sk89q.craftbook.mechanics.ic.gates.world.miscellaneous;

import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.block.Block;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.util.RegexUtil;

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