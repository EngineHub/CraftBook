package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class ParticleEffectST extends ParticleEffect implements SelfTriggeredIC{

	public ParticleEffectST(Server server, Sign sign) {
		super(server, sign);
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
			doEffect(state);
		}
	}
	
	public static class Factory extends AbstractICFactory {

		public Factory(Server server) {
			super(server);
		}

		@Override
		public IC create(Sign sign) {
			return new ParticleEffectST(getServer(), sign);
		}
	}
}
