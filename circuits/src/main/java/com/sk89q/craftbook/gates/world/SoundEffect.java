package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.block.Block;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.util.SignUtil;

public class SoundEffect extends AbstractIC {

    public SoundEffect(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
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
            String[] split = ICUtil.COLON_PATTERN.split(getSign().getLine(2));
            float volume = Float.parseFloat(split[0]) / 100f;
            byte pitch;
            try {
                pitch = (byte) (Integer.parseInt(split[1]) / 1.5873015873015873015873015873016);
            } catch (Exception e) {
                pitch = 0;
            }
            Block b = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());
            String soundName = getSign().getLine(3).trim();
            if (!soundName.isEmpty()) {
                b.getWorld().playSound(b.getLocation(), Sound.valueOf(soundName), volume, pitch);
            }
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
    }
}