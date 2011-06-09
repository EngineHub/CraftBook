package com.sk89q.craftbook.gates.logic;

import java.util.Random;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class RandomIC extends AbstractIC implements SelfTriggeredIC {

	
	public RandomIC(Server server, Sign block) {
		super(server, block);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getTitle() {
		return "RANDOM";
	}

	@Override
	public String getSignTitle() {
		return "1-Bit Random Generator";
	}

	@Override
	public void trigger(ChipState chip) {
		// Empty ( No inputs )
	}

	@Override
	public void think(ChipState state) {
		state.setOutput(0, new Random().nextBoolean());
	}

	public static class Factory extends AbstractICFactory {

		public Factory(Server server) {
			super(server);
		}

		@Override
		public IC create(Sign sign) {
			return new RandomIC(getServer(), sign); 
		}
	
	}
	
}
