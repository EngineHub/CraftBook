package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;

/**
 * @author Me4502
 */
public class ParticleEffect extends AbstractIC {

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

    public void doEffect() {

        try {
            int effectID;
            String[] split = ICUtil.COLON_PATTERN.split(getSign().getLine(2), 2);
            try {
                effectID = Integer.parseInt(split[0]);
            } catch (Exception e) {
                effectID = Effect.valueOf(split[0]).getId();
            }
            if (Effect.getById(effectID) == null) return;
            int effectData;
            try {
                effectData = Integer.parseInt(split[1]);
            } catch (Exception e) {
                effectData = 0;
            }
            if (effectID == 2001 && Material.getMaterial(effectData) == null) return;
            int times = Integer.parseInt(getSign().getLine(3));
            Block b = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());
            for (int i = 0; i < times; i++) {
                b.getWorld().playEffect(b.getLocation().add(0, 1, 0), Effect.getById(effectID), effectData, 50);
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

            try {
                if (sign.getLine(0).equalsIgnoreCase("SET P-DOOR")) {
                    sign.setLine(1, "[MC1212]");
                    sign.update(false);
                    return new SetDoor(getServer(), sign, this);
                }
            } catch (Exception ignored) {
            }
            return new ParticleEffect(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Creates particle effects.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "effectID:effectData",
                    "amount of particles"
            };
            return lines;
        }
    }
}
