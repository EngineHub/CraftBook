package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class ParticleEffectST extends ParticleEffect implements SelfTriggeredIC{

	public ParticleEffectST(Server server, Sign sign, boolean risingEdge) {
		super(server, sign, risingEdge);
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
		if (risingEdge && state.getInput(0) || (!risingEdge && !state.getInput(0))) {
			doEffect(state);
		}
	}
	
	public static class Factory extends AbstractICFactory {

		protected boolean risingEdge;

		public Factory(Server server, boolean risingEdge) {
			super(server);
			this.risingEdge = risingEdge;
		}

		@Override
		public IC create(Sign sign) {
			return new ParticleEffectST(getServer(), sign, risingEdge);
		}
	}
}
