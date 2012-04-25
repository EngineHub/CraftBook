package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class ChestCollectorST extends ChestCollector implements SelfTriggeredIC{

    public ChestCollectorST(Server server, Sign sign) {
        super(server, sign, true);
    }

    @Override
    public String getTitle() {
        return "Self-triggered Chest Collector";
    }

    @Override
    public String getSignTitle() {
        return "ST CHEST COLLECT";
    }

    @Override
    public void think(ChipState chip) {
    	chip.setOutput(0, collect());
    }


    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new ChestCollectorST(getServer(), sign);
        }
    }

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public void trigger(ChipState chip) {}

	@Override
	public void unload() {
		
	}

	
}
