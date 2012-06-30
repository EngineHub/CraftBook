package com.sk89q.craftbook.gates.logic;

import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.block.Sign;

/**
 * @author Silthus
 */
public class LowDelayer extends AbstractIC {

	public LowDelayer(Server server, Sign block) {
		super(server, block);
	}

	@Override
	public String getTitle() {
		return "Low Delayer";
	}

	@Override
	public String getSignTitle() {
		return "LOW_DELAYER";
	}

	@Override
	public void trigger(final ChipState chip) {
		int delay = Integer.parseInt(getSign().getLine(2));
		if (chip.getInput(0)) {
			chip.setOutput(0, true);
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(CircuitsPlugin.getInst(), new Runnable() {
				@Override
				public void run() {
					if (!chip.getInput(0)) {
						chip.setOutput(0, false);
					}
				}
			}, delay * 20);
		}
	}

	public static class Factory extends AbstractICFactory {

		public Factory(Server server) {
			super(server);
		}

		@Override
		public IC create(Sign sign) {
			return new LowDelayer(getServer(), sign);
		}
	}
}
