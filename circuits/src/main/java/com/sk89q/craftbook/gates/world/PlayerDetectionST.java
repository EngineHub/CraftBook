package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;
import org.bukkit.block.Sign;

/**
 * @author Silthus
 */
public class PlayerDetectionST extends PlayerDetection implements SelfTriggeredIC {

    public PlayerDetectionST(Server server, Sign block) {
        super(server, block);
    }

    @Override
    public String getTitle() {
        return "Self Triggered Player Detection";
    }

    @Override
    public String getSignTitle() {
        return "ST P-DETECTION";
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
            return new PlayerDetectionST(getServer(), sign);
        }

	    @Override
	    public void verify(Sign sign) throws ICVerificationException {
		    ICUtil.verifySignSyntax(sign);
	    }
    }
}
