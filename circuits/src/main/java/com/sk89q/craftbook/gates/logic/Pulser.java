package com.sk89q.craftbook.gates.logic;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.ChipState;
import org.bukkit.Server;
import org.bukkit.block.Sign;

/**
 * @author Silthus
 */
public class Pulser extends AbstractIC {

	public Pulser(Server server, Sign block) {
		super(server, block);
	}

	@Override
	public String getTitle() {
		return "Pulser";
	}

	@Override
	public String getSignTitle() {
		return "PULSER";
	}

	@Override
	public void trigger(ChipState chip) {
		//TODO: implement
	}
}
