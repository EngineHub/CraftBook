package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;
import org.bukkit.block.Sign;

/**
 * @author Silthus
 */
public class DetectionST extends Detection implements SelfTriggeredIC {

	public DetectionST(Server server, Sign block) {
		super(server, block);
	}

	@Override
	public String getTitle() {
		return "Self Triggered Detection";
	}

	@Override
	public String getSignTitle() {
		return "ST DETECTION";
	}

	@Override
	public void think(ChipState state) {
		state.setOutput(0, isDetected());
	}

	@Override
	public boolean isActive() {
		return true;
	}

	public static class Factory extends AbstractICFactory implements RestrictedIC {

		public Factory(Server server) {
			super(server);
		}

		@Override
		public IC create(Sign sign) {
			return new DetectionST(getServer(), sign);
		}
	}
}
