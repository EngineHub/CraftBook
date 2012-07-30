package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;

/**
 * @author Silthus
 */
public class ItemNotSensor extends ItemSensor {

    public ItemNotSensor(Server server, Sign block) {
	super(server, block);
    }

    @Override
    public String getTitle() {
	return "Item Not Sensor";
    }

    @Override
    public String getSignTitle() {
	return "ITEM NOT SENSOR";
    }

    @Override
    public void trigger(ChipState chip) {
	if (chip.getInput(0)) {
	    chip.setOutput(0, !isDetected());
	}
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

	public Factory(Server server) {
	    super(server);
	}

	@Override
	public IC create(Sign sign) {
	    return new ItemNotSensor(getServer(), sign);
	}
    }
}
