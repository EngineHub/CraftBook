package com.sk89q.craftbook.gates.world.miscellaneous;

import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.block.Block;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.SignUtil;

public class SoundEffect extends AbstractIC {

    public SoundEffect(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
        load();
    }

    float volume;
    byte pitch;
    Sound sound;

    public void load() {
        try {
            String[] split = ICUtil.COLON_PATTERN.split(getSign().getLine(2));
            volume = Float.parseFloat(split[0]) / 100f;
            try {
                pitch = (byte) (Integer.parseInt(split[1]) / 1.5873015873015873015873015873016);
            } catch (Exception e) {
                pitch = 0;
            }

            String soundName = getSign().getLine(3).trim();
            sound = Sound.valueOf(soundName);
        }
        catch(Exception e){}
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
        public String getDescription() {

            return "Plays a sound effect on high.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "volume:pitch",
                    "sound name"
            };
            return lines;
        }
    }
}