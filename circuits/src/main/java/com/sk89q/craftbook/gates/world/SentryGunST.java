package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class SentryGunST extends SentryGun implements SelfTriggeredIC{

    public SentryGunST(Server server, Sign block) {
	super(server, block);
    }
    
    @Override
    public String getTitle() {
	return "Self Triggered Sentry Gun";
    }

    @Override
    public String getSignTitle() {
	return "SENTRY GUN ST";
    }

    @Override
    public boolean isActive() {
	return false;
    }

    @Override
    public void think(ChipState state) {
	shoot();
    }

    public static class Factory extends AbstractICFactory implements
    RestrictedIC {

	public Factory(Server server) {
	    super(server);
	}

	@Override
	public IC create(Sign sign) {
	    return new SentryGunST(getServer(), sign);
	}
    }
}
