package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.SelfTriggeredIC;
import org.bukkit.Server;

/**
 * @author Me4502
 */
public class ParticleEffectST extends ParticleEffect implements SelfTriggeredIC {

    public ParticleEffectST(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Self-triggered Particle Effect";
    }

    @Override
    public String getSignTitle() {

        return "ST PARTICLE EFFECT";
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public void think(ChipState state) {

        if (state.getInput(0)) {
            doEffect();
        }
    }

    public static class Factory extends ParticleEffect.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ParticleEffectST(getServer(), sign, this);
        }
    }
}
