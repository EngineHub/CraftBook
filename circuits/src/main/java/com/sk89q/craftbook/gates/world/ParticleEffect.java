package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

/**
 * @author Me4502
 */
public class ParticleEffect extends AbstractIC {

    public ParticleEffect(Server server, Sign sign, ICFactory factory) {

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
            try {
                effectID = Integer.parseInt(getSign().getLine(2).split(":")[0]);
            } catch (Exception e) {
                effectID = Effect.valueOf(getSign().getLine(2).split(":")[0]).getId();
            }
            if (Effect.getById(effectID) == null) return;
            int effectData;
            try {
                effectData = Integer.parseInt(getSign().getLine(2).split(":")[1]);
            } catch (Exception e) {
                effectData = 0;
            }
            if (effectID == 2001 && Material.getMaterial(effectData) == null) return;
            int times = Integer.parseInt(getSign().getLine(3));
            Block b = SignUtil.getBackBlock(getSign().getBlock());
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
        public IC create(Sign sign) {

            try {
                if (sign.getLine(0).equalsIgnoreCase("SET P-DOOR")) {
                    sign.setLine(1, "[MC1212]");
                    sign.update();
                    return new SetDoor(getServer(), sign, this);
                }
            } catch (Exception e) {
            }
            return new ParticleEffect(getServer(), sign, this);
        }
    }
}
