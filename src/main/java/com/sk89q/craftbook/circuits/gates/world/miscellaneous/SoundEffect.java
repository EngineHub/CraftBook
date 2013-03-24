package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.block.Block;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;

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
        sound = Sound.valueOf(soundName);
        if(sound == null) {
            for(Sound sound : Sound.values()) {

                if(soundName.trim().length() > 14 && sound.name().length() > 15 && sound.name().startsWith(soundName)) {
                    this.sound = sound;
                    break;
                }
            }
        }
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

        if (chip.getInput(0)) {
            doSound();
        }
    }

    public void doSound() {

        Block b = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());
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

            String[] lines = new String[] {"volume:pitch", "sound name"};
            return lines;
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            Sound sound = Sound.valueOf(sign.getLine(3).trim());
            if(sound == null) {
                for(Sound s : Sound.values()) {

                    if(sign.getLine(3).trim().length() > 14 && s.name().length() > 15 && s.name().startsWith(sign.getLine(3).trim())) {
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