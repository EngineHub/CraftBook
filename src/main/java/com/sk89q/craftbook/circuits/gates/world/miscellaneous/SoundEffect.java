package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.*;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.block.Block;

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
            pitch = (byte) (Integer.parseInt(split[1]) / 1.5873015873015873015873015873016);
        } catch (Exception e) {
            pitch = 0;
        }

        String soundName = getSign().getLine(3).trim();
        sound = Sound.valueOf(soundName);
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

        try {
            Block b = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());
            b.getWorld().playSound(b.getLocation(), sound, volume, pitch);
        } catch (Exception ignored) {
        }
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
    }
}