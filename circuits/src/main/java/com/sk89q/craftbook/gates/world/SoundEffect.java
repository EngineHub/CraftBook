package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.SignUtil;
import net.minecraft.server.Packet62NamedSoundEffect;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

public class SoundEffect extends AbstractIC {

    public SoundEffect(Server server, Sign sign) {

        super(server, sign);
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
            float volume = Integer.parseInt(getSign().getLine(2).split(":")[0]) / 100;
            byte pitch;
            try {
                pitch = (byte) (Integer.parseInt(getSign().getLine(2).split(":")[1])
                        / 1.5873015873015873015873015873016);
            } catch (Exception e) {
                pitch = 0;
            }
            Block b = SignUtil.getBackBlock(getSign().getBlock());
            String soundName = getSign().getLine(3).trim();
            if (soundName.length() > 0)
                ((CraftServer) getServer()).getHandle().sendPacketNearby(b.getX(), b.getY() + 1, b.getZ(), 50,
                        ((CraftWorld) getSign().getWorld()).getHandle().dimension,
                        new Packet62NamedSoundEffect(soundName, b.getX(), b.getY() + 1, b.getZ(), volume, pitch));
        } catch (Exception ignored) {
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new SoundEffect(getServer(), sign);
        }
    }
}