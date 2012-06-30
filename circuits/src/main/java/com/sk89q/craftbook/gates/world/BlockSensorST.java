package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class BlockSensorST extends BlockSensor implements SelfTriggeredIC {

    public BlockSensorST(Server server, Sign sign) {
        super(server, sign);
    }

    @Override
    public String getTitle() {
        return "Self-triggered Block Sensor";
    }

    @Override
    public String getSignTitle() {
        return "ST BLOCK SENSOR";
    }

    @Override
    public void think(ChipState chip) {
        chip.setOutput(0, hasBlock());
    }


    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new BlockSensorST(getServer(), sign);
        }
    }

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public void trigger(ChipState chip) {}
}
